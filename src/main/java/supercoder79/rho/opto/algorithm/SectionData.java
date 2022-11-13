package supercoder79.rho.opto.algorithm;

import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.PalettedContainer;

public record SectionData(LevelChunkSection section, int minY, PalettedContainer<Holder<Biome>> container) {

}