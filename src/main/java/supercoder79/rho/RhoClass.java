package supercoder79.rho;

import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.DensityFunction;

import java.util.List;

public interface RhoClass {
    double compute(DensityFunction.FunctionContext ctx);

    default void init(ChunkPos pos) {

    }

    RhoClass makeNew(List list);

    List getArgs();
}
