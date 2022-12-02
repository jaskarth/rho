package supercoder79.rho.opto.passes;

import supercoder79.rho.ast.Node;
import supercoder79.rho.gen.CodegenContext;
import supercoder79.rho.opto.NodeVisitor;

import java.util.HashMap;
import java.util.Map;

public class GlobalNodeNumbering {
    public static Map<Node, Integer> numbers = new HashMap<>();
    private static int pointer = 0;

    public static Node numberNodes(CodegenContext ctx, Node node) {
        return NodeVisitor.visitNodeTreeReplacing(node, GlobalNodeNumbering::visitTree);
    }

    private static Node visitTree(Node node) {
        numbers.put(node, pointer++);
        return node;
    }

}
