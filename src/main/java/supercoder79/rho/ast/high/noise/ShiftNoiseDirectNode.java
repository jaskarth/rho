package supercoder79.rho.ast.high.noise;

import org.objectweb.asm.Opcodes;
import supercoder79.rho.RemappingClassRefs;
import supercoder79.rho.ast.Node;
import supercoder79.rho.ast.common.AddNode;
import supercoder79.rho.ast.common.ConstNode;
import supercoder79.rho.ast.common.MulNode;
import supercoder79.rho.ast.low.ContextBlockInsnNode;
import supercoder79.rho.ast.low.GetFieldNode;
import supercoder79.rho.ast.low.IfElseNode;
import supercoder79.rho.ast.low.InvokeNode;
import supercoder79.rho.gen.CodegenContext;

import java.util.List;

public record ShiftNoiseDirectNode(int noiseIdx, double xzScale, double yScale, Node shiftX, Node shiftY, Node shiftZ) implements Node {

    @Override
    public Node lower(CodegenContext ctx) {
        String id = ctx.getNextFieldId("noise");
        ctx.addFieldGen(cl -> cl.visitField(ACC_PRIVATE, id, RemappingClassRefs.CLASS_NORMALNOISE.getAsDescriptor(), null, null));
        ctx.addCtorFieldRef(new CodegenContext.MinSelfFieldRef(id, RemappingClassRefs.CLASS_NORMALNOISE.getAsDescriptor()), noiseIdx);

        Node getfield = new GetFieldNode(false, ctx.contextName(), id, RemappingClassRefs.CLASS_NORMALNOISE.getAsDescriptor());
        Node x = new AddNode(new MulNode(new ContextBlockInsnNode(CodegenContext.Type.X), new ConstNode(xzScale)), shiftX).lower(ctx);
        Node y = new AddNode(new MulNode(new ContextBlockInsnNode(CodegenContext.Type.Y), new ConstNode(yScale)), shiftY).lower(ctx);
        Node z = new AddNode(new MulNode(new ContextBlockInsnNode(CodegenContext.Type.Z), new ConstNode(xzScale)), shiftZ).lower(ctx);

        final InvokeNode invokeNode = new InvokeNode(INVOKEVIRTUAL, RemappingClassRefs.CLASS_NORMALNOISE.get(), RemappingClassRefs.METHOD_NORMALNOISE_GETVALUE.get(), "(DDD)D", getfield, x, y, z);
        return new IfElseNode(getfield, Opcodes.IFNONNULL, invokeNode, new ConstNode(0.0));
    }

    @Override
    public Node replaceNode(Node old, Node newNode) {
        if (old == shiftX) {
            return new ShiftNoiseDirectNode(noiseIdx, xzScale, yScale, newNode, shiftY, shiftZ);
        } else if (old == shiftY) {
            return new ShiftNoiseDirectNode(noiseIdx, xzScale, yScale, shiftX, newNode, shiftZ);
        } else if (old == shiftZ) {
            return new ShiftNoiseDirectNode(noiseIdx, xzScale, yScale, shiftX, shiftY, newNode);
        }

        return this;
    }

    @Override
    public List<Node> children() {
        return List.of(shiftX, shiftY, shiftZ);
    }
}
