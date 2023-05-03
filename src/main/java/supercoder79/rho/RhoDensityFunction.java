package supercoder79.rho;

import com.mojang.serialization.MapCodec;
import net.minecraft.util.CubicSpline;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.DensityFunctions;

import java.util.ArrayList;
import java.util.List;

public record RhoDensityFunction(RhoClass rho, double minValue, double maxValue) implements DensityFunction {
    @Override
    public double compute(FunctionContext functionContext) {
        return rho.compute(functionContext);
    }

    @Override
    public void fillArray(double[] ds, ContextProvider contextProvider) {
        contextProvider.fillAllDirectly(ds, this);
    }

    @Override
    public DensityFunction mapAll(Visitor visitor) {
        List args = new ArrayList(rho.getArgs());
        mapArgs(visitor, args);

        return visitor.apply(new RhoDensityFunction(rho.makeNew(args), minValue, maxValue));
    }

    public static void mapArgs(Visitor visitor, List args) {
        for (int i = 0; i < args.size(); i++) {
            Object o = args.get(i);
//            if (o instanceof FlatCache2) {
//                args.set(i, new FlatCache2.Impl(o.hashCode()));
//            } else
//                if (o instanceof SingleCache) {
//                args.set(i, new SingleCache.Impl(o.hashCode()));
//            } else if (o instanceof OnceCache) {
//                args.set(i, new OnceCache.Impl(o.hashCode()));
//            }
            if (visitor instanceof RhoCacheAwareVisitor cacheAwareVisitor && (o instanceof FlatCache2 || o instanceof SingleCache || o instanceof OnceCache)) {
                args.set(i, cacheAwareVisitor.rho$visitCache(o));
            }

            if (o instanceof DensityFunctions.Marker marker) {
                args.set(i, visitor.apply(marker));
            }

            if (o instanceof DensityFunctions.BeardifierOrMarker beardifierOrMarker) {
                args.set(i, visitor.apply(beardifierOrMarker));
            }

            if (o instanceof CubicSpline s) {
                args.set(i, s.mapAll(coordinate -> {
                    if (coordinate instanceof DensityFunctions.Spline.Coordinate coord) {
                        return coord.mapAll(visitor);
                    }

                    if (coordinate instanceof RhoSplineCoord coord) {
                        return coord.remap(visitor);
                    }

                    return coordinate;
                }));
            }
        }
    }

    @Override
    public KeyDispatchDataCodec<? extends DensityFunction> codec() {
        return CODEC;
    }

    public static final KeyDispatchDataCodec<RhoDensityFunction> CODEC = KeyDispatchDataCodec.of(
            MapCodec.unit(new RhoDensityFunction(null, 0, 0))
    );
}
