package supercoder79.rho.ast.high;

import supercoder79.rho.ast.Node;
import supercoder79.rho.ast.Var;
import supercoder79.rho.ast.low.*;
import supercoder79.rho.gen.CodegenContext;

import java.util.List;

public record SplineNode(int idx) implements Node {
    @Override
    public Node lower(CodegenContext ctx) {

        // FIXME: make this better
        String id = ctx.getNextFieldId("spline");
        ctx.addFieldGen(cl -> cl.visitField(ACC_PRIVATE, id, "Lnet/minecraft/util/CubicSpline;", null, null));
        ctx.addCtorFieldRef(new CodegenContext.MinSelfFieldRef(id, "Lnet/minecraft/util/CubicSpline;"), idx);

        Node getfield = new GetFieldNode(false, ctx.contextName(), id, "Lnet/minecraft/util/CubicSpline;");
        Node newNode = new NewNode("net/minecraft/world/level/levelgen/DensityFunctions$Spline$Point");
        Node dup = new RawInsnNode(DUP);
        Node varReference = new VarReferenceNode(new Var(1), ALOAD);
        Node init = new InvokeNode(INVOKESPECIAL, "net/minecraft/world/level/levelgen/DensityFunctions$Spline$Point", "<init>",
                "(Lnet/minecraft/world/level/levelgen/DensityFunction$FunctionContext;)V", varReference);

        Node apply = new InvokeNode(INVOKEINTERFACE, "net/minecraft/util/CubicSpline", "apply", "(Ljava/lang/Object;)F");

//        throw new RuntimeException();

        return new SequenceNode(getfield, newNode, dup, init, apply, new RawInsnNode(F2D));
    }

    @Override
    public Node replaceNode(Node old, Node newNode) {
        return this;
    }

    @Override
    public List<Node> children() {
        return List.of();
    }
}
