package supercoder79.rho.ast.low;

import org.objectweb.asm.MethodVisitor;
import supercoder79.rho.ast.Node;
import supercoder79.rho.gen.CodegenContext;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public record InvokeNode(int type, String clazz, String name, String descriptor, Node... args) implements Node {

    @Override
    public boolean isLow() {
        return true;
    }

    @Override
    public List<Node> children() {
        return List.of(args);
    }

    @Override
    public void codegen(CodegenContext ctx, MethodVisitor visitor) {
        for (Node arg : args) {
            arg.codegen(ctx, visitor);
        }

        visitor.visitMethodInsn(type, clazz, name, descriptor, type == INVOKEINTERFACE);
    }

    @Override
    public Node replaceNode(Node old, Node newNode) {
        for (int i = 0; i < args.length; i++) {
            Node nd = args[i];
            if (nd == old) {
                Node[] clone = args.clone();
                clone[i] = newNode;
                return new InvokeNode(type, clazz, name, descriptor, clone);
            }
        }

        return this;
    }

    @Override
    public String getDotNodeLabel() {
        return "" + name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InvokeNode that = (InvokeNode) o;
        return type == that.type && Objects.equals(clazz, that.clazz) && Objects.equals(name, that.name) && Objects.equals(descriptor, that.descriptor) && Arrays.equals(args, that.args);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(type, clazz, name, descriptor);
        result = 31 * result + Arrays.hashCode(args);
        return result;
    }
}
