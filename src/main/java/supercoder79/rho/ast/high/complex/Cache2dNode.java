package supercoder79.rho.ast.high.complex;

import org.objectweb.asm.Opcodes;
import supercoder79.rho.ast.Node;
import supercoder79.rho.ast.Var;
import supercoder79.rho.ast.low.*;
import supercoder79.rho.gen.CodegenContext;

import java.util.List;

public record Cache2dNode(Node node) implements Node {
    @Override
    public Node lower(CodegenContext ctx) {
        Var varD = ctx.getNextVar();
        ctx.addLocalVar(varD);

        Var varJ = ctx.getNextVar();
        ctx.addLocalVar(varJ, "J");
        
        String idC = ctx.getNextFieldId("cache");
        String idV = ctx.getNextFieldId("val");
        ctx.addFieldGen(cl -> cl.visitField(ACC_PRIVATE, idC, "J", null, null));
        ctx.addFieldGen(cl -> cl.visitField(ACC_PRIVATE, idV, "D", null, null));

        Node asLong = new InsnNode(
                INVOKESTATIC, "net/minecraft/world/level/ChunkPos", "asLong", "(II)J",
                new ContextBlockInsnNode(CodegenContext.Type.X, false), new ContextBlockInsnNode(CodegenContext.Type.Z, false));

        Node varDef = new VarAssignNode(varJ, asLong, LSTORE);

        Node getFieldC = new GetFieldNode(false, ctx.contextName(), idC, "J");
        Node lcmp = new RawInsnNode(LCMP);

        Node putJ = new SetFieldNode(false, ctx.contextName(), idC, "J", new VarReferenceNode(varJ, LLOAD));
        Node varDefD = new VarAssignNode(varD, node.lower(ctx));
        Node putD = new SetFieldNode(false, ctx.contextName(), idV, "D", new VarReferenceNode(varD));
        Node refD = new VarReferenceNode(varD);


        return new IfElseNode(new SequenceNode(varDef, new VarReferenceNode(varJ, LLOAD), getFieldC, lcmp), Opcodes.IFEQ,
                new GetFieldNode(false, ctx.contextName(), idV, "D"),
                new SequenceNode(putJ, varDefD, putD, refD)
                );
    }

    @Override
    public Node replaceNode(Node old, Node newNode) {
        if (old == node) {
            return new Cache2dNode(newNode);
        }

        return this;
    }

    @Override
    public List<Node> children() {
        return List.of(node);
    }
}
