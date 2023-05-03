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

        Var varJ = ctx.getNextVar();
        ctx.addLocalVar(varJ, ClassRefs.LONG);

        String id = ctx.getNextFieldId("flatCache2_");
        ctx.addFieldGen(cl -> cl.visitField(ACC_PRIVATE, id, CACHE_DESC, null, null));
        ctx.addCtorFieldRef(new CodegenContext.MinSelfFieldRef(id, CACHE_DESC), index);

        Node asLong = new InvokeNode(
                INVOKESTATIC, RemappingClassRefs.CLASS_CHUNKPOS.get(), RemappingClassRefs.METHOD_CHUNKPOS_ASLONG.get(), "(II)J",
                new ContextBlockInsnNode(CodegenContext.Type.X, false), new ContextBlockInsnNode(CodegenContext.Type.Z, false));

        Node varDef = new VarAssignNode(varJ, asLong, LSTORE);

        // TODO: dup instead of multiple getfield?
        Node fieldCache = new GetFieldNode(false, null, id, CACHE_DESC);
        Node isInCache = new InvokeNode(INVOKEINTERFACE, ClassRefs.FLAT_CACHE_2, "isInCache", "(J)Z", new VarReferenceNode(varJ, LLOAD));

        return new IfElseNode(new SequenceNode(varDef, fieldCache, isInCache), IFEQ,
                // TODO: why is this inverted?
                new SequenceNode(
                        new GetFieldNode(false, null, id, CACHE_DESC),
                        new InvokeNode(INVOKEINTERFACE, ClassRefs.FLAT_CACHE_2, "getAndPutInCache", "(JD)D", new VarReferenceNode(varJ, LLOAD), node.lower(ctx))
                ),
                new SequenceNode(
                        new GetFieldNode(false, null, id, CACHE_DESC),
                        new InvokeNode(INVOKEINTERFACE, ClassRefs.FLAT_CACHE_2, "getFromCache", "(J)D", new VarReferenceNode(varJ, LLOAD))
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
