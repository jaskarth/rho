package supercoder79.rho.ast.high.noise;

import org.objectweb.asm.Opcodes;
import supercoder79.rho.RemappingClassRefs;
import supercoder79.rho.ast.Node;
import supercoder79.rho.ast.common.ConstNode;
import supercoder79.rho.ast.common.MulNode;
import supercoder79.rho.ast.low.GetFieldNode;
import supercoder79.rho.ast.low.IfElseNode;
import supercoder79.rho.ast.low.InvokeNode;
import supercoder79.rho.gen.CodegenContext;

import java.util.List;

public record ShiftNoiseNode(int noiseIdx, Node shiftX, Node shiftY, Node shiftZ, boolean unchecked) implements Node {

    @Override
    public Node lower(CodegenContext ctx) {
        String id = ctx.getNextFieldId("noise");
        ctx.addFieldGen(cl -> cl.visitField(ACC_PRIVATE, id, RemappingClassRefs.CLASS_NORMALNOISE.getAsDescriptor(), null, null));
        ctx.addCtorFieldRef(new CodegenContext.MinSelfFieldRef(id, RemappingClassRefs.CLASS_NORMALNOISE.getAsDescriptor()), noiseIdx);

        Node getfield = new GetFieldNode(false, null, id, RemappingClassRefs.CLASS_NORMALNOISE.getAsDescriptor());
        Node x = new MulNode(shiftX, new ConstNode(0.25)).lower(ctx);
        Node y = new MulNode(shiftY, new ConstNode(0.25)).lower(ctx);
        Node z = new MulNode(shiftZ, new ConstNode(0.25)).lower(ctx);

        final InvokeNode invokeNode = new InvokeNode(INVOKEVIRTUAL, RemappingClassRefs.CLASS_NORMALNOISE.get(), RemappingClassRefs.METHOD_NORMALNOISE_GETVALUE.get(), "(DDD)D", getfield, x, y, z);
        return new MulNode(unchecked ? invokeNode : new IfElseNode(getfield, Opcodes.IFNONNULL, invokeNode, new ConstNode(0.0)), new ConstNode(4.0));
    }

    @Override
    public Node replaceNode(Node old, Node newNode) {
        if (old == shiftX) {
            return new ShiftNoiseNode(noiseIdx, newNode, shiftY, shiftZ, unchecked);
        } else if (old == shiftY) {
            return new ShiftNoiseNode(noiseIdx, shiftX, newNode, shiftZ, unchecked);
        } else if (old == shiftZ) {
            return new ShiftNoiseNode(noiseIdx, shiftX, shiftY, newNode, unchecked);
        }

        return this;
    }

    @Override
    public List<Node> children() {
        return List.of(shiftX, shiftY, shiftZ);
    }
}