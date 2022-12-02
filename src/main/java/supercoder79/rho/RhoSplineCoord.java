package supercoder79.rho;

import net.minecraft.util.ToFloatFunction;
import net.minecraft.world.level.levelgen.DensityFunctions;

public record RhoSplineCoord(RhoClass rho, float min, float max) implements ToFloatFunction<DensityFunctions.Spline.Point> {
    @Override
    public float apply(DensityFunctions.Spline.Point point) {
        return (float) rho.compute(point.context());
    }

    @Override
    public float minValue() {
        return min;
    }

    @Override
    public float maxValue() {
        return max;
    }
}
