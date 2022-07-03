package supercoder79.rho.ast.high;

import supercoder79.rho.ast.Node;
import supercoder79.rho.ast.Var;
import supercoder79.rho.ast.common.ConstNode;
import supercoder79.rho.ast.common.MulNode;
import supercoder79.rho.ast.low.SequenceNode;
import supercoder79.rho.ast.low.SubNode;
import supercoder79.rho.ast.low.VarAssignNode;
import supercoder79.rho.ast.low.VarReferenceNode;
import supercoder79.rho.gen.CodegenContext;

import java.util.List;

public record SqueezeNode(Node inner) implements Node {
    @Override
    public Node lower(CodegenContext ctx) {
        Var var = ctx.getNextVar();
        ctx.addLocalVar(var);
        
        // clamp lower will lower inner
        Node vardef = new VarAssignNode(var, new ClampNode(inner, -1, 1).lower(ctx));

        // Sub(Div(e, 2), Div(Mul(e, Mul(e, e)), 24))

        Node sub = new SubNode(
                new MulNode(
                        new VarReferenceNode(var),
                        new ConstNode(0.5)
                ),
                new MulNode(
                        new MulNode(
                                new VarReferenceNode(var),
                                new MulNode(
                                        new VarReferenceNode(var),
                                        new VarReferenceNode(var)
                                )
                        ),
                        new ConstNode(1 / 24.0)
                )
        ).lower(ctx);

        return new SequenceNode(vardef, sub);
    }

    @Override
    public Node replaceNode(Node old, Node newNode) {
        if (old == inner) {
            return new SqueezeNode(newNode);
        }

        return this;
    }

    @Override
    public List<Node> children() {
        return List.of(inner);
    }
}
