package supercoder79.rho.mixin;

import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.levelgen.blending.Blender;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import supercoder79.rho.RhoDensityFunction;

@Mixin(NoiseChunk.class)
public class MixinNoiseChunk {
    @Inject(method = "<init>", at = @At("TAIL"))
    private void setupState(int i, RandomState randomState, int j, int k, NoiseSettings noiseSettings, DensityFunctions.BeardifierOrMarker beardifierOrMarker, NoiseGeneratorSettings noiseGeneratorSettings, Aquifer.FluidPicker fluidPicker, Blender blender, CallbackInfo ci) {
        ChunkPos p = new ChunkPos(j >> 4, k >> 4);
        randomState.router().mapAll(d -> {
            if (d instanceof RhoDensityFunction rho) {
                rho.rho().init(p);
            }

            return d;
        });
    }
}
