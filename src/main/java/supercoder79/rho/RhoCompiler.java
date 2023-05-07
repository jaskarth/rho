package supercoder79.rho;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Holder;
import net.minecraft.util.CubicSpline;
import net.minecraft.util.ToFloatFunction;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.DensityFunctions;
import org.objectweb.asm.*;
import org.objectweb.asm.commons.AnalyzerAdapter;
import supercoder79.rho.ast.McToAst;
import supercoder79.rho.ast.Node;
import supercoder79.rho.ast.common.ConstNode;
import supercoder79.rho.ast.common.ReturnNode;
import supercoder79.rho.ast.high.complex.CacheFlatNode;
import supercoder79.rho.ast.low.GetFieldNode;
import supercoder79.rho.ast.low.InvokeNode;
import supercoder79.rho.gen.CodegenContext;
import supercoder79.rho.gen.DotExporter;
import supercoder79.rho.opto.RunOptoPasses;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;

public final class RhoCompiler {
    public static boolean isCompilingCurrently = false;
    public static String currentName = "";
    public static String currentStatus = "";
    public static boolean DO_COMPILE = true;
    public static boolean COMPILE_RECURSIVELY = true;
    public static boolean DUMP_DEBUG_DATA = true;

    // TODO: handle on-demand compilation better

    private static int compileCount = 0;

    private static HashMap<Node, RhoClass> cachedRhoClass = new HashMap<>();
    private static IdentityHashMap<DensityFunctions.Marker, DensityFunctions.Marker> markerCache = new IdentityHashMap<>();

    public static synchronized DensityFunction compile(DensityFunction function) {
        return compile("", function);
    }

    public static synchronized DensityFunction compile(String suffix, DensityFunction function) {
        compileCount++;
        isCompilingCurrently = true;
        // TODO: include dimension
        currentName = suffix;

        List<Object> data = new ArrayList<>();

        currentStatus = "Parse";
        Node node = McToAst.convertToAst(function, data);

        DotExporter.toDotFile(node, "Initial");

        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS
                | ClassWriter.COMPUTE_FRAMES
        );
        ClassVisitor visitor = writer;//new CheckClassAdapter(writer); //

        String name = ("RhoCompiled_" + compileCount) + (suffix.isEmpty() ? "" : "_") + suffix;

        visitor.visit(61, Opcodes.ACC_PUBLIC, name, null, ClassRefs.OBJECT, new String[]{ClassRefs.RHO_CLASS});

        // FIXME: cleanup

        String desc = ClassRefs.methodDescriptor(ClassRefs.DOUBLE, RemappingClassRefs.CLASS_FUNCTION_CONTEXT.get());

        MethodVisitor method =
//                new FlowAnalysisAdapter(
//                        name, Opcodes.ACC_PUBLIC, "compute", "(Lnet/minecraft/world/level/levelgen/DensityFunction$FunctionContext;)D",
                new AnalyzerAdapter(
                        name, Opcodes.ACC_PUBLIC, "compute", desc,
                        visitor.visitMethod(Opcodes.ACC_PUBLIC, "compute", desc, null, null)
//                )
    );

        CodegenContext ctx = new CodegenContext(name, visitor, method);

        node = RunOptoPasses.optimizeAst(ctx, node, true);

        // shortcut for constant functions
        {
            Node actualNode = ((ReturnNode) node).body();
            if (actualNode instanceof ConstNode constNode) {
                isCompilingCurrently = false;
                return DensityFunctions.constant(constNode.value());
            } else if (actualNode instanceof InvokeNode invokeNode) {
                // delegate node: new InvokeNode(INVOKEINTERFACE, RemappingClassRefs.CLASS_DENSITY_FUNCTION.get(), RemappingClassRefs.METHOD_DENSITY_FUNC_COMPUTE.get(),
                //                ClassRefs.methodDescriptor(ClassRefs.DOUBLE, RemappingClassRefs.CLASS_FUNCTION_CONTEXT.get()),
                //                getfield, new VarReferenceNode(new Var(1), ALOAD))
                if (invokeNode.type() == Opcodes.INVOKEINTERFACE && invokeNode.clazz().equals(RemappingClassRefs.CLASS_DENSITY_FUNCTION.get()) && invokeNode.name().equals(RemappingClassRefs.METHOD_DENSITY_FUNC_COMPUTE.get())) {
                    final Node receiver = invokeNode.args()[0];
                    if (receiver instanceof GetFieldNode getFieldNode) {
                        for (Pair<CodegenContext.MinSelfFieldRef, Integer> ctorRef : ctx.ctorRefs) {
                            if (ctorRef.getFirst().name().equals(getFieldNode.name())) {
                                isCompilingCurrently = false;
                                return (DensityFunction) data.get(ctorRef.getSecond());
                            }
                        }
                    }
                }
            }
        }

        // cache codegen
        if (cachedRhoClass.containsKey(node)) {
            isCompilingCurrently = false;
            return new RhoDensityFunction(cachedRhoClass.get(node).makeNew(data), function.minValue(), function.maxValue());
        }

