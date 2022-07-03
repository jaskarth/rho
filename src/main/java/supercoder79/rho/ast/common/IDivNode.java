package supercoder79.rho.ast.common;

import org.objectweb.asm.MethodVisitor;
import supercoder79.rho.ast.Node;
import supercoder79.rho.gen.CodegenContext;

import java.util.List;

public record IDivNode(boolean lowered, Node left, Node right) implements Node {
    public IDivNode(Node left, Node right) {
        this(false, left, right);
    }

    @Override
    public Node lower(CodegenContext ctx) {
        if (!isLow()) {
            return new IDivNode(true, left.lower(ctx), right.lower(ctx));
        }

        return this;
    }

    @Override
    public boolean isLow() {
        return this.lowered;
    }

    @Override
    public void codegen(CodegenContext ctx, MethodVisitor visitor) {
        if (!isLow()) {
//            throw new IllegalStateException("Dual use node never lowered!");
        }

        left.codegen(ctx, visitor);
        right.codegen(ctx, visitor);

        visitor.visitInsn(IDIV);
    }

    @Override
    public Node replaceNode(Node old, Node newNode) {
        if (old == left) {
            return new IDivNode(lowered, newNode, right);
        } else if (old == right) {
            return new IDivNode(lowered, left, newNode);
        }

        return this;
    }

    @Override
    public List<Node> children() {
        return List.of(left, right);
    }
}
