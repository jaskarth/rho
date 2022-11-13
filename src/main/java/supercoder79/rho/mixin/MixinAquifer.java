package supercoder79.rho.mixin;

import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.Aquifer;
import net.minecraft.world.level.levelgen.NoiseChunk;
import net.minecraft.world.level.levelgen.NoiseRouter;
import net.minecraft.world.level.levelgen.PositionalRandomFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import supercoder79.rho.opto.algorithm.RhoAquifer;

@Mixin(Aquifer.class)
public interface MixinAquifer {
    /**
     * @author SuperCoder79
     * @reason Optimize Aquifer code to calculate values faster
     */
    @Overwrite
    static Aquifer create(
            NoiseChunk noiseChunk,
            ChunkPos chunkPos,
            NoiseRouter noiseRouter,
            PositionalRandomFactory positionalRandomFactory,
            int i,
            int j,
            Aquifer.FluidPicker fluidPicker
    ) {
        return new RhoAquifer(noiseChunk, chunkPos, noiseRouter, positionalRandomFactory, i, j, fluidPicker);
    }
}
