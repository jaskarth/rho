package supercoder79.rho.ast.high;

import supercoder79.rho.ClassRefs;
import supercoder79.rho.ast.Node;
import supercoder79.rho.ast.low.InvokeNode;
import supercoder79.rho.gen.CodegenContext;

import java.util.List;

public record MaxNode(Node left, Node right) implements Node {
    @Override
    public Node lower(CodegenContext ctx) {
        return new InvokeNode(INVOKESTATIC, ClassRefs.MATH, "max", ClassRefs.methodDescriptor(ClassRefs.DOUBLE, ClassRefs.DOUBLE), left.lower(ctx), right.lower(ctx));
    }

    @Override
    public Node replaceNode(Node old, Node newNode) {
        if (old == left) {
            return new MaxNode(newNode, right);
        } else if (old == right) {
            return new MaxNode(left, newNode);
        }

        return this;
    }

    @Override
    public List<Node> children() {
        return List.of(left, right);
    }
}
