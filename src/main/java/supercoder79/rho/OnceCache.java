package supercoder79.rho;

import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.NoiseChunk;
import org.jetbrains.annotations.Nullable;
import supercoder79.rho.mixin.NoiseChunkAccessor;

import java.util.Arrays;
import java.util.BitSet;

public interface OnceCache {
    boolean isInCache(DensityFunction.FunctionContext ctx);

    double getFromCache();

    double getAndPutInCache(DensityFunction.FunctionContext pos, double value);

    class Impl implements OnceCache {
        private final int hashCode;
        private final NoiseChunk this$0;
        private long lastIdx = -1;
        private double lastValue;
        private long lastArrayCounter = -1;
        private final double[] lastArray;
        private final BitSet lastArrayUsed;

        public Impl(int hashCode, NoiseChunk this$0) {
            this.hashCode = hashCode;
            this.this$0 = this$0;
            final int length1 = ((NoiseChunkAccessor) this$0).getCellWidth() * ((NoiseChunkAccessor) this$0).getCellWidth() * ((NoiseChunkAccessor) this$0).getCellHeight();
            final int length2 = ((NoiseChunkAccessor) this$0).getCellCountY() + 1;
            this.lastArray = new double[Math.max(length1, length2)];
            this.lastArrayUsed = new BitSet(this.lastArray.length);
        }

        @Override
        public boolean isInCache(DensityFunction.FunctionContext ctx) {
            return ctx == this$0 &&
                   (
                           (this.lastArrayCounter == ((NoiseChunkAccessor) this$0).getArrayInterpolationCounter() && this.lastArrayUsed.get(((NoiseChunkAccessor) this$0).getArrayIndex())) ||
                           this$0.interpolationCounter == this.lastIdx
                   );

        }

        @Override
        public double getFromCache() {
            if (this.lastArrayCounter == ((NoiseChunkAccessor) this$0).getArrayInterpolationCounter()) {
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
                final long arrayInterpolationCounter = ((NoiseChunkAccessor) chunk).getArrayInterpolationCounter();
                if (arrayInterpolationCounter != this.lastArrayCounter) {
                    Arrays.fill(this.lastArray, 0);
                    this.lastArrayUsed.clear();
                    this.lastArrayCounter = arrayInterpolationCounter;
                }
                final int arrayIndex = ((NoiseChunkAccessor) chunk).getArrayIndex();
                this.lastArray[arrayIndex] = value;
                this.lastArrayUsed.set(arrayIndex);
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
