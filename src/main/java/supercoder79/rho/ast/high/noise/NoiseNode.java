package supercoder79.rho.ast.high.noise;

import org.objectweb.asm.Opcodes;
import supercoder79.rho.ClassRefs;
import supercoder79.rho.RemappingClassRefs;
import supercoder79.rho.ast.Node;
import supercoder79.rho.ast.common.ConstNode;
import supercoder79.rho.ast.common.MulNode;
import supercoder79.rho.ast.low.ContextBlockInsnNode;
import supercoder79.rho.ast.low.GetFieldNode;
import supercoder79.rho.ast.low.IfElseNode;
import supercoder79.rho.ast.low.InvokeNode;
import supercoder79.rho.gen.CodegenContext;

import java.util.List;

public record NoiseNode(int noiseIdx, double xzScale, double yScale, boolean unchecked) implements Node {

    @Override
    public Node lower(CodegenContext ctx) {
        String id = ctx.getNextFieldId("noise");
        ctx.addFieldGen(cl -> cl.visitField(ACC_PRIVATE, id, RemappingClassRefs.CLASS_NORMALNOISE.getAsDescriptor(), null, null));
        ctx.addCtorFieldRef(new CodegenContext.MinSelfFieldRef(id, RemappingClassRefs.CLASS_NORMALNOISE.getAsDescriptor()), noiseIdx);

        Node getfield = new GetFieldNode(false, null, id, RemappingClassRefs.CLASS_NORMALNOISE.getAsDescriptor());
        Node x = new MulNode(new ContextBlockInsnNode(CodegenContext.Type.X), new ConstNode(xzScale));
        Node y = new MulNode(new ContextBlockInsnNode(CodegenContext.Type.Y), new ConstNode(yScale));
        Node z = new MulNode(new ContextBlockInsnNode(CodegenContext.Type.Z), new ConstNode(xzScale));

        final InvokeNode invokeNode = new InvokeNode(INVOKEVIRTUAL, RemappingClassRefs.CLASS_NORMALNOISE.get(), RemappingClassRefs.METHOD_NORMALNOISE_GETVALUE.get(),
                ClassRefs.methodDescriptor(ClassRefs.DOUBLE, ClassRefs.DOUBLE, ClassRefs.DOUBLE, ClassRefs.DOUBLE),
                getfield, x, y, z);
        return unchecked ? invokeNode : new IfElseNode(getfield, Opcodes.IFNONNULL, invokeNode, new ConstNode(0.0));
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
