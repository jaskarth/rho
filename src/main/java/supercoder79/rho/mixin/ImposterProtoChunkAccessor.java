package supercoder79.rho.mixin;

import net.minecraft.world.level.chunk.ImposterProtoChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ImposterProtoChunk.class)
public interface ImposterProtoChunkAccessor {
    @Accessor
    boolean isAllowWrites();
}
