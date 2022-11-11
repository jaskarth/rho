package supercoder79.rho.opto.passes;

import supercoder79.rho.ast.Node;
import supercoder79.rho.ast.common.AddNode;
import supercoder79.rho.ast.common.ConstNode;
import supercoder79.rho.gen.CodegenContext;
import supercoder79.rho.opto.NodeVisitor;

public class InstCombine {
    public static Node combineInstructions(CodegenContext ctx, Node node) {
        return NodeVisitor.visitNodeTreeReplacing(node, InstCombine::visitTree);
    }

    private static Node visitTree(Node node) {
        if (node instanceof AddNode add) {
            // Combine two additions
            if (add.right() instanceof ConstNode c1) {
                if (add.left() instanceof AddNode a && a.left() instanceof ConstNode c2) {
                    return new AddNode(a.right(), new ConstNode(c1.value() + c2.value()));
                }

                if (add.left() instanceof AddNode a && a.right() instanceof ConstNode c2) {
                    return new AddNode(a.left(), new ConstNode(c1.value() + c2.value()));
                }
            }

            if (add.left() instanceof ConstNode c1) {
                if (add.right() instanceof AddNode a && a.left() instanceof ConstNode c2) {
                    return new AddNode(a.right(), new ConstNode(c1.value() + c2.value()));
                }

                if (add.right() instanceof AddNode a && a.right() instanceof ConstNode c2) {
                    return new AddNode(a.left(), new ConstNode(c1.value() + c2.value()));
                }
            }
        }

        return node;
    }
}
