package supercoder79.rho.mixin;

import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.PalettedContainerRO;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LevelChunkSection.class)
public interface LevelChunkSectionAccessor {
    @Accessor
    PalettedContainerRO<Holder<Biome>> getBiomes();

    @Accessor
    void setBiomes(PalettedContainerRO<Holder<Biome>> biomes);
}
