package supercoder79.rho.ast.low;

import org.objectweb.asm.MethodVisitor;
import supercoder79.rho.ast.Node;
import supercoder79.rho.gen.CodegenContext;

import java.util.List;

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
}
