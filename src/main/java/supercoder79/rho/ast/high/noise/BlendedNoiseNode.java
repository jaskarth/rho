package supercoder79.rho.ast.high.noise;

import supercoder79.rho.ClassRefs;
import supercoder79.rho.RemappingClassRefs;
import supercoder79.rho.ast.Node;
import supercoder79.rho.ast.Var;
import supercoder79.rho.ast.low.GetFieldNode;
import supercoder79.rho.ast.low.InvokeNode;
import supercoder79.rho.ast.low.VarReferenceNode;
import supercoder79.rho.gen.CodegenContext;

import java.util.List;

public record BlendedNoiseNode(int index) implements Node {
    @Override
    public Node lower(CodegenContext ctx) {
        String id = ctx.getNextFieldId("blendednoise");
        ctx.addFieldGen(cl -> cl.visitField(ACC_PRIVATE, id, RemappingClassRefs.CLASS_DENSITY_FUNCTION.getAsDescriptor(), null, null));
        ctx.addCtorFieldRef(new CodegenContext.MinSelfFieldRef(id, RemappingClassRefs.CLASS_DENSITY_FUNCTION.getAsDescriptor()), index);

        Node getfield = new GetFieldNode(false, null, id, RemappingClassRefs.CLASS_DENSITY_FUNCTION.getAsDescriptor());
        return new InvokeNode(INVOKEINTERFACE, RemappingClassRefs.CLASS_DENSITY_FUNCTION.get(), RemappingClassRefs.METHOD_DENSITY_FUNC_COMPUTE.get(),
                ClassRefs.methodDescriptor(ClassRefs.DOUBLE, RemappingClassRefs.CLASS_FUNCTION_CONTEXT.get()),
                getfield, new VarReferenceNode(new Var(1), ALOAD));
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
