package supercoder79.rho.ast.high;

import org.objectweb.asm.MethodVisitor;
import supercoder79.rho.ast.Node;
import supercoder79.rho.ast.Var;
import supercoder79.rho.ast.common.ConstNode;
import supercoder79.rho.ast.low.*;
import supercoder79.rho.gen.CodegenContext;

import java.util.List;

public record RangeChoiceNode(Node input, double minInclusive, double maxExclusive, Node ifTrue, Node ifFalse) implements Node {
    @Override
    public Node lower(CodegenContext ctx) {
        Var var = ctx.getNextVar();
        ctx.addLocalVar(var);

        Node vardef = new VarAssignNode(var, input.lower(ctx));
        Node content = new IfAndElseNode(
                new SequenceNode(new VarReferenceNode(var), new ConstNode(minInclusive), new RawInsnNode(DCMPL)),
                new SequenceNode(new VarReferenceNode(var), new ConstNode(maxExclusive), new RawInsnNode(DCMPG)),
                IFLT,
                IFGE,
                ifFalse.lower(ctx),
                ifTrue.lower(ctx)

        );


        return new SequenceNode(vardef, content);
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
