package supercoder79.rho;

import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.NoiseChunk;

public interface OnceCache {
    boolean isInCache(DensityFunction.FunctionContext ctx);

    double getFromCache();

    double getAndPutInCache(DensityFunction.FunctionContext pos, double value);

    class Impl implements OnceCache {
        private long lastIdx = -1;
        private double lastValue;

        // TODO: figure out how to implement last array
        @Override
        public boolean isInCache(DensityFunction.FunctionContext ctx) {
            if (ctx instanceof NoiseChunk chunk) {
                return chunk.interpolationCounter == this.lastIdx;
            }

            return false;
        }

        @Override
        public double getFromCache() {
            return this.lastValue;
        }

        @Override
        public double getAndPutInCache(DensityFunction.FunctionContext pos, double value) {
            if (pos instanceof NoiseChunk chunk) {
                this.lastIdx = chunk.interpolationCounter;
                this.lastValue = value;
            }
            return value;
        }
    }
}
