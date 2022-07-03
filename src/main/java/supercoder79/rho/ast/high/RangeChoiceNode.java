package supercoder79.rho.ast.high;

import org.objectweb.asm.MethodVisitor;
import supercoder79.rho.ast.Node;
import supercoder79.rho.gen.CodegenContext;

import java.util.List;

public record RangeChoiceNode(Node input, double minInclusive, double maxExclusive, Node ifTrue, Node ifFalse) implements Node {
    @Override
    public Node lower(CodegenContext ctx) {
        throw new RuntimeException();
    }

    @Override
    public Node replaceNode(Node old, Node newNode) {
        if (old == input) {
            return new RangeChoiceNode(newNode, minInclusive, maxExclusive, ifTrue, ifFalse);
        } else if (old == ifTrue) {
            return new RangeChoiceNode(input, minInclusive, maxExclusive, newNode, ifFalse);
        } else if (old == ifFalse) {
            return new RangeChoiceNode(input, minInclusive, maxExclusive, ifTrue, newNode);
        }

        return this;
    }

    @Override
    public List<Node> children() {
        return List.of(input, ifTrue, ifFalse);
    }
}
