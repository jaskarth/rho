package supercoder79.rho.ast.high.complex;

import supercoder79.rho.ClassRefs;
import supercoder79.rho.RemappingClassRefs;
import supercoder79.rho.ast.Node;
import supercoder79.rho.ast.Var;
import supercoder79.rho.ast.low.*;
import supercoder79.rho.gen.CodegenContext;

import java.util.List;

public record CacheFlatNode(int index, Node node) implements Node {
    public static final String CACHE_DESC = ClassRefs.descriptor(ClassRefs.FLAT_CACHE_2);

    @Override
    public Node lower(CodegenContext ctx) {
        String id = ctx.getNextFieldId("flatCache2_");
        ctx.addFieldGen(cl -> cl.visitField(ACC_PRIVATE, id, CACHE_DESC, null, null));
        ctx.addCtorFieldRef(new CodegenContext.MinSelfFieldRef(id, CACHE_DESC), index);

        // TODO: dup instead of multiple getfield?
        Node fieldCache = new GetFieldNode(false, null, id, CACHE_DESC);
        Node isInCache = new InvokeNode(INVOKEINTERFACE, ClassRefs.FLAT_CACHE_2, "isInCache", ClassRefs.methodDescriptor(ClassRefs.BOOLEAN, RemappingClassRefs.CLASS_FUNCTION_CONTEXT.get()), new VarReferenceNode(new Var(1), ALOAD));

        return new IfElseNode(new SequenceNode(fieldCache, isInCache), IFEQ,
                // TODO: why is this inverted?
                node.lower(ctx),
                new SequenceNode(
                        new GetFieldNode(false, null, id, CACHE_DESC),
                        new InvokeNode(INVOKEINTERFACE, ClassRefs.FLAT_CACHE_2, "getFromCache", ClassRefs.methodDescriptor(ClassRefs.DOUBLE, RemappingClassRefs.CLASS_FUNCTION_CONTEXT.get()), new VarReferenceNode(new Var(1), ALOAD))
                )
        );
//        throw new RuntimeException();
    }

    @Override
    public Node replaceNode(Node old, Node newNode) {
        if (old == node) {
            return new CacheFlatNode(index, newNode);
        }

        return this;
    }

    @Override
    public List<Node> children() {
        return List.of(node);
    }
}
