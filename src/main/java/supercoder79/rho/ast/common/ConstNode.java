package supercoder79.rho.ast.common;

import org.objectweb.asm.MethodVisitor;
import supercoder79.rho.ast.Node;
import supercoder79.rho.gen.CodegenContext;

import java.util.List;

public record ConstNode(double value) implements Node {
    @Override
    public boolean isLow() {
        return true;
    }

    @Override
    public List<Node> children() {
        return List.of();
    }

    @Override
    public void codegen(CodegenContext ctx, MethodVisitor visitor) {
        if (value == 0) {
            visitor.visitInsn(DCONST_0);
        } else if (value == 1) {
            visitor.visitInsn(DCONST_1);
        } else {
            visitor.visitLdcInsn(value);
        }
    }

    @Override
    public Node replaceNode(Node old, Node newNode) {
        return this;
    }

    @Override
    public String getDotNodeLabel() {
        return "" + value;
    }
}
