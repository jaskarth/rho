package supercoder79.rho.ast.high;

import supercoder79.rho.ast.Node;
import supercoder79.rho.ast.Var;
import supercoder79.rho.ast.common.MulNode;
import supercoder79.rho.ast.low.SequenceNode;
import supercoder79.rho.ast.low.VarAssignNode;
import supercoder79.rho.ast.low.VarReferenceNode;
import supercoder79.rho.gen.CodegenContext;

import java.util.List;

public record SquareNode(Node inner) implements Node {
    @Override
    public Node lower(CodegenContext ctx) {
        Var var = ctx.getNextVar();
        ctx.addLocalVar(var);

        // Lower into
        // double x = ...
        // x * x
        //
        // Avoid recalculating x
        Node defVar = new VarAssignNode(var, inner);
        Node mul = new MulNode(new VarReferenceNode(var), new VarReferenceNode(var));

        return new SequenceNode(defVar, mul);
    }

    @Override
    public Node replaceNode(Node old, Node newNode) {
        if (old == inner) {
            return new SquareNode(newNode);
        }

        return this;
    }

    @Override
    public List<Node> children() {
        return List.of(inner);
    }
}
