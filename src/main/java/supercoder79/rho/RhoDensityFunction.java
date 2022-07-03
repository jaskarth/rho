package supercoder79.rho;

import com.mojang.serialization.MapCodec;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.DensityFunctions;

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
        return visitor.apply(this);
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
