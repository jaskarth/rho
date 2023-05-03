package supercoder79.rho.opto.passes;

import supercoder79.rho.ast.Node;
import supercoder79.rho.ast.common.AddNode;
import supercoder79.rho.ast.common.ConstNode;
import supercoder79.rho.ast.common.MulNode;
import supercoder79.rho.ast.high.complex.CacheFlatNode;
import supercoder79.rho.ast.high.complex.CacheOnceNode;
import supercoder79.rho.gen.CodegenContext;
import supercoder79.rho.opto.NodeVisitor;

public class FoldConstants {
    public static Node foldConstants(CodegenContext ctx, Node node) {
        return NodeVisitor.visitNodeTreeReplacing(node, FoldConstants::visitTree);
    }

    private static Node visitTree(Node node) {
        if (node instanceof AddNode add) {
            if (add.left() instanceof ConstNode c1 && add.right() instanceof ConstNode c2) {
                return new ConstNode(c1.value() + c2.value());
            }

            // Identity rule
            if (add.right() instanceof ConstNode c && c.value() == 0) {
                return add.left();
            }

            if (add.left() instanceof ConstNode c && c.value() == 0) {
                return add.right();
            }
        }

        if (node instanceof MulNode mul) {
            if (mul.left() instanceof ConstNode c1 && mul.right() instanceof ConstNode c2) {
                return new ConstNode(c1.value() * c2.value());
            }

            // Identity rule
            if (mul.right() instanceof ConstNode c && c.value() == 1) {
                return mul.left();
            }

            if (mul.left() instanceof ConstNode c && c.value() == 1) {
                return mul.right();
            }

            // Zero rule
            if (mul.right() instanceof ConstNode c && c.value() == 0) {
                return new ConstNode(0);
            }

            if (mul.left() instanceof ConstNode c && c.value() == 0) {
                return new ConstNode(0);
            }
        }

        if (node instanceof CacheOnceNode cache) {
            if (cache.node() instanceof ConstNode c) {
                return new ConstNode(c.value());
            }
        }



        return node;
    }
}
