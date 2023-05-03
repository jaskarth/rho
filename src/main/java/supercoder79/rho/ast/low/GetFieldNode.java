package supercoder79.rho.ast.low;


import org.objectweb.asm.MethodVisitor;
import supercoder79.rho.ast.Node;
import supercoder79.rho.gen.CodegenContext;

import java.util.List;

public record GetFieldNode(boolean isStatic, String owner, String name, String descriptor) implements Node {

    @Override
    public boolean isLow() {
        return true;
    }

    @Override
    public void codegen(CodegenContext ctx, MethodVisitor visitor) {
        if (!isStatic) {
            visitor.visitVarInsn(ALOAD, 0);
        }

        visitor.visitFieldInsn(isStatic ? GETSTATIC : GETFIELD, owner != null ? owner : ctx.contextName(), name, descriptor);
    }

    @Override
    public Node replaceNode(Node old, Node newNode) {
        return this;
    }

    @Override
    public List<Node> children() {
        return List.of();
    }
}
