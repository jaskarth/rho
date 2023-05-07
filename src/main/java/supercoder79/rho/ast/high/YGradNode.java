package supercoder79.rho.ast.high;

import supercoder79.rho.RemappingClassRefs;
import supercoder79.rho.ast.Node;
import supercoder79.rho.ast.common.ConstNode;
import supercoder79.rho.ast.low.ContextBlockInsnNode;
import supercoder79.rho.ast.low.InvokeNode;
import supercoder79.rho.ast.low.SequenceNode;
import supercoder79.rho.gen.CodegenContext;

import java.util.List;

public record YGradNode(int fromY, int toY, double fromValue, double toValue) implements Node {
    @Override
    public Node lower(CodegenContext ctx) {
//        throw new RuntimeException();

        // FIXME: impl correctly


        return new InvokeNode(INVOKESTATIC, RemappingClassRefs.CLASS_MTH.get(), RemappingClassRefs.METHOD_MTH_CLAMPEDMAP.get(), "(DDDDD)D",
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
