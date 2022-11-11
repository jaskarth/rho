package supercoder79.rho.ast.high.noise;

import net.minecraft.world.level.levelgen.DensityFunction;
import supercoder79.rho.ClassRefs;
import supercoder79.rho.ast.Node;
import supercoder79.rho.ast.common.ConstNode;
import supercoder79.rho.ast.common.MulNode;
import supercoder79.rho.ast.low.ContextBlockInsnNode;
import supercoder79.rho.ast.low.GetFieldNode;
import supercoder79.rho.ast.low.InsnNode;
import supercoder79.rho.ast.low.SequenceNode;
import supercoder79.rho.gen.CodegenContext;

import java.util.List;

public record NoiseNode(int noiseIdx, double xzScale, double yScale) implements Node {

    @Override
    public Node lower(CodegenContext ctx) {
        String id = ctx.getNextFieldId("noise");
        ctx.addFieldGen(cl -> cl.visitField(ACC_PRIVATE, id, "Lnet/minecraft/world/level/levelgen/synth/NormalNoise;", null, null));
        ctx.addCtorFieldRef(new CodegenContext.MinSelfFieldRef(id, "Lnet/minecraft/world/level/levelgen/synth/NormalNoise;"), noiseIdx);

        Node getfield = new GetFieldNode(false, ctx.contextName(), id, "Lnet/minecraft/world/level/levelgen/synth/NormalNoise;");
        Node x = new MulNode(new ContextBlockInsnNode(CodegenContext.Type.X), new ConstNode(xzScale));
        Node y = new MulNode(new ContextBlockInsnNode(CodegenContext.Type.Y), new ConstNode(yScale));
        Node z = new MulNode(new ContextBlockInsnNode(CodegenContext.Type.Z), new ConstNode(xzScale));

        return new InsnNode(INVOKEVIRTUAL, "net/minecraft/world/level/levelgen/synth/NormalNoise", "getValue",
                ClassRefs.methodDescriptor(ClassRefs.DOUBLE, ClassRefs.DOUBLE, ClassRefs.DOUBLE, ClassRefs.DOUBLE),
                getfield, x, y, z);
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
