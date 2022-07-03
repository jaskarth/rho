package supercoder79.rho.opto.passes;

import supercoder79.rho.ast.Node;
import supercoder79.rho.gen.CodegenContext;

public final class LowerAst {
    public static Node lower(CodegenContext ctx, Node node) {
        return node.lower(ctx);
    }
}
