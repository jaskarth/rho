package supercoder79.rho.ast.high.complex;

import org.objectweb.asm.Opcodes;
import supercoder79.rho.ast.Node;
import supercoder79.rho.ast.Var;
import supercoder79.rho.ast.low.*;
import supercoder79.rho.gen.CodegenContext;

import java.util.List;

public record CacheFlatNode(int index, Node node) implements Node {
    public static final String CACHE_DESC = "Lsupercoder79/rho/FlatCache2;";
    public static final String CACHE_NAME = "supercoder79/rho/FlatCache2";

    @Override
    public Node lower(CodegenContext ctx) {

        Var varJ = ctx.getNextVar();
        ctx.addLocalVar(varJ, "J");

        String id = ctx.getNextFieldId("flatCache2_");
        ctx.addFieldGen(cl -> cl.visitField(ACC_PRIVATE, id, CACHE_DESC, null, null));
        ctx.addCtorFieldRef(new CodegenContext.MinSelfFieldRef(id, CACHE_DESC), index);

        Node asLong = new InsnNode(
                INVOKESTATIC, "net/minecraft/world/level/ChunkPos", "asLong", "(II)J",
                new ContextBlockInsnNode(CodegenContext.Type.X, false), new ContextBlockInsnNode(CodegenContext.Type.Z, false));

        Node varDef = new VarAssignNode(varJ, asLong, LSTORE);

        // TODO: dup instead of multiple getfield?
        Node fieldCache = new GetFieldNode(false, ctx.contextName(), id, CACHE_DESC);
        Node isInCache = new InsnNode(INVOKEINTERFACE, CACHE_NAME, "isInCache", "(J)Z", new VarReferenceNode(varJ, LLOAD));

        return new IfElseNode(new SequenceNode(varDef, fieldCache, isInCache), IFEQ,
                // TODO: why is this inverted?
                new SequenceNode(
                        new GetFieldNode(false, ctx.contextName(), id, CACHE_DESC),
                        new InsnNode(INVOKEINTERFACE, CACHE_NAME, "getAndPutInCache", "(JD)D", new VarReferenceNode(varJ, LLOAD), node.lower(ctx))
                ),
                new SequenceNode(
                        new GetFieldNode(false, ctx.contextName(), id, CACHE_DESC),
                        new InsnNode(INVOKEINTERFACE, CACHE_NAME, "getFromCache", "(J)D", new VarReferenceNode(varJ, LLOAD))
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
