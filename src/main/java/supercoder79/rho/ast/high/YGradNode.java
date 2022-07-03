package supercoder79.rho.ast.high;

import org.objectweb.asm.Opcodes;
import supercoder79.rho.ast.Node;
import supercoder79.rho.ast.common.ConstNode;
import supercoder79.rho.ast.low.ContextBlockInsnNode;
import supercoder79.rho.ast.low.InsnNode;
import supercoder79.rho.ast.low.RawInsnNode;
import supercoder79.rho.ast.low.SequenceNode;
import supercoder79.rho.gen.CodegenContext;

import java.util.List;

public record YGradNode(int fromY, int toY, double fromValue, double toValue) implements Node {
    @Override
    public Node lower(CodegenContext ctx) {
//        throw new RuntimeException();

        // FIXME: impl correctly


        return new InsnNode(INVOKESTATIC, "net/minecraft/util/Mth", "clampedMap", "(DDDDD)D",
                new SequenceNode(new ContextBlockInsnNode(CodegenContext.Type.Y)),
                new ConstNode(fromY), new ConstNode(toY), new ConstNode(fromValue), new ConstNode(toValue));
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
