package supercoder79.rho.ast.low;

import org.objectweb.asm.MethodVisitor;
import supercoder79.rho.ast.Node;
import supercoder79.rho.gen.CodegenContext;

import java.util.List;

@Deprecated // needs to be lowered w/ insn node, i'm just very lazy
public record ContextBlockInsnNode(CodegenContext.Type type, boolean asDouble) implements Node {
    public ContextBlockInsnNode(CodegenContext.Type type) {
        this(type, true);
    }
    @Override
    public boolean isLow() {
        return true;
    }

    @Override
    public void codegen(CodegenContext ctx, MethodVisitor visitor) {
        ctx.referenceContextBlockXYZ(this.type, this.asDouble);
    }

    @Override
    public Node replaceNode(Node old, Node newNode) {
        return this;
    }

    @Override
    public List<Node> children() {
        return List.of();
    }

    @Override
    public String getDotNodeLabel() {
        return type.toString();
    }
}
