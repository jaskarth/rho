package supercoder79.rho.mixin;

import net.minecraft.core.Registry;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.NoiseRouter;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import supercoder79.rho.RhoCompiler;
import supercoder79.rho.RhoDensityFunction;

@Mixin(RandomState.class)
public class MixinRandomState {
    @Mutable
    @Shadow @Final private NoiseRouter router;

    @Mutable
    @Shadow @Final private Climate.Sampler sampler;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void injectCtor(NoiseGeneratorSettings noiseGeneratorSettings, Registry<NormalNoise.NoiseParameters> registry, long l, CallbackInfo ci) {
        if (!RhoCompiler.DO_COMPILE) {
            return;
        }

        NoiseRouter oldRouter = this.router;

        this.router = new NoiseRouter(
                oldRouter.barrierNoise(),
                oldRouter.fluidLevelFloodednessNoise(),
                oldRouter.fluidLevelSpreadNoise(),
                oldRouter.lavaNoise(),
                ////
//                oldRouter.temperature(),
//                oldRouter.vegetation(),
//                oldRouter.continents(),
//                oldRouter.erosion(),
//                oldRouter.depth(),
//                oldRouter.ridges(),
                    ////
                new RhoDensityFunction(
                        RhoCompiler.compile("Temp", oldRouter.temperature())
                ),
                new RhoDensityFunction(
                        RhoCompiler.compile("Vegetation", oldRouter.vegetation())
                ),
                new RhoDensityFunction(
                        RhoCompiler.compile("Continents", oldRouter.continents())
                ),
                new RhoDensityFunction(
                        RhoCompiler.compile("Erosion", oldRouter.erosion())
                ),
                new RhoDensityFunction(
                        RhoCompiler.compile("Depth", oldRouter.depth())
                ),
                new RhoDensityFunction(
                        RhoCompiler.compile("Ridges", oldRouter.ridges())
                ),
                // Initial density
                new RhoDensityFunction(
                        RhoCompiler.compile("InitialDensity", oldRouter.initialDensityWithoutJaggedness())
                ),
                oldRouter.finalDensity(),
                oldRouter.veinToggle(),
                oldRouter.veinRidged(),
                oldRouter.veinGap()
        );

        this.sampler = new Climate.Sampler(
                this.router.temperature(),
                this.router.vegetation(),
                this.router.continents(),
                this.router.erosion(),
                this.router.depth(),
                this.router.ridges(),
                noiseGeneratorSettings.spawnTarget()
        );
    }
}
