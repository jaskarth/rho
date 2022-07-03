package supercoder79.rho.ast.high;

import org.objectweb.asm.Opcodes;
import supercoder79.rho.ast.Node;
import supercoder79.rho.ast.Var;
import supercoder79.rho.ast.common.ConstNode;
import supercoder79.rho.ast.common.MulNode;
import supercoder79.rho.ast.low.*;
import supercoder79.rho.gen.CodegenContext;

import java.util.List;

public record BelowZeroLowNode(Node inner, double value) implements Node {
    @Override
    public Node lower(CodegenContext ctx) {
        Var var = ctx.getNextVar();
        ctx.addLocalVar(var);

        Node vardef = new VarAssignNode(var, inner.lower(ctx));
        Node val = new VarReferenceNode(var);
        Node constant = new ConstNode(0);

        // double x = ...
        // if (x < 0) {
        //  x * <value>
        // } else {
        //  x
        // }

        return new IfElseNode(
                new SequenceNode(vardef, val, constant, new RawInsnNode(DCMPL)),
                Opcodes.IFLT,
                new MulNode(new VarReferenceNode(var),new ConstNode(value)),
                new VarReferenceNode(var));
    }

    @Override
    public Node replaceNode(Node old, Node newNode) {
        if (old == inner) {
            return new BelowZeroLowNode(newNode, value);
        }

        return this;
    }

    @Override
    public List<Node> children() {
        return List.of(inner);
    }
}
