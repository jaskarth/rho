package supercoder79.rho.test;

import net.minecraft.SharedConstants;
import net.minecraft.core.RegistryAccess;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.level.levelgen.*;
import supercoder79.rho.RhoClass;
import supercoder79.rho.RhoCompiler;
import supercoder79.rho.RhoDensityFunction;

import java.util.ArrayList;

public final class TestStandalone {
    public static void main(String[] args) {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();

        RhoCompiler.DO_COMPILE = false;

        NoiseRouter router = BuiltinRegistries.NOISE_GENERATOR_SETTINGS.get(NoiseGeneratorSettings.OVERWORLD).noiseRouter();
//        RandomState randomState = RandomState.create(RegistryAccess.builtinCopy(), NoiseGeneratorSettings.OVERWORLD, 200);
//        NoiseRouter router = randomState.router();
//        System.out.println(router.ridges());
        DensityFunction func = router.continents();

        RhoClass compiled = RhoCompiler.compile(func);
        compiled.makeNew(new ArrayList());
        DensityFunction rfunc = new RhoDensityFunction(compiled);

        for (int i = 0; i < 10; i++) {
            long start = System.currentTimeMillis();
            double sum = 0;
            for (int x = 0; x < 1000; x++) {
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
            for (int x = 0; x < 1000; x++) {
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
