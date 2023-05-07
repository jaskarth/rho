package supercoder79.rho.mixin;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.util.CubicSpline;
import net.minecraft.util.ToFloatFunction;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.*;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import supercoder79.rho.FlatCache2;
import supercoder79.rho.OnceCache;
import supercoder79.rho.RhoCacheAwareVisitor;
import supercoder79.rho.RhoDensityFunction;
import supercoder79.rho.RhoSplineCoord;
import supercoder79.rho.SingleCache;

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

    @ModifyArg(method = "<init>",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/levelgen/DensityFunction;mapAll(Lnet/minecraft/world/level/levelgen/DensityFunction$Visitor;)Lnet/minecraft/world/level/levelgen/DensityFunction;"))
    private DensityFunction.Visitor modifyVisitor1(DensityFunction.Visitor visitor) {
        return rho$modifyVisitor0(visitor);
    }

    @ModifyArg(method = "<init>",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/levelgen/NoiseRouter;mapAll(Lnet/minecraft/world/level/levelgen/DensityFunction$Visitor;)Lnet/minecraft/world/level/levelgen/NoiseRouter;"))
    private DensityFunction.Visitor modifyVisitor2(DensityFunction.Visitor visitor) {
        return rho$modifyVisitor0(visitor);
    }

    @ModifyArg(method = "cachedClimateSampler",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/levelgen/DensityFunction;mapAll(Lnet/minecraft/world/level/levelgen/DensityFunction$Visitor;)Lnet/minecraft/world/level/levelgen/DensityFunction;"))
    private DensityFunction.Visitor modifyVisitor3(DensityFunction.Visitor visitor) {
        return rho$modifyVisitor0(visitor);
    }
    private final Int2ObjectOpenHashMap<Object> rho$cache = new Int2ObjectOpenHashMap<>();

    @Unique
    private DensityFunction.Visitor rho$modifyVisitor0(DensityFunction.Visitor visitor) {
        NoiseChunk thiz = (NoiseChunk) (Object) this;
        return new RhoCacheAwareVisitor() {
            @Override
            public Object rho$visitCache(Object node) {
                if (node instanceof SingleCache) {
                    return rho$cache.computeIfAbsent(node.hashCode(), SingleCache.Impl::new);
                } else if (node instanceof OnceCache) {
                    return rho$cache.computeIfAbsent(node.hashCode(), hashCode -> new OnceCache.Impl(hashCode, thiz));
                } else if (node instanceof FlatCache2 flatCache2) {
                    return rho$cache.computeIfAbsent(node.hashCode(), hashCode -> new FlatCache2.Impl(hashCode, visitor.apply(flatCache2.getNoiseFiller().mapAll(visitor)), thiz));
                } else {
                    return node;
                }
            }

            @Override
            public DensityFunction apply(DensityFunction densityFunction) {
                return visitor.apply(densityFunction);
            }
        };
    }

}
