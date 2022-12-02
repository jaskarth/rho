package supercoder79.rho.opto;

import supercoder79.rho.RhoCompiler;
import supercoder79.rho.ast.Node;
import supercoder79.rho.gen.CodegenContext;
import supercoder79.rho.gen.DotExporter;

public record PassWithDot(OptoPass pass, String name) implements OptoPass {
    @Override
    public Node run(CodegenContext ctx, Node node) {
        RhoCompiler.currentStatus = name;

        System.out.println("Running " + name);
        Node res = this.pass.run(ctx, node);
        DotExporter.toDotFile(res, name);

        return res;
    }
}
