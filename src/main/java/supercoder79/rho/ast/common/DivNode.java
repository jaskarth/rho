package supercoder79.rho.ast.common;

import org.objectweb.asm.MethodVisitor;
import supercoder79.rho.ast.Node;
import supercoder79.rho.gen.CodegenContext;

import java.util.List;

public record DivNode(boolean lowered, Node left, Node right) implements Node {
    public DivNode(Node left, Node right) {
        this(false, left, right);
    }

    @Override
    public Node lower(CodegenContext ctx) {
        if (!isLow()) {
            return new DivNode(true, left.lower(ctx), right.lower(ctx));
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

        visitor.visitInsn(DDIV);
    }

    @Override
    public Node replaceNode(Node old, Node newNode) {
        if (old == left) {
            return new DivNode(lowered, newNode, right);
        } else if (old == right) {
            return new DivNode(lowered, left, newNode);
        }

        return this;
    }

    @Override
    public List<Node> children() {
        return List.of(left, right);
    }
}
