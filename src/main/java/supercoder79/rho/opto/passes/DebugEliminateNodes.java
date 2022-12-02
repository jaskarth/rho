package supercoder79.rho.opto.passes;

import supercoder79.rho.ast.Node;
import supercoder79.rho.ast.common.ConstNode;
import supercoder79.rho.gen.CodegenContext;
import supercoder79.rho.opto.NodeVisitor;

public class DebugEliminateNodes {
    public static Node eliminateTree(CodegenContext ctx, Node node) {
        return NodeVisitor.visitNodeTreeReplacing(node, DebugEliminateNodes::visitTree);
    }

    private static Node visitTree(Node node) {
        return node;
    }
}
