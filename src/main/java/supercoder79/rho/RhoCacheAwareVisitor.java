package supercoder79.rho;

import net.minecraft.world.level.levelgen.DensityFunction;

public interface RhoCacheAwareVisitor extends DensityFunction.Visitor {

    Object rho$visitCache(Object node);

}
