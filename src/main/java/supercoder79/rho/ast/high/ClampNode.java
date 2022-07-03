package supercoder79.rho.ast.high;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import supercoder79.rho.ast.Node;
import supercoder79.rho.ast.Var;
import supercoder79.rho.ast.common.ConstNode;
import supercoder79.rho.ast.low.*;
import supercoder79.rho.gen.CodegenContext;

import java.util.List;

public record ClampNode(Node inner, double min, double max) implements Node {
    @Override
    public Node lower(CodegenContext ctx) {
        Var var = ctx.getNextVar();
        ctx.addLocalVar(var);

        Node vardef = new VarAssignNode(var, inner.lower(ctx));
        Node val = new VarReferenceNode(var);
        Node minConst = new ConstNode(min);
        Node maxConst = new ConstNode(max);

        return new IfElseNode(
                new SequenceNode(vardef, val, minConst, new RawInsnNode(DCMPL)),
                Opcodes.IFLE,
                new ConstNode(min),
                new IfElseNode(
                        new SequenceNode(new VarReferenceNode(var), maxConst, new RawInsnNode(DCMPG)),
                        Opcodes.IFGE,
                        new ConstNode(max),
                        new VarReferenceNode(var)
                )
        );
    }

    @Override
    public Node replaceNode(Node old, Node newNode) {
        if (old == inner) {
            return new ClampNode(newNode, min, max);
        }

        return this;
    }

    @Override
    public List<Node> children() {
        return List.of(inner);
    }
}
