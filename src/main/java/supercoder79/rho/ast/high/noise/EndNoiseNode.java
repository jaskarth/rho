package supercoder79.rho.ast.high.noise;

import supercoder79.rho.ClassRefs;
import supercoder79.rho.ast.Node;
import supercoder79.rho.ast.common.*;
import supercoder79.rho.ast.low.ContextBlockInsnNode;
import supercoder79.rho.ast.low.GetFieldNode;
import supercoder79.rho.ast.low.InvokeNode;
import supercoder79.rho.gen.CodegenContext;

import java.util.List;

public record EndNoiseNode(int noiseIdx) implements Node {

    @Override
    public Node lower(CodegenContext ctx) {
        String id = ctx.getNextFieldId("noise");

        ctx.addFieldGen(cl -> cl.visitField(ACC_PRIVATE, id, "Lnet/minecraft/world/level/levelgen/synth/SimplexNoise;", null, null));
        ctx.addCtorFieldRef(new CodegenContext.MinSelfFieldRef(id, "Lnet/minecraft/world/level/levelgen/synth/SimplexNoise;"), noiseIdx);

        Node getfield = new GetFieldNode(false, ctx.contextName(), id, "Lnet/minecraft/world/level/levelgen/synth/SimplexNoise;");
        Node x = new IDivNode(new ContextBlockInsnNode(CodegenContext.Type.X, false), new IConstNode(8));
        Node z = new IDivNode(new ContextBlockInsnNode(CodegenContext.Type.Z, false), new IConstNode(8));

        InvokeNode insn = new InvokeNode(INVOKESTATIC,
                ClassRefs.DENSITY_SUPPORT, "endNoise", "(Lnet/minecraft/world/level/levelgen/synth/SimplexNoise;II)D",
                getfield, x, z);

        return new MulNode(new AddNode(insn, new ConstNode(-8)), new ConstNode(1 / 128.0));
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
