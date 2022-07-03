package supercoder79.rho.ast.low;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import supercoder79.rho.ast.Node;
import supercoder79.rho.gen.CodegenContext;

import java.util.List;

public record IfElseNode(Node cond, int iftype, Node trueBranch, Node falseBranch) implements Node {

    @Override
    public boolean isLow() {
        return true;
    }

    @Override
    public void codegen(CodegenContext ctx, MethodVisitor visitor) {
        Label isTrue = new Label();
        Label endIf = new Label();

        cond.codegen(ctx, visitor);
        visitor.visitJumpInsn(iftype, isTrue);
        falseBranch.codegen(ctx, visitor);
        visitor.visitJumpInsn(GOTO, endIf);

        visitor.visitLabel(isTrue);
        trueBranch.codegen(ctx, visitor);
        visitor.visitLabel(endIf);
    }

    @Override
    public Node replaceNode(Node old, Node newNode) {
        if (old == cond) {
            return new IfElseNode(newNode, iftype, trueBranch, falseBranch);
        } else if (old == trueBranch) {
            return new IfElseNode(cond, iftype, newNode, falseBranch);
        } else if (old == falseBranch) {
            return new IfElseNode(cond, iftype, trueBranch, newNode);
        }

        return this;
    }

    @Override
    public List<Node> children() {
        return List.of(cond, trueBranch, falseBranch);
    }
}
