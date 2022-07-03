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
        NoiseRouter oldRouter = this.router;

        this.router = new NoiseRouter(
                oldRouter.barrierNoise(),
                oldRouter.fluidLevelFloodednessNoise(),
                oldRouter.fluidLevelSpreadNoise(),
                oldRouter.lavaNoise(),
                oldRouter.temperature(),
                oldRouter.vegetation(),
                oldRouter.continents(),
                oldRouter.erosion(),
                new RhoDensityFunction(
                        RhoCompiler.compile(oldRouter.depth())
                ),
                new RhoDensityFunction(
                        RhoCompiler.compile(oldRouter.ridges())
                ),
                // Initial density
                new RhoDensityFunction(
                        RhoCompiler.compile(oldRouter.initialDensityWithoutJaggedness())
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
