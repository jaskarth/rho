package supercoder79.rho.ast;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import supercoder79.rho.gen.CodegenContext;

import java.util.List;


public interface Node extends Opcodes {
    default Node lower(CodegenContext ctx) {
        return this;
    }

    default boolean isLow() {
        return false;
    }

    default void codegen(CodegenContext ctx, MethodVisitor visitor) {
        if (isLow()) {
            throw new IllegalStateException("Lowered node must generate code: " + getClass().getSimpleName());
        } else {
            throw new IllegalStateException("Cannot codegen a non-lowered node: " + this.getClass().getSimpleName());
        }
    }

    Node replaceNode(Node old, Node newNode);

    List<Node> children();

    default String getDotNodeLabel() {
        return "";
    }

    default String getDotEdgeLabel(Node child) {
        return "";
    }
}
