package supercoder79.rho.ast.high.noise;

import supercoder79.rho.ClassRefs;
import supercoder79.rho.RemappingClassRefs;
import supercoder79.rho.ast.Node;
import supercoder79.rho.ast.Var;
import supercoder79.rho.ast.common.MulNode;
import supercoder79.rho.ast.high.AbsNode;
import supercoder79.rho.ast.low.*;
import supercoder79.rho.gen.CodegenContext;

import java.util.List;

public record WeirdSamplerNode(Node inner, int noiseIdx, int doubleFuncIdx) implements Node {

    public static final String DOUBLE_FUNC = "it/unimi/dsi/fastutil/doubles/Double2DoubleFunction";

    @Override
    public Node lower(CodegenContext ctx) {
        String id = ctx.getNextFieldId("scaledsampler");
        ctx.addFieldGen(cl -> cl.visitField(ACC_PRIVATE, id, ClassRefs.descriptor(DOUBLE_FUNC), null, null));
        ctx.addCtorFieldRef(new CodegenContext.MinSelfFieldRef(id, ClassRefs.descriptor(DOUBLE_FUNC)), doubleFuncIdx);

        Node getfield = new GetFieldNode(false, ctx.contextName(), id, ClassRefs.descriptor(DOUBLE_FUNC));

        Var var = ctx.getNextVar();
        VarAssignNode assign = new VarAssignNode(var, new InvokeNode(INVOKEINTERFACE, DOUBLE_FUNC, "get", "(D)D",
                getfield, inner.lower(ctx)));
        // Assign(e, Invoke(doubleFunc, inner));
        // Mul(e, Abs(Invoke(noise, x / e, y / e, z / e)))

        return new SequenceNode(assign, new MulNode(new VarReferenceNode(var), new AbsNode(new NoiseDivNode(noiseIdx, var)).lower(ctx)));
    }

    @Override
    public Node replaceNode(Node old, Node newNode) {
        if (old == inner) {
            return new WeirdSamplerNode(newNode, noiseIdx, doubleFuncIdx);
        }

        return this;
    }

    @Override
    public List<Node> children() {
        return List.of(inner);
    }
}
