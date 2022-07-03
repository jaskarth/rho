package supercoder79.rho.opto;

import supercoder79.rho.ast.Node;
import supercoder79.rho.gen.CodegenContext;

import java.util.function.Function;

@FunctionalInterface
public interface OptoPass {
    Node run(CodegenContext ctx, Node node);
}
