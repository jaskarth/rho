package supercoder79.rho.mixin;

import net.minecraft.core.*;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;
import net.minecraft.world.level.levelgen.structure.structures.NetherFossilPieces;
import net.minecraft.world.level.levelgen.structure.structures.NetherFossilStructure;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Optional;
import java.util.function.Predicate;

@Mixin(NetherFossilStructure.class)
public abstract class MixinNetherFossilStructure extends Structure {

    protected MixinNetherFossilStructure(StructureSettings structureSettings) {
        super(structureSettings);
    }

    @Shadow public abstract Optional<Structure.GenerationStub> findGenerationPoint(Structure.GenerationContext generationContext);

    @Shadow @Final public HeightProvider height;

    @Override
    public StructureStart generate(
            RegistryAccess registryAccess,
            ChunkGenerator chunkGenerator,
            BiomeSource biomeSource,
            RandomState randomState,
            StructureTemplateManager structureTemplateManager,
            long l,
            ChunkPos chunkPos,
            int i,
            LevelHeightAccessor levelHeightAccessor,
            Predicate<Holder<Biome>> predicate
    ) {
        Structure.GenerationContext generationContext = new Structure.GenerationContext(
                registryAccess, chunkGenerator, biomeSource, randomState, structureTemplateManager, l, chunkPos, levelHeightAccessor, predicate
        );

        WorldgenRandom worldgenRandom = generationContext.random();
        int x = generationContext.chunkPos().getMinBlockX() + worldgenRandom.nextInt(16);
        int z = generationContext.chunkPos().getMinBlockZ() + worldgenRandom.nextInt(16);
        int seaLevel = generationContext.chunkGenerator().getSeaLevel();
        WorldGenerationContext worldGenerationContext = new WorldGenerationContext(generationContext.chunkGenerator(), generationContext.heightAccessor());
        int y = this.height.sample(worldgenRandom, worldGenerationContext);

        if (isValidBiomeRho(new BlockPos(x, y, z), chunkGenerator, randomState, predicate)) {
            Optional<Structure.GenerationStub> optional = this.findGenerationPointRho(
                    x, y, z, seaLevel, worldgenRandom, generationContext
            );

            if (optional.isPresent() && isValidBiomeRho(optional.get().position(), chunkGenerator, randomState, predicate)) {
                StructurePiecesBuilder structurePiecesBuilder = (optional.get()).getPiecesBuilder();
                StructureStart structureStart = new StructureStart(this, chunkPos, i, structurePiecesBuilder.build());
                if (structureStart.isValid()) {
                    return structureStart;
                }
            }
        }

        return StructureStart.INVALID_START;
    }

    private static boolean isValidBiomeRho(
            BlockPos blockPos, ChunkGenerator chunkGenerator, RandomState randomState, Predicate<Holder<Biome>> predicate
    ) {
        return predicate.test(
                chunkGenerator.getBiomeSource()
                        .getNoiseBiome(QuartPos.fromBlock(blockPos.getX()), QuartPos.fromBlock(blockPos.getY()), QuartPos.fromBlock(blockPos.getZ()), randomState.sampler())
        );
    }

    private Optional<Structure.GenerationStub> findGenerationPointRho(int i, int l, int j, int k, WorldgenRandom random, Structure.GenerationContext generationContext) {
        NoiseColumn noiseColumn = generationContext.chunkGenerator().getBaseColumn(i, j, generationContext.heightAccessor(), generationContext.randomState());
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos(i, l, j);

        while(l > k) {
            BlockState blockState = noiseColumn.getBlock(l);
            BlockState blockState2 = noiseColumn.getBlock(--l);
            if (blockState.isAir() && (blockState2.is(Blocks.SOUL_SAND) || blockState2.isFaceSturdy(EmptyBlockGetter.INSTANCE, mutableBlockPos.setY(l), Direction.UP))) {
                break;
            }
        }

        if (l <= k) {
            return Optional.empty();
        } else {
            BlockPos blockPos = new BlockPos(i, l, j);
            return Optional.of(
                    new Structure.GenerationStub(
                            blockPos,
                            structurePiecesBuilder -> NetherFossilPieces.addPieces(generationContext.structureTemplateManager(), structurePiecesBuilder, random, blockPos)
                    )
            );
        }
    }
}
