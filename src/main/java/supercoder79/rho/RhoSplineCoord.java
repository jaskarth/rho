package supercoder79.rho;

import net.minecraft.util.ToFloatFunction;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.DensityFunctions;

import java.util.ArrayList;
import java.util.List;

public record RhoSplineCoord(RhoClass rho, float minValue, float maxValue) implements ToFloatFunction<DensityFunctions.Spline.Point> {
    @Override
    public float apply(DensityFunctions.Spline.Point point) {
        return (float) rho.compute(point.context());
    }

    public RhoSplineCoord remap(DensityFunction.Visitor visitor) {
        List args = new ArrayList(rho.getArgs());
        RhoDensityFunction.mapArgs(visitor, args);

        return new RhoSplineCoord(rho.makeNew(args), minValue, maxValue);
    }
}
