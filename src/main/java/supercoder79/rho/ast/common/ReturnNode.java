package supercoder79.rho.ast.common;

import org.objectweb.asm.MethodVisitor;
import supercoder79.rho.ast.Node;
import supercoder79.rho.gen.CodegenContext;

import java.util.List;

public record ReturnNode(Node body) implements Node {
    @Override
    public Node lower(CodegenContext ctx) {
        return new ReturnNode(body.lower(ctx));
    }

    @Override
    public boolean isLow() {
        return true;
    }

    @Override
    public void codegen(CodegenContext ctx, MethodVisitor visitor) {
        body.codegen(ctx, visitor);

        visitor.visitInsn(DRETURN);
    }

    @Override
    public Node replaceNode(Node old, Node newNode) {
        if (old == body) {
            return new ReturnNode(newNode);
        }

        return this;
    }

    @Override
    public List<Node> children() {
        return List.of(body);
    }
}
