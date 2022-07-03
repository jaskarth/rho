package supercoder79.rho.opto;

import supercoder79.rho.opto.passes.FoldConstants;
import supercoder79.rho.opto.passes.FoldFma;
import supercoder79.rho.opto.passes.LowerAst;
import supercoder79.rho.opto.passes.NormalizeTree;

import java.util.List;

public final class OptoPasses {
    public static final List<OptoPass> COMPILE_DONTOPTIMIZE = List.of(
            of(NormalizeTree::normalize, "NormTree"),
            of(LowerAst::lower, "Lower")
    );

    public static final List<OptoPass> COMPILE_OPTIMIZETEST = List.of(
            of(NormalizeTree::normalize, "NormTree"),
            of(FoldConstants::foldConstants, "FoldConst"),
            of(LowerAst::lower, "Lower")
    );

    public static final List<OptoPass> COMPILE_OPTIMIZEFULLY = List.of(
            of(NormalizeTree::normalize, "NormTree"),
            of(FoldConstants::foldConstants, "FoldConstBL"),
            of(LowerAst::lower, "Lower"),
            of(FoldConstants::foldConstants, "FoldConstAL"),
            of(FoldFma::foldFma, "FoldFMA"),
            of(FoldConstants::foldConstants, "FoldConstAF")
    );

    public static final List<OptoPass> COMPILE_OPTIMIZENOFMA = List.of(
            of(NormalizeTree::normalize, "NormTree"),
            of(FoldConstants::foldConstants, "FoldConstBL"),
            of(LowerAst::lower, "Lower"),
            of(FoldConstants::foldConstants, "FoldConstAL")
    );

    private static PassWithDot of(OptoPass ps, String n) {
        return new PassWithDot(ps, n);
    }
}
