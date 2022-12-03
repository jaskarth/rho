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
        private static final int EXTRA_RAD = 4;
        private static final int SIZE = 5 + (EXTRA_RAD * 2);
        private static final int SIZE2 = SIZE * SIZE;
        private static final double[] REF = new double[SIZE2];
        static {
            Arrays.fill(REF, 0.0);
        }

        private final double[] cache = new double[SIZE2];
        private final BitSet initialized = new BitSet(SIZE2);

        private int startX;
        private int startZ;

        private static int hits = 0;
        private static int misses = 0;

        public void init(ChunkPos pos) {
            this.startX = (pos.x << 2) - EXTRA_RAD;
            this.startZ = (pos.z << 2) - EXTRA_RAD;
            this.initialized.clear();
            System.arraycopy(REF, 0, this.cache, 0, SIZE2);
        }

        public boolean isInCache(long pos) {
            int idx = getIndex(pos);

            return idx >= 0 && idx < SIZE2 && this.initialized.get(idx);
        }

        @Override
        public double getFromCache(long pos) {
//            hits++;
            return cache[getIndex(pos)];
        }

        @Override
        public double getAndPutInCache(long pos, double value) {
            int idx = getIndex(pos);
//            misses++;

            if (idx >= 0 && idx < SIZE2) {
                cache[idx] = value;
                initialized.set(idx);
            } else {
                boolean bl = false;
            }

            return value;
        }

        private int getIndex(long pos) {
            int x = (ChunkPos.getX(pos) >> 2) - this.startX;
            int z = (ChunkPos.getZ(pos) >> 2) - this.startZ;

            return x * SIZE + z;
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
}
