package supercoder79.rho.ast.low;

import org.objectweb.asm.MethodVisitor;
import supercoder79.rho.ast.Node;
import supercoder79.rho.ast.Var;
import supercoder79.rho.gen.CodegenContext;

import java.util.List;

public record VarAssignNode(Var var, Node value, int type) implements Node {
    public VarAssignNode(Var var, Node value) {
        this(var, value, DSTORE);
    }

    @Override
    public boolean isLow() {
        return true;
    }

    @Override
    public void codegen(CodegenContext ctx, MethodVisitor visitor) {
        value.codegen(ctx, visitor);

        visitor.visitVarInsn(type, var.index());
    }

    @Override
    public Node replaceNode(Node old, Node newNode) {
        if (value == old) {
            return new VarAssignNode(var, newNode, type);
        }

        return this;
    }

    @Override
    public List<Node> children() {
        return List.of(value);
    }

    @Override
    public String getDotNodeLabel() {
        return "" + var.index();
    }
}
