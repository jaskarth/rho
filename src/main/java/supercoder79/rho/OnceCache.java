package supercoder79.rho;

import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.NoiseChunk;
import org.jetbrains.annotations.Nullable;
import supercoder79.rho.mixin.NoiseChunkAccessor;

public interface OnceCache {
    boolean isInCache(DensityFunction.FunctionContext ctx);

    double getFromCache();

    double getAndPutInCache(DensityFunction.FunctionContext pos, double value);

    class Impl implements OnceCache {
        private final int hashCode;
        private final NoiseChunk this$0;
        private long lastIdx = -1;
        private double lastValue;
        private long lastArrayCounter;
        private double[] lastArray;

        public Impl(int hashCode, NoiseChunk this$0) {
            this.hashCode = hashCode;
            this.this$0 = this$0;
        }

        // TODO: figure out how to implement last array
        @Override
        public boolean isInCache(DensityFunction.FunctionContext ctx) {
            if (ctx == this$0 && ctx instanceof NoiseChunk chunk) {
                return (this.lastArray != null && this.lastArrayCounter == ((NoiseChunkAccessor) chunk).getArrayInterpolationCounter()) ||
                        chunk.interpolationCounter == this.lastIdx;
            }

            return false;
        }

        @Override
        public double getFromCache() {
            if (this.lastArray != null && this.lastArrayCounter == ((NoiseChunkAccessor) this$0).getArrayInterpolationCounter()) {
                return this.lastArray[((NoiseChunkAccessor) this$0).getArrayIndex()];
            } else {
                return this.lastValue;
            }
        }

        @Override
        public double getAndPutInCache(DensityFunction.FunctionContext pos, double value) {
            if (pos instanceof NoiseChunk chunk) {
                this.lastIdx = chunk.interpolationCounter;
                this.lastValue = value;
                if (this.lastArray == null) {
                    final int length1 = ((NoiseChunkAccessor) chunk).getCellWidth() * ((NoiseChunkAccessor) chunk).getCellWidth() * ((NoiseChunkAccessor) chunk).getCellHeight();
                    final int length2 = ((NoiseChunkAccessor) chunk).getCellCountY() + 1;
                    this.lastArray = new double[Math.max(length1, length2)];
                }
                this.lastArrayCounter = ((NoiseChunkAccessor) chunk).getArrayInterpolationCounter();
                this.lastArray[((NoiseChunkAccessor) chunk).getArrayIndex()] = value;
            }
            return value;
        }

        @Override
        public int hashCode() {
            return hashCode;
        }
    }

    class Noop implements OnceCache {
        private final int hashCode;

        public Noop(int hashCode) {
            this.hashCode = hashCode;
        }

        @Override
        public boolean isInCache(DensityFunction.FunctionContext ctx) {
            return false;
        }

        @Override
        public double getFromCache() {
            return 0;
        }

        @Override
        public double getAndPutInCache(DensityFunction.FunctionContext pos, double value) {
            return value;
        }

        @Override
        public int hashCode() {
            return hashCode;
        }
    }
}
