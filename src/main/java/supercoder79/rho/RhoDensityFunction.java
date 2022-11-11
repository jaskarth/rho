package supercoder79.rho;

import com.mojang.serialization.MapCodec;
import net.minecraft.util.CubicSpline;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.DensityFunctions;

import java.util.List;

public record RhoDensityFunction(RhoClass rho) implements DensityFunction {
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
        List args = rho.getArgs();
        for (int i = 0; i < args.size(); i++) {
            if (args.get(i) instanceof CubicSpline<?,?> s) {
                CubicSpline<DensityFunctions.Spline.Point, DensityFunctions.Spline.Coordinate> spline = (CubicSpline<DensityFunctions.Spline.Point, DensityFunctions.Spline.Coordinate>) (s);
                args.set(i, spline.mapAll(coordinate -> coordinate.mapAll(visitor)));
            }
        }

        return visitor.apply(new RhoDensityFunction(rho.makeNew(args)));
    }

    // Not needed

    @Override
    public double minValue() {
        return 0;
    }

    @Override
    public double maxValue() {
        return 0;
    }

    @Override
    public KeyDispatchDataCodec<? extends DensityFunction> codec() {
        return CODEC;
    }

    public static final KeyDispatchDataCodec<RhoDensityFunction> CODEC = KeyDispatchDataCodec.of(
            MapCodec.unit(new RhoDensityFunction(null))
    );
}
