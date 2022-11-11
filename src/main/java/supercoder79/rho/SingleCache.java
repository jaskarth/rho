package supercoder79.rho;

// TODO: extract this and FlatCache2 out into a single Cache interface
public interface SingleCache {
    boolean isInCache(long pos);

    double getFromCache(long pos);

    double getAndPutInCache(long pos, double value);

    class Threaded implements SingleCache {
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
    }

    class Impl implements SingleCache {
        private long key;
        private double value;


        @Override
        public boolean isInCache(long pos) {
            return key == pos;
        }

        @Override
        public double getFromCache(long pos) {
            return value;
        }

        @Override
        public double getAndPutInCache(long pos, double value) {
            this.value = value;
            this.key = pos;

            return value;
        }
    }
}
