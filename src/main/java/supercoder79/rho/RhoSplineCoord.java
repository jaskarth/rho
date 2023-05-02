package supercoder79.rho;

import net.minecraft.util.ToFloatFunction;
import net.minecraft.world.level.levelgen.DensityFunctions;

import java.util.ArrayList;
import java.util.List;

public record RhoSplineCoord(RhoClass rho, float minValue, float maxValue) implements ToFloatFunction<DensityFunctions.Spline.Point> {
    @Override
    public float apply(DensityFunctions.Spline.Point point) {
        return (float) rho.compute(point.context());
    }

    public RhoSplineCoord remap() {
        List args = new ArrayList(rho.getArgs());
        for (int i = 0; i < args.size(); i++) {
            Object o = args.get(i);
            if (o instanceof FlatCache2) {
                args.set(i, new FlatCache2.Impl());
            } else if (o instanceof SingleCache) {
                args.set(i, new SingleCache.Impl());
            } else if (o instanceof OnceCache) {
                args.set(i, new OnceCache.Impl());
            }
        }

        return new RhoSplineCoord(rho.makeNew(args), minValue, maxValue);
    }
}
