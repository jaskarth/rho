package supercoder79.rho.test;

import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.client.gui.screens.worldselection.WorldCreationContext;
import net.minecraft.commands.Commands;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.Bootstrap;
import net.minecraft.server.RegistryLayer;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.WorldLoader;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.ServerPacksSource;
import net.minecraft.server.packs.resources.CloseableResourceManager;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.NoiseRouter;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import supercoder79.rho.RhoClass;
import supercoder79.rho.RhoCompiler;
import supercoder79.rho.RhoDensityFunction;
import supercoder79.rho.gen.DotExporter;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class TestStandalone {

    private static RegistryAccess.Frozen getRegistryManager() {
        record WorldCreationSettings(WorldGenSettings worldGenSettings, WorldDataConfiguration dataConfiguration) {
        }

        PackRepository resourcePackManager = new PackRepository(new ServerPacksSource());
        WorldLoader.PackConfig dataPacks = new WorldLoader.PackConfig(resourcePackManager, WorldDataConfiguration.DEFAULT, false, true);
        WorldLoader.InitConfig serverConfig = new WorldLoader.InitConfig(dataPacks, Commands.CommandSelection.DEDICATED, 2);
        final ExecutorService service = Executors.newSingleThreadExecutor();
        CompletableFuture<WorldCreationContext> completableFuture = WorldLoader.load(
                serverConfig,
                context -> new WorldLoader.DataLoadOutput<>(
                        new WorldCreationSettings(
                                new WorldGenSettings(WorldOptions.defaultWithRandomSeed(), WorldPresets.createNormalWorldDimensions(context.datapackWorldgen())), context.dataConfiguration()
                        ),
                        context.datapackDimensions()
                ),
                // DO NOT CONVERT THIS TO LAMBDA DUE TO FREEZES
                new WorldLoader.ResultFactory<WorldCreationSettings, WorldCreationContext>() {
                    @Override
                    public WorldCreationContext create(CloseableResourceManager resourceManager, ReloadableServerResources dataPackContents, LayeredRegistryAccess<RegistryLayer> combinedDynamicRegistries, WorldCreationSettings generatorOptions) {
                        resourceManager.close();
                        return new WorldCreationContext(generatorOptions.worldGenSettings(), combinedDynamicRegistries, dataPackContents, generatorOptions.dataConfiguration());
                    }
                },
                Util.backgroundExecutor(),
                service
        );
        final WorldCreationContext holder = completableFuture.join();
        service.shutdown();
        return holder.worldgenLoadContext();
    }

    public static void main(String[] args) {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();

        RhoCompiler.DO_COMPILE = false;
//        RhoCompiler.COMPILE_RECURSIVELY = false;

        final RegistryAccess.Frozen registryManager = getRegistryManager();
        NoiseRouter router = registryManager.registryOrThrow(Registries.NOISE_SETTINGS).get(NoiseGeneratorSettings.OVERWORLD).noiseRouter();
//        RandomState randomState = RandomState.create(RegistryAccess.builtinCopy(), NoiseGeneratorSettings.OVERWORLD, 200);
//        NoiseRouter router = randomState.router();
//        System.out.println(router.ridges());
        DensityFunction func = router.finalDensity();

        RhoClass compiled = RhoCompiler.compile(func);
        DensityFunction rfunc = new RhoDensityFunction(compiled);

        for (int i = 0; i < 10; i++) {
            long start = System.currentTimeMillis();
            double sum = 0;
            for (int x = 0; x < 100; x++) {
                for (int z = 0; z < 100; z++) {
                    for (int y = 0; y < 100; y++) {
                        sum += rfunc.compute(new DensityFunction.SinglePointContext(x, y, z));
                    }
                }
            }
            long end = System.currentTimeMillis();

            System.out.println("Rho Took: " + (end - start) + " ms");

            System.out.println(sum);

            start = System.currentTimeMillis();
            sum = 0;
            for (int x = 0; x < 100; x++) {
                for (int z = 0; z < 100; z++) {
                    for (int y = 0; y < 100; y++) {
                        sum += func.compute(new DensityFunction.SinglePointContext(x, y, z));
                    }
                }
            }
            end = System.currentTimeMillis();

            System.out.println("Vanilla Took: " + (end - start) + " ms");

            System.out.println(sum);
        }
    }
}
