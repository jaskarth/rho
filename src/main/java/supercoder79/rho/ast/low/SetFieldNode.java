package supercoder79.rho.ast.low;


import org.objectweb.asm.MethodVisitor;
import supercoder79.rho.ast.Node;
import supercoder79.rho.gen.CodegenContext;

import java.util.List;

public record SetFieldNode(boolean isStatic, String owner, String name, String descriptor, Node value) implements Node {

    @Override
    public boolean isLow() {
        return true;
    }

    @Override
    public void codegen(CodegenContext ctx, MethodVisitor visitor) {
        if (!isStatic) {
            visitor.visitVarInsn(ALOAD, 0);
        }

        value.codegen(ctx, visitor);

        visitor.visitFieldInsn(isStatic ? PUTSTATIC : PUTFIELD, owner, name, descriptor);
    }

    @Override
    public Node replaceNode(Node old, Node newNode) {
        if (old == value) {
            return new SetFieldNode(isStatic, owner, name, descriptor, newNode);
        }

        return this;
    }

    @Override
    public List<Node> children() {
        return List.of(value);
    }
}
