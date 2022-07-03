package supercoder79.rho.ast.high.complex;

import supercoder79.rho.ast.Node;
import supercoder79.rho.gen.CodegenContext;

import java.util.List;

public record InterpolationNode(Node inner) implements Node {
    @Override
    public Node lower(CodegenContext ctx) {
        throw new RuntimeException();
    }

    @Override
    public Node replaceNode(Node old, Node newNode) {
        if (old == inner) {
            return new InterpolationNode(newNode);
        }

        return this;
    }

    @Override
    public List<Node> children() {
        return List.of(inner);
    }
}
