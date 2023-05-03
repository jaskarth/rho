package supercoder79.rho.ast.high;

import supercoder79.rho.ClassRefs;
import supercoder79.rho.RemappingClassRefs;
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
        ctx.addFieldGen(cl -> cl.visitField(ACC_PRIVATE, id, RemappingClassRefs.CLASS_CUBIC_SPLINE.getAsDescriptor(), null, null));
        ctx.addCtorFieldRef(new CodegenContext.MinSelfFieldRef(id, RemappingClassRefs.CLASS_CUBIC_SPLINE.getAsDescriptor()), idx);

        Node getfield = new GetFieldNode(false, null, id, RemappingClassRefs.CLASS_CUBIC_SPLINE.getAsDescriptor());
        Node newNode = new NewNode(RemappingClassRefs.CLASS_SPLINEPOINT.get());
        Node dup = new RawInsnNode(DUP);
        Node varReference = new VarReferenceNode(new Var(1), ALOAD);
        Node init = new InvokeNode(INVOKESPECIAL, RemappingClassRefs.CLASS_SPLINEPOINT.get(), "<init>",
                ClassRefs.methodDescriptor(ClassRefs.VOID, RemappingClassRefs.CLASS_FUNCTION_CONTEXT.get()), varReference);

        Node apply = new InvokeNode(INVOKEINTERFACE, RemappingClassRefs.CLASS_CUBIC_SPLINE.get(), RemappingClassRefs.METHOD_SPLINE_APPLY.get(), "(Ljava/lang/Object;)F");

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
