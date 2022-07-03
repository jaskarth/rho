package supercoder79.rho.opto.passes;

import supercoder79.rho.ast.Node;
import supercoder79.rho.ast.common.AddNode;
import supercoder79.rho.ast.common.ConstNode;
import supercoder79.rho.ast.common.MulNode;
import supercoder79.rho.gen.CodegenContext;
import supercoder79.rho.opto.NodeVisitor;

public final class NormalizeTree {
    public static Node normalize(CodegenContext ctx, Node node) {
        return NodeVisitor.visitNodeTreeReplacing(node, NormalizeTree::visitTree);
    }

    private static Node visitTree(Node node) {
        // Make math subtrees correct
        if (node instanceof AddNode add) {
            if (add.left() instanceof ConstNode && !(add.right() instanceof ConstNode)) {
                return add.swap();
            }
        } else if (node instanceof MulNode mul) {
            if (mul.left() instanceof ConstNode && !(mul.right() instanceof ConstNode)) {
                return mul.swap();
            }
        }

        // Eliminate unused types

        return node;
    }
}
