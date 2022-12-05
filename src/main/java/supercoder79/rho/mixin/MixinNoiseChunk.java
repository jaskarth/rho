package supercoder79.rho.mixin;

import net.minecraft.util.CubicSpline;
import net.minecraft.util.ToFloatFunction;
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
import supercoder79.rho.RhoSplineCoord;

@Mixin(NoiseChunk.class)
public class MixinNoiseChunk {
    @Shadow @Final
    int firstNoiseX;

    @Shadow @Final
    int firstNoiseZ;

    @Inject(method = "wrapNew", at = @At("HEAD"))
    private void setupState(DensityFunction func, CallbackInfoReturnable<DensityFunction> cir) {
        if (func instanceof RhoDensityFunction rho) {
            ChunkPos pos = new ChunkPos(this.firstNoiseX >> 2, this.firstNoiseZ >> 2);
            rho.rho().init(pos);

            for (Object arg : rho.rho().getArgs()) {
                rhoRecurseInit(arg, pos);
            }
        }
    }

    private static void rhoRecurseInit(Object o, ChunkPos pos) {
        if (o instanceof CubicSpline.Multipoint spline) {
            ToFloatFunction coordinate = spline.coordinate();
            if (coordinate instanceof RhoSplineCoord rho) {
                rho.rho().init(pos);
            }

            for (Object value : spline.values()) {
                rhoRecurseInit(value, pos);
            }
        }

        if (o instanceof DensityFunctions.Marker marker) {
            if (marker.wrapped() instanceof RhoDensityFunction rho) {
                rho.rho().init(pos);

                for (Object arg : rho.rho().getArgs()) {
                    rhoRecurseInit(arg, pos);
                }
            }
        }
    }
}
