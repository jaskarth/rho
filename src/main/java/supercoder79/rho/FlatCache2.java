package supercoder79.rho;

import net.minecraft.core.QuartPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.NoiseChunk;
import supercoder79.rho.mixin.NoiseChunkAccessor;

public interface FlatCache2 {

    boolean isInCache(DensityFunction.FunctionContext ctx);

    double getFromCache(DensityFunction.FunctionContext ctx);

//    double getAndPutInCache(long pos, double value);

    void init(ChunkPos pos);

    DensityFunction getNoiseFiller();


    class Impl implements FlatCache2 {

        private final int hashCode;
        private final DensityFunction noiseFiller;
        private final int startX;
        private final int startZ;
        private final int noiseSizeXZ;
        private final double[] cache;

        public Impl(int hashCode, DensityFunction noiseFiller, NoiseChunk noiseChunk) {
            this.hashCode = hashCode;
            this.noiseFiller = noiseFiller;
            this.startX = ((NoiseChunkAccessor) noiseChunk).getFirstNoiseX();
            this.startZ = ((NoiseChunkAccessor) noiseChunk).getFirstNoiseZ();
            this.noiseSizeXZ = ((NoiseChunkAccessor) noiseChunk).getNoiseSizeXZ() + 1;
            this.cache = new double[noiseSizeXZ * noiseSizeXZ];
            int index = 0;
            for(int i = 0; i < this.noiseSizeXZ; ++i) {
                int x = QuartPos.toBlock(this.startX + i);
                for(int l = 0; l < this.noiseSizeXZ; ++l) {
                    int z = QuartPos.toBlock(this.startZ + l);
//                    this.values[i][l] = densityFunction.compute(new DensityFunction.SinglePointContext(x, 0, z));
                    this.cache[index ++] = noiseFiller.compute(new DensityFunction.SinglePointContext(x, 0, z));
                }
            }
        }

        @Override
        public boolean isInCache(DensityFunction.FunctionContext ctx) {
            int x = QuartPos.fromBlock(ctx.blockX()) - startX;
            int z = QuartPos.fromBlock(ctx.blockZ()) - startZ;
            return x >= 0 && x < noiseSizeXZ && z >= 0 && z < noiseSizeXZ;
        }

        @Override
        public double getFromCache(DensityFunction.FunctionContext ctx) {
            int x = QuartPos.fromBlock(ctx.blockX()) - startX;
            int z = QuartPos.fromBlock(ctx.blockZ()) - startZ;
            return this.cache[x * this.noiseSizeXZ + z];
        }

        public void init(ChunkPos pos) {
        }

        @Override
        public int hashCode() {
            return hashCode;
        }

        @Override
        public DensityFunction getNoiseFiller() {
            return noiseFiller;
        }

//        static {
//            Thread t = new Thread(() -> {
//                while (true) {
//                    try {
//                        Thread.sleep(4000);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//
//                    System.out.println("Hits: " + hits + " Misses: " + misses);
//                }
//            });
//
//            t.setDaemon(true);
//            t.start();
//        }
    }

    class Noop implements FlatCache2 {
        private final int hashCode;
        private final DensityFunction noiseFiller;

        public Noop(int hashCode, DensityFunction noiseFiller) {
            this.hashCode = hashCode;
            this.noiseFiller = noiseFiller;
        }

        @Override
        public boolean isInCache(DensityFunction.FunctionContext ctx) {
            return false;
        }

        @Override
        public double getFromCache(DensityFunction.FunctionContext ctx) {
            return 0;
        }

//        @Override
//        public double getAndPutInCache(long pos, double value) {
//            return value;
//        }

        @Override
        public void init(ChunkPos pos) {

        }

        @Override
        public int hashCode() {
            return hashCode;
        }

        @Override
        public DensityFunction getNoiseFiller() {
            return noiseFiller;
        }
    }
}
