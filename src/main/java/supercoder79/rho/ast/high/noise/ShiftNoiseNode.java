package supercoder79.rho.ast.high.noise;

import supercoder79.rho.ast.Node;
import supercoder79.rho.ast.common.ConstNode;
import supercoder79.rho.ast.common.MulNode;
import supercoder79.rho.ast.low.GetFieldNode;
import supercoder79.rho.ast.low.InvokeNode;
import supercoder79.rho.gen.CodegenContext;

import java.util.List;

public record ShiftNoiseNode(int noiseIdx, Node shiftX, Node shiftY, Node shiftZ) implements Node {

    @Override
    public Node lower(CodegenContext ctx) {
        String id = ctx.getNextFieldId("noise");
        ctx.addFieldGen(cl -> cl.visitField(ACC_PRIVATE, id, "Lnet/minecraft/world/level/levelgen/synth/NormalNoise;", null, null));
        ctx.addCtorFieldRef(new CodegenContext.MinSelfFieldRef(id, "Lnet/minecraft/world/level/levelgen/synth/NormalNoise;"), noiseIdx);

        Node getfield = new GetFieldNode(false, ctx.contextName(), id, "Lnet/minecraft/world/level/levelgen/synth/NormalNoise;");
        Node x = new MulNode(shiftX, new ConstNode(0.25)).lower(ctx);
        Node y = new MulNode(shiftY, new ConstNode(0.25)).lower(ctx);
        Node z = new MulNode(shiftZ, new ConstNode(0.25)).lower(ctx);

        return new MulNode(new InvokeNode(INVOKEVIRTUAL, "net/minecraft/world/level/levelgen/synth/NormalNoise", "getValue", "(DDD)D", getfield, x, y, z), new ConstNode(4.0));
    }

    @Override
    public Node replaceNode(Node old, Node newNode) {
        if (old == shiftX) {
            return new ShiftNoiseNode(noiseIdx, newNode, shiftY, shiftZ);
        } else if (old == shiftY) {
            return new ShiftNoiseNode(noiseIdx, shiftX, newNode, shiftZ);
        } else if (old == shiftZ) {
            return new ShiftNoiseNode(noiseIdx, shiftX, shiftY, newNode);
        }

        return this;
    }

    @Override
    public List<Node> children() {
        return List.of(shiftX, shiftY, shiftZ);
    }
}