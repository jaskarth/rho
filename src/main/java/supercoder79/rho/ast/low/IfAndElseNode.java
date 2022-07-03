package supercoder79.rho.ast.low;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import supercoder79.rho.ast.Node;
import supercoder79.rho.gen.CodegenContext;

import java.util.List;

@Deprecated // Only until ast can represent && in IfElseNode
public record IfAndElseNode(Node cond, Node cond2, int iftype, int iftype2, Node trueBranch, Node falseBranch) implements Node {

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
        // &&
        cond2.codegen(ctx, visitor);
        visitor.visitJumpInsn(iftype2, isTrue);

        falseBranch.codegen(ctx, visitor);
        visitor.visitJumpInsn(GOTO, endIf);

        visitor.visitLabel(isTrue);
        trueBranch.codegen(ctx, visitor);
        visitor.visitLabel(endIf);
    }

    @Override
    public Node replaceNode(Node old, Node newNode) {
        if (old == cond) {
            return new IfAndElseNode(newNode, cond2, iftype, iftype2, trueBranch, falseBranch);
        } else if (old == cond2) {
            return new IfAndElseNode(cond, newNode, iftype, iftype2, trueBranch, falseBranch);
        } else if (old == trueBranch) {
            return new IfAndElseNode(cond, cond2, iftype, iftype2, newNode, falseBranch);
        } else if (old == falseBranch) {
            return new IfAndElseNode(cond, cond2, iftype, iftype2, trueBranch, newNode);
        }

        return this;
    }

    @Override
    public List<Node> children() {
        return List.of(cond, cond2,trueBranch, falseBranch);
    }
}
