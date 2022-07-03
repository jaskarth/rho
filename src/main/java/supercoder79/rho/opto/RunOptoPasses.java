package supercoder79.rho.opto;

import supercoder79.rho.ast.Node;
import supercoder79.rho.gen.CodegenContext;

import java.util.List;

public final class RunOptoPasses {
    public static Node optimizeAst(CodegenContext ctx, Node node, boolean optimize) {
        List<OptoPass> passes = optimize ? OptoPasses.COMPILE_OPTIMIZETEST : OptoPasses.COMPILE_DONTOPTIMIZE;

        for (OptoPass pass : passes) {
            node = pass.run(ctx, node);
        }

        System.out.println("Finished optimization");

        return node;
    }
}
