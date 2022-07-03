package supercoder79.rho.ast.low;

import org.objectweb.asm.MethodVisitor;
import supercoder79.rho.ast.Node;
import supercoder79.rho.ast.Var;
import supercoder79.rho.gen.CodegenContext;

import java.util.List;

public record VarReferenceNode(Var var, int type) implements Node {
    public VarReferenceNode(Var var) {
        this(var, DLOAD);
    }


    @Override
    public boolean isLow() {
        return true;
    }

    @Override
    public void codegen(CodegenContext ctx, MethodVisitor visitor) {
        visitor.visitVarInsn(type, var.index());
    }

    @Override
    public Node replaceNode(Node old, Node newNode) {
        return this;
    }

    @Override
    public List<Node> children() {
        return List.of();
    }

    @Override
    public String getDotNodeLabel() {
        return "" + var.index();
    }
}
