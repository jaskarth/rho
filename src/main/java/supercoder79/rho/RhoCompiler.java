package supercoder79.rho;

import com.mojang.datafixers.util.Pair;
import net.minecraft.world.level.levelgen.DensityFunction;
import org.objectweb.asm.*;
import org.objectweb.asm.commons.AnalyzerAdapter;
import org.objectweb.asm.util.CheckClassAdapter;
import supercoder79.rho.ast.McToAst;
import supercoder79.rho.ast.Node;
import supercoder79.rho.ast.high.complex.CacheFlatNode;
import supercoder79.rho.gen.CodegenContext;
import supercoder79.rho.gen.DotExporter;
import supercoder79.rho.opto.RunOptoPasses;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public final class RhoCompiler {
    public static boolean DO_COMPILE = true;

    // TODO: handle on-demand compilation better

    // TODO: some splines use density functions? those need to also be compiled

    private static int compileCount = 0;

    public static synchronized RhoClass compile(DensityFunction function) {
        return compile("", function);
    }

    public static synchronized RhoClass compile(String suffix, DensityFunction function) {
        compileCount++;
        List<Object> data = new ArrayList<>();

        Node node = McToAst.convertToAst(function, data);
        DotExporter.toDotFile(node, "Initial");

        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS
                | ClassWriter.COMPUTE_FRAMES
        );
        ClassVisitor visitor = writer;//new CheckClassAdapter(writer); //

        String name = ("RhoCompiled_" + compileCount) + (suffix.isEmpty() ? "" : "_") + suffix;

        visitor.visit(61, Opcodes.ACC_PUBLIC, name, null, ClassRefs.OBJECT, new String[]{ClassRefs.RHO_CLASS});

        // FIXME: cleanup

        MethodVisitor method =
                new AnalyzerAdapter(
                        name, Opcodes.ACC_PUBLIC, "compute", "(Lnet/minecraft/world/level/levelgen/DensityFunction$FunctionContext;)D",
                        visitor.visitMethod(Opcodes.ACC_PUBLIC, "compute", "(Lnet/minecraft/world/level/levelgen/DensityFunction$FunctionContext;)D", null, null)
                );

        CodegenContext ctx = new CodegenContext(name, visitor, method);

        node = RunOptoPasses.optimizeAst(ctx, node, true);

        Label start = new Label();
        Label end = new Label();

        method.visitCode();

        method.visitLabel(start);

        node.codegen(ctx, method);

        method.visitLabel(end);

        ctx.applyFieldGens();
        visitor.visitField(Opcodes.ACC_PRIVATE, "list", ClassRefs.descriptor(ClassRefs.LIST), null, null);

        ctx.applyLocals(start, end);

        method.visitMaxs(0, 0);

        buildConstructor(ctx, name, visitor.visitMethod(Opcodes.ACC_PUBLIC, "<init>", ClassRefs.methodDescriptor(ClassRefs.VOID, ClassRefs.LIST), null, null));

        buildInitMethod(ctx, name, visitor.visitMethod(Opcodes.ACC_PUBLIC, "init", "(Lnet/minecraft/world/level/ChunkPos;)V", null, null));
        buildGetArgs(ctx, name, visitor.visitMethod(Opcodes.ACC_PUBLIC, "getArgs", ClassRefs.methodDescriptor(ClassRefs.LIST), null, null));
        buildMakeNew(ctx, name, visitor.visitMethod(Opcodes.ACC_PUBLIC, "makeNew", ClassRefs.methodDescriptor(ClassRefs.RHO_CLASS, ClassRefs.LIST), null, null));

        visitor.visitEnd();

        // TODO: assign fields properly

        byte[] bytes = writer.toByteArray();

        File file = Paths.get(".", "compiled", name + ".class").toFile();
        file.getParentFile().mkdirs();
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(bytes);
            System.out.println("!!");
        } catch (Exception e) {
            e.printStackTrace();
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

        return (RhoClass)o;
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
                init.visitMethodInsn(Opcodes.INVOKEINTERFACE, CacheFlatNode.CACHE_NAME, "init", "(Lnet/minecraft/world/level/ChunkPos;)V", true);
            }
        }

        init.visitInsn(Opcodes.RETURN);

        init.visitLabel(end);
        init.visitLocalVariable("this", ClassRefs.descriptor(className), null, start, end, 0);
        init.visitLocalVariable("ctx", "Lnet/minecraft/world/level/ChunkPos;", null, start, end, 1);
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
