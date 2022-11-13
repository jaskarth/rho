package supercoder79.rho.mixin;

import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeResolver;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.PalettedContainer;
import net.minecraft.world.level.chunk.PalettedContainerRO;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(LevelChunkSection.class)
public abstract class MixinLevelChunkSection {
    @Shadow private PalettedContainerRO<Holder<Biome>> biomes;

    @Shadow public abstract int bottomBlockY();

    /**
     * @author SuperCoder79
     * @reason Minecraft iterates XYZ but XZY is significantly (~50%) faster
     */
    @Overwrite
    public void fillBiomesFromNoise(BiomeResolver biomeResolver, Climate.Sampler sampler, int i, int j) {
        PalettedContainer<Holder<Biome>> palettedContainer = this.biomes.recreate();
        int k = QuartPos.fromBlock(this.bottomBlockY());
        int l = 4;

        for(int m = 0; m < 4; ++m) {
            for(int o = 0; o < 4; ++o) {
                for(int n = 0; n < 4; ++n) {
                    palettedContainer.getAndSetUnchecked(m, n, o, biomeResolver.getNoiseBiome(i + m, k + n, j + o, sampler));
                }
            }
        }

        this.biomes = palettedContainer;
    }
}
