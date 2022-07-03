package supercoder79.rho.mixin;

import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.DensityFunctions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import supercoder79.rho.RhoDensityFunction;

@Mixin(DensityFunctions.class)
public class MixinDensityFunctions {
    @Inject(method = "bootstrap", at = @At("TAIL"))
    private static void bootstrapRho(Registry<Codec<? extends DensityFunction>> registry, CallbackInfoReturnable<Codec<? extends DensityFunction>> cir) {
        Registry.register(registry, new ResourceLocation("rho", "rho"), RhoDensityFunction.CODEC.codec());
    }
}
