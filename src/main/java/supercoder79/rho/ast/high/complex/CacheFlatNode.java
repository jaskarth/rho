package supercoder79.rho.ast.high.complex;

import supercoder79.rho.ast.Node;
import supercoder79.rho.gen.CodegenContext;

import java.util.List;

public record CacheFlatNode(Node node) implements Node {
    @Override
    public Node lower(CodegenContext ctx) {
        // FIXME: implement
        return node.lower(ctx);
//        throw new RuntimeException();
    }

    @Override
    public Node replaceNode(Node old, Node newNode) {
        if (old == node) {
            return new CacheFlatNode(newNode);
        }

        return this;
    }

    @Override
    public List<Node> children() {
        return List.of(node);
    }
}