        Label start = new Label();
        Label end = new Label();

        method.visitCode();

        method.visitLabel(start);

        currentStatus = "Write Code";
        node.codegen(ctx, method);

        method.visitLabel(end);

        ctx.applyFieldGens();
        visitor.visitField(Opcodes.ACC_PRIVATE, "list", ClassRefs.descriptor(ClassRefs.LIST), null, null);

        ctx.applyLocals(start, end);

        method.visitMaxs(0, 0);

        method.visitEnd();

        currentStatus = "Finalize";

        buildConstructor(ctx, name, visitor.visitMethod(Opcodes.ACC_PUBLIC, "<init>", ClassRefs.methodDescriptor(ClassRefs.VOID, ClassRefs.LIST), null, null));

        buildInitMethod(ctx, name, visitor.visitMethod(Opcodes.ACC_PUBLIC, "init", ClassRefs.methodDescriptor(ClassRefs.VOID, RemappingClassRefs.CLASS_CHUNKPOS.get()), null, null));
        buildGetArgs(ctx, name, visitor.visitMethod(Opcodes.ACC_PUBLIC, "getArgs", ClassRefs.methodDescriptor(ClassRefs.LIST), null, null));
        buildMakeNew(ctx, name, visitor.visitMethod(Opcodes.ACC_PUBLIC, "makeNew", ClassRefs.methodDescriptor(ClassRefs.RHO_CLASS, ClassRefs.LIST), null, null));

        visitor.visitEnd();

        currentStatus = "Load";

        // TODO: assign fields properly

        byte[] bytes = writer.toByteArray();

