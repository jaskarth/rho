package supercoder79.rho.opto;

import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import it.unimi.dsi.fastutil.objects.ReferenceList;
import supercoder79.rho.ast.Node;

import java.util.Deque;
import java.util.LinkedList;
import java.util.function.Function;

public final class NodeVisitor {

    /**
     * Node visit replacement algorithm
     * This function will visit every child node recursively, and if a replacement is found, will restart iteration.
     * This will be called until the node stops being changed so ensure that your actions converge!
     *
     * @param node Root node; will never be called through action.
     * @param action Mutates nodes. Return the given node if no change.
     * @return The new node.
     */
    public static Node visitNodeTreeReplacing(Node node, Function<Node, Node> action) {

        while (true) {
            ReplaceRes res = visitNodeReplacing(node, action);
            node = res.node();

            if (res.type == 1) {
                continue;
            }
            break;
        }

        return node;
    }

    private static ReplaceRes visitNodeReplacing(Node node, Function<Node, Node> action) {
        for (Node child : node.children()) {
            Node newNode = action.apply(child);

            if (child != newNode) {
                Node replaced = node.replaceNode(child, newNode);
                if (replaced == node) {
                    throw new IllegalStateException("Node " + node + " did not replace child " + child + " with " + newNode);
                }

                return new ReplaceRes(1, replaced);
            }

            ReplaceRes res = visitNodeReplacing(child, action);
            if (res.type == 1) {
                Node replaced = node.replaceNode(child, res.node);
                if (replaced == node) {
                    throw new IllegalStateException("Node " + node + " did not replace child " + child + " with " + res.node);
                }

                return new ReplaceRes(1, replaced);
            }
        }

        return new ReplaceRes(0, node);
    }

    private record ReplaceRes(int type, Node node) {

    }
}
