package supercoder79.rho.ast.low;

import org.objectweb.asm.MethodVisitor;
import supercoder79.rho.ast.Node;
import supercoder79.rho.gen.CodegenContext;

import java.util.List;

@Deprecated
public record LessThanNode(Node left, Node right) implements Node {
    @Override
    public boolean isLow() {
        return true;
    }

    @Override
    public void codegen(CodegenContext ctx, MethodVisitor visitor) {
        left.codegen(ctx, visitor);
        right.codegen(ctx, visitor);

        visitor.visitInsn(DCMPL);
    }

    @Override
    public Node replaceNode(Node old, Node newNode) {
        if (old == left) {
            return new LessThanNode(newNode, right);
        } else if (old == right) {
            return new LessThanNode(left, newNode);
        }

        return this;
    }

    @Override
    public List<Node> children() {
        return List.of(left, right);
    }
}
