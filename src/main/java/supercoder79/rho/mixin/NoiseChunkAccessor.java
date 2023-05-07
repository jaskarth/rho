package supercoder79.rho.mixin;

import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.levelgen.NoiseChunk;
import net.minecraft.world.level.levelgen.NoiseRouter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;

@Mixin(NoiseChunk.class)
public interface NoiseChunkAccessor {
    @Invoker
    Climate.Sampler callCachedClimateSampler(NoiseRouter noiseRouter, List<Climate.ParameterPoint> list);

    @Accessor
    long getArrayInterpolationCounter();

    @Accessor
    int getCellWidth();

    @Accessor
    int getCellHeight();

    @Accessor
    int getCellCountY();

    @Accessor
    int getArrayIndex();

    @Accessor
    int getFirstNoiseX();

    @Accessor
    int getFirstNoiseZ();

    @Accessor
    int getNoiseSizeXZ();

}
