package supercoder79.rho.gen;

import com.mojang.datafixers.util.Pair;
import org.objectweb.asm.*;
import supercoder79.rho.ast.Node;
import supercoder79.rho.ast.Var;
import supercoder79.rho.ast.common.ReturnNode;

import java.util.*;
import java.util.function.Consumer;

public class CodegenContext implements Opcodes {
    private final String name;
    private final ClassVisitor clazz;
    private MethodVisitor method;
    private final List<Consumer<ClassVisitor>> fieldGens = new ArrayList<>();
    private final Set<Pair<Var, String>> vars = new HashSet<>();
    private int varIndex = 2;
    private int fieldIndex = 2;
    public final List<Pair<MinSelfFieldRef, Integer>> ctorRefs = new ArrayList<>();
    private final Deque<Pair<Node, String>> methodStack = new LinkedList<>();
    private int methodIndex = 0;

    public CodegenContext(String name, ClassVisitor clazz, MethodVisitor method) {
        this.name = name;
        this.clazz = clazz;
        this.method = method;
    }

    public void generateNextMethods() {
        // FIXME: needs a deque of the whole context
        // Move context into inner class?

        
        while (!this.methodStack.isEmpty()) {
            Pair<Node, String> pair = this.methodStack.pop();
            MethodVisitor methodVisitor = clazz.visitMethod(ACC_PUBLIC, pair.getSecond(), "(Lnet/minecraft/world/level/levelgen/DensityFunction$FunctionContext;)D", null, null);
            this.method = methodVisitor;

            // TODO: need to call node optimizer
            pair.getFirst().codegen(this, methodVisitor);
        }
    }

    public void addFieldGen(Consumer<ClassVisitor> gen) {
        fieldGens.add(gen);
    }

    public void addCtorFieldRef(MinSelfFieldRef ref, int idx) {
        ctorRefs.add(Pair.of(ref, idx));
    }

    public record MinSelfFieldRef(String name, String desc) {}

    public void applyFieldGens() {
        for (Consumer<ClassVisitor> f : fieldGens) {
            f.accept(this.clazz);
        }
    }

    public void applyLocals(Label start, Label end) {
        this.method.visitLocalVariable("this", "L" + contextName() + ";", null, start, end, 0);
        this.method.visitLocalVariable("ctx", "Lnet/minecraft/world/level/levelgen/DensityFunction$FunctionContext;", null, start, end, 1);

        for (Pair<Var, String> var : this.vars) {
            this.method.visitLocalVariable("rho" + var.getFirst().index(), var.getSecond(), null, start, end, var.getFirst().index());
        }
    }

    public void addLocalVar(Var var) {
        addLocalVar(var, "D");
    }

    public void addLocalVar(Var var, String type) {
        vars.add(Pair.of(var, type));
    }

    // , -> int/double
    public void referenceContextBlockXYZ(Type type, boolean asDouble) {
        method.visitVarInsn(ALOAD, 1);

        // TODO: remap
        if (type == Type.X) {
            method.visitMethodInsn(INVOKEINTERFACE, "net/minecraft/world/level/levelgen/DensityFunction$FunctionContext", "blockX", "()I", true);
        } else if (type == Type.Y) {
            method.visitMethodInsn(INVOKEINTERFACE, "net/minecraft/world/level/levelgen/DensityFunction$FunctionContext", "blockY", "()I", true);
        } else {
            method.visitMethodInsn(INVOKEINTERFACE, "net/minecraft/world/level/levelgen/DensityFunction$FunctionContext", "blockZ", "()I", true);
        }

        if (asDouble) {
            method.visitInsn(I2D);
        }
    }

    // Return: method name
    public String forkMethodwise(Node body) {
        String methodName = "method" + ++methodIndex;
        methodStack.add(Pair.of(new ReturnNode(body), methodName));

        return methodName;
    }

    public String contextName() {
        return this.name;
    }

    public String getNextFieldId(String prefix) {
        return prefix + (++fieldIndex);
    }

    public enum Type {
        X, Y, Z
    }

    public Var getNextVar() {
        return getNextVar(true);
    }

    public Var getNextVar(boolean needs2Slots) {
        int temp = varIndex;
        varIndex = varIndex + (needs2Slots ? 2 : 1);
        return new Var(temp);
    }
}