        if (DUMP_DEBUG_DATA) {
            File file = Paths.get(".", "compiled", name + ".class").toFile();
            file.getParentFile().mkdirs();
            try (FileOutputStream fos = new FileOutputStream(file)) {
                fos.write(bytes);
                System.out.println("!!");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (COMPILE_RECURSIVELY) {
            for (int i = 0; i < data.size(); i++) {
                Object obj = data.get(i);

                CubicSpline spline = compileSpline(name, obj);

                if (spline != null) {
                    data.set(i, spline);
                }

                if (obj instanceof DensityFunctions.Marker marker) {
                    data.set(i, new DensityFunctions.Marker(marker.type(), compile(name + "_Marker" + i, marker.wrapped())));
                }

                if (obj instanceof FlatCache2.Noop flatCache2) {
                    data.set(i, new FlatCache2.Noop(flatCache2.hashCode(), compile(name + "_FlatCache2_" + i, flatCache2.getNoiseFiller())));
                }
            }
        }

        Object o;
        try {
            Class<?> compiled = defineClass(name, bytes);
            o = compiled.getConstructor(List.class).newInstance(data);
            System.out.println(o);
//            MethodType mt = MethodType.methodType(double.class, DensityFunction.FunctionContext.class);
//            handle = lookup.findVirtual(compiled, "compute", mt);
        } catch (ClassNotFoundException | InvocationTargetException | InstantiationException | IllegalAccessException |
                 NoSuchMethodException e) {
            throw new RuntimeException(e);
        }

        isCompilingCurrently = false;

        // Debugging to dump a single density function at a time
//        if (true) throw new RuntimeException();

        final RhoClass rhoClass = (RhoClass) o;
        cachedRhoClass.put(node, rhoClass);
        return new RhoDensityFunction(rhoClass, function.minValue(), function.maxValue());
    }

    private static CubicSpline compileSpline(String name, Object obj) {
        if (obj instanceof CubicSpline.Multipoint multipoint) {
            ToFloatFunction coordinate = multipoint.coordinate();

            float[] locations = multipoint.locations();
            // List of cubic splines
            List values = multipoint.values();
            float[] derivatives = multipoint.derivatives();
            float max = multipoint.maxValue();
            float min = multipoint.minValue();

            boolean didWork = false;
            if (coordinate instanceof DensityFunctions.Spline.Coordinate coord) {
                final DensityFunction function = coord.function().value();
                coordinate = new DensityFunctions.Spline.Coordinate(
                        new Holder.Direct<>(compile(name + "_SplC", coord.function().value()))
                );
                didWork = true;
            }

            values = new ArrayList(values);

            for (int i = 0; i < values.size(); i++) {
                Object value = values.get(i);

                CubicSpline res = compileSpline(name + "_Spl" + i, value);

                if (res != null) {
                    values.set(i, res);
                    didWork = true;
                }
            }

            if (didWork) {
                return new CubicSpline.Multipoint(
                        coordinate,
                        locations,
                        values,
                        derivatives,
                        max,
                        min
                );
            }
        }

        return null;
    }

    private static void buildConstructor(CodegenContext ctx, String name, MethodVisitor init) {
        Label start2 = new Label();
        Label end2 = new Label();
        init.visitCode();
        init.visitLabel(start2);

        // super()
        init.visitVarInsn(Opcodes.ALOAD, 0);
        init.visitMethodInsn(Opcodes.INVOKESPECIAL, ClassRefs.OBJECT, "<init>", ClassRefs.methodDescriptor(ClassRefs.VOID), false);

        for (Pair<CodegenContext.MinSelfFieldRef, Integer> ref : ctx.ctorRefs) {
            init.visitVarInsn(Opcodes.ALOAD, 0);
            init.visitVarInsn(Opcodes.ALOAD, 1);
            init.visitLdcInsn(ref.getSecond());
            init.visitMethodInsn(Opcodes.INVOKEINTERFACE, ClassRefs.LIST, "get", ClassRefs.methodDescriptor(ClassRefs.OBJECT, ClassRefs.INT), true);
            init.visitTypeInsn(Opcodes.CHECKCAST, ref.getFirst().desc().substring(1).substring(0, ref.getFirst().desc().indexOf(";") - 1));
            init.visitFieldInsn(Opcodes.PUTFIELD, name, ref.getFirst().name(), ref.getFirst().desc());
        }

        init.visitVarInsn(Opcodes.ALOAD, 0);
        init.visitVarInsn(Opcodes.ALOAD, 1);
        init.visitFieldInsn(Opcodes.PUTFIELD, name, "list", ClassRefs.descriptor(ClassRefs.LIST));

        init.visitInsn(Opcodes.RETURN);

        init.visitLabel(end2);
        init.visitLocalVariable("this", ClassRefs.descriptor(name), null, start2, end2, 0);
        init.visitLocalVariable("ctx", ClassRefs.descriptor(ClassRefs.LIST), null, start2, end2, 1);
        init.visitMaxs(0, 0);
    }

    private static void buildInitMethod(CodegenContext ctx, String className, MethodVisitor init) {
        Label start = new Label();
        Label end = new Label();

        init.visitCode();
        init.visitLabel(start);

        for (Pair<CodegenContext.MinSelfFieldRef, Integer> ref : ctx.ctorRefs) {
            if (ref.getFirst().desc().equals(CacheFlatNode.CACHE_DESC)) {
                init.visitVarInsn(Opcodes.ALOAD, 0);
                init.visitFieldInsn(Opcodes.GETFIELD, className, ref.getFirst().name(), ref.getFirst().desc());
                init.visitVarInsn(Opcodes.ALOAD, 1);
                init.visitMethodInsn(Opcodes.INVOKEINTERFACE, ClassRefs.FLAT_CACHE_2, "init", ClassRefs.methodDescriptor(ClassRefs.VOID, RemappingClassRefs.CLASS_CHUNKPOS.get()), true);
            }
        }

        init.visitInsn(Opcodes.RETURN);

        init.visitLabel(end);
        init.visitLocalVariable("this", ClassRefs.descriptor(className), null, start, end, 0);
        init.visitLocalVariable("ctx", RemappingClassRefs.CLASS_CHUNKPOS.getAsDescriptor(), null, start, end, 1);
        init.visitMaxs(0, 0);
    }

    private static void buildGetArgs(CodegenContext ctx, String className, MethodVisitor init) {
        Label start = new Label();
        Label end = new Label();

        init.visitCode();
        init.visitLabel(start);

        init.visitVarInsn(Opcodes.ALOAD, 0);
        init.visitFieldInsn(Opcodes.GETFIELD, className, "list", ClassRefs.descriptor(ClassRefs.LIST));
        init.visitInsn(Opcodes.ARETURN);

        init.visitLabel(end);

        init.visitLocalVariable("this", ClassRefs.descriptor(className), null, start, end, 0);
        init.visitMaxs(0, 0);
    }

    private static void buildMakeNew(CodegenContext ctx, String className, MethodVisitor init) {
        Label start = new Label();
        Label end = new Label();

        init.visitCode();
        init.visitLabel(start);

        init.visitTypeInsn(Opcodes.NEW, className);
        init.visitInsn(Opcodes.DUP);
        init.visitVarInsn(Opcodes.ALOAD, 1);
        init.visitMethodInsn(Opcodes.INVOKESPECIAL, className, "<init>", ClassRefs.methodDescriptor(ClassRefs.VOID, ClassRefs.LIST), false);
        init.visitInsn(Opcodes.ARETURN);

        init.visitLabel(end);

        init.visitLocalVariable("this", ClassRefs.descriptor(className), null, start, end, 0);
        init.visitLocalVariable("ctx", ClassRefs.descriptor(ClassRefs.LIST), null, start, end, 1);
        init.visitMaxs(0, 0);
    }

    private static Class<?> defineClass(String className, byte[] bytes) throws ClassNotFoundException {
        ClassLoader classLoader = new ClassLoader(RhoCompiler.class.getClassLoader()) {
            @Override
            public Class<?> loadClass(String name) throws ClassNotFoundException {
                if (name.equals(className)) {
                    return super.defineClass(name, bytes, 0, bytes.length);
                }

                return super.loadClass(name);
            }
        };

        return classLoader.loadClass(className);
    }
}
