package supercoder79.rho.mixin;

import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.levelgen.blending.Blender;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import supercoder79.rho.RhoDensityFunction;

@Mixin(NoiseChunk.class)
public class MixinNoiseChunk {
    @Shadow @Final
    int firstNoiseX;

    @Shadow @Final
    int firstNoiseZ;

    @Inject(method = "wrapNew", at = @At("HEAD"))
    private void setupState(DensityFunction func, CallbackInfoReturnable<DensityFunction> cir) {
        if (func instanceof RhoDensityFunction rho) {
            rho.rho().init(new ChunkPos(this.firstNoiseX << 4, this.firstNoiseZ << 4));
        }
    }
}
