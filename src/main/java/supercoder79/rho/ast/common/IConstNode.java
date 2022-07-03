package supercoder79.rho.ast.common;

import org.objectweb.asm.MethodVisitor;
import supercoder79.rho.ast.Node;
import supercoder79.rho.gen.CodegenContext;

import java.util.List;

public record IConstNode(int value) implements Node {
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
            visitor.visitInsn(ICONST_0);
        } else if (value == 1) {
            visitor.visitInsn(ICONST_1);
        } else if (value == 2) {
            visitor.visitInsn(ICONST_2);
        } else if (value == 3) {
            visitor.visitInsn(ICONST_3);
        } else if (value == 4) {
            visitor.visitInsn(ICONST_4);
        } else if (value == 5) {
            visitor.visitInsn(ICONST_5);
        } else if (value == -1) {
            visitor.visitInsn(ICONST_M1);
        }else {
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
