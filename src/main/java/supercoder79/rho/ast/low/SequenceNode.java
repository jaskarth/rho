package supercoder79.rho.ast.low;

import org.objectweb.asm.MethodVisitor;
import supercoder79.rho.ast.Node;
import supercoder79.rho.gen.CodegenContext;

import java.util.List;

public record SequenceNode(Node... nodes) implements Node {
    @Override
    public Node lower(CodegenContext ctx) {
        throw new IllegalStateException("Impossible to lower a sequence of nodes, as it is a synthetic construct");
    }

    @Override
    public boolean isLow() {
        return true;
    }

    @Override
    public void codegen(CodegenContext ctx, MethodVisitor visitor) {
        for (Node node : nodes) {
            node.codegen(ctx, visitor);
        }
    }

    @Override
    public Node replaceNode(Node old, Node newNode) {
        for (int i = 0; i < nodes.length; i++) {
            Node nd = nodes[i];
            if (nd == old) {
                Node[] clone = nodes.clone();
                clone[i] = newNode;
                return new SequenceNode(clone);
            }
        }

        return this;
    }

    @Override
    public List<Node> children() {
        return List.of(nodes);
    }

    @Override
    public String getDotEdgeLabel(Node child) {
        for (int i = 0; i < nodes.length; i++) {
            Node nd = nodes[i];

            if (nd == child) {
                return "" + i;
            }
        }

        return "";
    }
}
