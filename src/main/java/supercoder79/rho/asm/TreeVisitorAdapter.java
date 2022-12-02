package supercoder79.rho.asm;

import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.util.Printer;

public class TreeVisitorAdapter extends MethodVisitor {
    public final MethodNode node;

    protected TreeVisitorAdapter(int access, String name, String desc, MethodVisitor mv) {
        super(Opcodes.ASM9, mv);
        this.node = new MethodNode(access, name, desc, null, null);
    }

    @Override
    public void visitInsn(int opcode) {
        super.visitInsn(opcode);
        node.visitInsn(opcode);
    }

    @Override
    public void visitIntInsn(int opcode, int operand) {
        super.visitIntInsn(opcode, operand);
        node.visitIntInsn(opcode, operand);
    }

    @Override
    public void visitVarInsn(int opcode, int varIndex) {
        super.visitVarInsn(opcode, varIndex);
        node.visitVarInsn(opcode, varIndex);
    }

    @Override
    public void visitTypeInsn(int opcode, String type) {
        super.visitTypeInsn(opcode, type);
        node.visitTypeInsn(opcode, type);
    }

    @Override
    public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
        super.visitFieldInsn(opcode, owner, name, descriptor);
        node.visitFieldInsn(opcode, owner, name, descriptor);
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
        super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
        node.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
    }

    @Override
    public void visitInvokeDynamicInsn(String name, String descriptor, Handle bootstrapMethodHandle, Object... bootstrapMethodArguments) {
        super.visitInvokeDynamicInsn(name, descriptor, bootstrapMethodHandle, bootstrapMethodArguments);
        node.visitInvokeDynamicInsn(name, descriptor, bootstrapMethodHandle, bootstrapMethodArguments);
    }

    @Override
    public void visitJumpInsn(int opcode, Label label) {
        super.visitJumpInsn(opcode, label);
        node.visitJumpInsn(opcode, label);
    }

    @Override
    public void visitLabel(Label label) {
        super.visitLabel(label);
        node.visitLabel(label);
    }

    @Override
    public void visitLdcInsn(Object value) {
        super.visitLdcInsn(value);
        node.visitLdcInsn(value);
    }

    @Override
    public void visitIincInsn(int varIndex, int increment) {
        super.visitIincInsn(varIndex, increment);
        node.visitIincInsn(varIndex, increment);
    }

    @Override
    public void visitTableSwitchInsn(int min, int max, Label dflt, Label... labels) {
        super.visitTableSwitchInsn(min, max, dflt, labels);
        node.visitTableSwitchInsn(min, max, dflt, labels);
    }

    @Override
    public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) {
        super.visitLookupSwitchInsn(dflt, keys, labels);
        node.visitLookupSwitchInsn(dflt, keys, labels);
    }

    @Override
    public void visitMultiANewArrayInsn(String descriptor, int numDimensions) {
        super.visitMultiANewArrayInsn(descriptor, numDimensions);
        node.visitMultiANewArrayInsn(descriptor, numDimensions);
    }

    @Override
    public void visitFrame(int type, int numLocal, Object[] local, int numStack, Object[] stack) {
        super.visitFrame(type, numLocal, local, numStack, stack);
        node.visitFrame(type, numLocal, local, numStack, stack);
    }

    @Override
    public void visitMaxs(int maxStack, int maxLocals) {
        super.visitMaxs(maxStack, maxLocals);
        node.visitMaxs(maxStack, maxLocals);
    }
}
