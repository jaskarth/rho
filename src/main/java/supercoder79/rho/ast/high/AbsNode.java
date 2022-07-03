package supercoder79.rho.ast.high;

import supercoder79.rho.ast.Node;
import supercoder79.rho.ast.low.InsnNode;
import supercoder79.rho.gen.CodegenContext;

import java.util.List;

public record AbsNode(Node inner) implements Node {
    @Override
    public Node lower(CodegenContext ctx) {
        // Math.abs(node)
        return new InsnNode(INVOKESTATIC, "java/lang/Math", "abs", "(D)D", inner.lower(ctx));
    }

    @Override
    public Node replaceNode(Node old, Node newNode) {
        if (old == inner) {
            return new AbsNode(newNode);
        }

        return this;
    }

    @Override
    public List<Node> children() {
        return List.of(inner);
    }
}
