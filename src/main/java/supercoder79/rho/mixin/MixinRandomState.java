package supercoder79.rho.mixin;

import net.minecraft.core.HolderGetter;
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

@Mixin(value = RandomState.class, priority = 950)
public class MixinRandomState {
    @Mutable
    @Shadow @Final private NoiseRouter router;

    @Mutable
    @Shadow @Final private Climate.Sampler sampler;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void injectCtor(NoiseGeneratorSettings noiseGeneratorSettings, HolderGetter holderGetter, long l, CallbackInfo ci) {
        if (!RhoCompiler.DO_COMPILE) {
            return;
        }

        NoiseRouter oldRouter = this.router;

        this.router = new NoiseRouter(
//                RhoCompiler.compile("Barrier", oldRouter.barrierNoise()),
//                RhoCompiler.compile("FluidLevelFloodedness", oldRouter.fluidLevelFloodednessNoise()),
//                RhoCompiler.compile("FluidLevelSpread", oldRouter.fluidLevelSpreadNoise()),
//                RhoCompiler.compile("Lava", oldRouter.lavaNoise()),
                oldRouter.barrierNoise(),
                oldRouter.fluidLevelFloodednessNoise(),
                oldRouter.fluidLevelSpreadNoise(),
                oldRouter.lavaNoise(),
//                RhoCompiler.compile("Temperature", oldRouter.temperature()),
//                RhoCompiler.compile("Vegetation", oldRouter.vegetation()),
//                RhoCompiler.compile("Continents", oldRouter.continents()),
//                RhoCompiler.compile("Erosion", oldRouter.erosion()),
//                RhoCompiler.compile("Depth", oldRouter.depth()),
                oldRouter.temperature(),
                oldRouter.vegetation(),
                oldRouter.continents(),
                oldRouter.erosion(),
                oldRouter.depth(),
//                RhoCompiler.compile("Ridges", oldRouter.ridges()),
//                RhoCompiler.compile("InitialDensityWithoutJaggedness", oldRouter.initialDensityWithoutJaggedness()),
                oldRouter.ridges(),
                oldRouter.initialDensityWithoutJaggedness(),
                RhoCompiler.compile("Final", oldRouter.finalDensity()),
//                RhoCompiler.compile("VeinToggle", oldRouter.veinToggle()),
//                RhoCompiler.compile("VeinRidged", oldRouter.veinRidged()),
//                RhoCompiler.compile("VeinGap", oldRouter.veinGap())
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
