package supercoder79.rho;

import net.minecraft.world.level.ChunkPos;

import java.util.Arrays;
import java.util.BitSet;

public interface FlatCache2 {

    boolean isInCache(long pos);

    double getFromCache(long pos);

    double getAndPutInCache(long pos, double value);

    void init(ChunkPos pos);

    class Threaded implements FlatCache2 {
        private final ThreadLocal<Impl> impl = ThreadLocal.withInitial(Impl::new);

        public Threaded() {

        }

        @Override
        public boolean isInCache(long pos) {
            return impl.get().isInCache(pos);
        }

        @Override
        public double getFromCache(long pos) {
            return impl.get().getFromCache(pos);
        }

        @Override
        public double getAndPutInCache(long pos, double value) {
            return impl.get().getAndPutInCache(pos, value);
        }

        @Override
        public void init(ChunkPos pos) {
            impl.get().init(pos);
        }
    }


    class Impl implements FlatCache2 {
        private static final double[] REF = new double[5 * 5];
        static {
            Arrays.fill(REF, 0.0);
        }

        private final double[] cache = new double[5 * 5];
        private final BitSet initialized = new BitSet(5 * 5);

        private int startX;
        private int startZ;

        public void init(ChunkPos pos) {
            this.startX = pos.x << 2;
            this.startZ = pos.z << 2;
            this.initialized.clear();
            System.arraycopy(REF, 0, this.cache, 0, 5 * 5);
        }

        public boolean isInCache(long pos) {
            int idx = getIndex(pos);

            return idx >= 0 && idx < 25 && this.initialized.get(idx);
        }

        @Override
        public double getFromCache(long pos) {
            return cache[getIndex(pos)];
        }

        @Override
        public double getAndPutInCache(long pos, double value) {
            int idx = getIndex(pos);

            if (idx >= 0 && idx < 25) {
                cache[idx] = value;
                initialized.set(idx);
            }

            return value;
        }

        private int getIndex(long pos) {
            int x = (ChunkPos.getX(pos) >> 2) - this.startX;
            int z = (ChunkPos.getZ(pos) >> 2) - this.startZ;

            return x * 5 + z;
        }
    }
}
