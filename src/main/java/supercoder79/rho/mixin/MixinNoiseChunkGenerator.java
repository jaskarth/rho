package supercoder79.rho.mixin;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.QuartPos;
import net.minecraft.core.Registry;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeResolver;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.chunk.*;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import supercoder79.rho.opto.algorithm.SectionData;

import java.util.Optional;

@Mixin(NoiseBasedChunkGenerator.class)
public abstract class MixinNoiseChunkGenerator extends ChunkGenerator {

    public MixinNoiseChunkGenerator(Registry<StructureSet> registry, Optional<HolderSet<StructureSet>> optional, BiomeSource biomeSource) {
        super(registry, optional, biomeSource);
    }

    @Shadow protected abstract NoiseChunk createNoiseChunk(ChunkAccess chunkAccess, StructureManager structureManager, Blender blender, RandomState randomState);

    @Shadow @Final protected Holder<NoiseGeneratorSettings> settings;

    /**
     * @author SuperCoder79
     * @reason Minecraft iterates XYZ but XZY is significantly (~50%) faster
     */
    @Overwrite
    private void doCreateBiomes(Blender blender, RandomState randomState, StructureManager structureManager, ChunkAccess chunk) {
        if (chunk instanceof ImposterProtoChunk imposter) {
            if (!((ImposterProtoChunkAccessor)imposter).isAllowWrites()) {
                // Eject if the imposter doesn't allow writes
                return;
            }

            chunk = imposter.getWrapped();
        }

        NoiseChunk noiseChunk = chunk.getOrCreateNoiseChunk(c -> this.createNoiseChunk(c, structureManager, blender, randomState));
        BiomeResolver biomeResolver = BelowZeroRetrogen.getBiomeResolver(blender.getBiomeResolver(this.biomeSource), chunk);
        Climate.Sampler sampler = ((NoiseChunkAccessor)noiseChunk).callCachedClimateSampler(randomState.router(), (this.settings.value()).spawnTarget());

        ChunkPos chunkPos = chunk.getPos();
        int bX = QuartPos.fromBlock(chunkPos.getMinBlockX());
        int bZ = QuartPos.fromBlock(chunkPos.getMinBlockZ());

        LevelChunkSection[] sections = chunk.getSections();
        SectionData[] data = new SectionData[sections.length];
        for (int i = 0; i < sections.length; i++) {
            LevelChunkSection section = sections[i];

            int minY = QuartPos.fromBlock(section.bottomBlockY());
            PalettedContainer<Holder<Biome>> container = ((LevelChunkSectionAccessor)section).getBiomes().recreate();
            data[i] = new SectionData(section, minY, container);
        }

        // XZ itr
        for (int ax = 0; ax < 4; ax++) {
            for (int az = 0; az < 4; az++) {

                for (SectionData sectionData : data) {
                    int minY = sectionData.minY();
                    PalettedContainer<Holder<Biome>> container = sectionData.container();

                    for (int ay = 0; ay < 4; ay++) {
                        container.getAndSetUnchecked(ax, ay, az, biomeResolver.getNoiseBiome(bX + ax, minY + ay, bZ + az, sampler));
                    }
                }
            }
        }

        for (SectionData sec : data) {
            ((LevelChunkSectionAccessor)sec.section()).setBiomes(sec.container());
        }
    }
}
