package supercoder79.rho.ast.common;

import org.objectweb.asm.MethodVisitor;
import supercoder79.rho.ast.Node;
import supercoder79.rho.gen.CodegenContext;

import java.util.List;

public record MulNode(boolean lowered, Node left, Node right) implements Node {
    public MulNode(Node left, Node right) {
        this(false, left, right);
    }

    @Override
    public Node lower(CodegenContext ctx) {
        if (!isLow()) {
            return new MulNode(true, left.lower(ctx), right.lower(ctx));
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

        visitor.visitInsn(DMUL);
    }

    @Override
    public Node replaceNode(Node old, Node newNode) {
        if (old == left) {
            return new MulNode(lowered, newNode, right);
        } else if (old == right) {
            return new MulNode(lowered, left, newNode);
        }

        return this;
    }

    public Node swap() {
        return new MulNode(lowered, right, left);
    }

    @Override
    public List<Node> children() {
        return List.of(left, right);
    }
}
