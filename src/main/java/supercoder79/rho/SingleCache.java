package supercoder79.rho;

// TODO: extract this and FlatCache2 out into a single Cache interface
public interface SingleCache {
    boolean isInCache(long pos);

    double getFromCache(long pos);

    double getAndPutInCache(long pos, double value);

    class Impl implements SingleCache {
        private final int hashCode;

        private long key;
        private double value;

        public Impl(int hashCode) {
            this.hashCode = hashCode;
        }

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

        @Override
        public int hashCode() {
            return hashCode;
        }
    }
    class Noop implements SingleCache {
        private final int hashCode;

        public Noop(int hashCode) {
            this.hashCode = hashCode;
        }

        @Override
        public boolean isInCache(long pos) {
            return false;
        }

        @Override
        public double getFromCache(long pos) {
            return 0;
        }

        @Override
        public double getAndPutInCache(long pos, double value) {
            return value;
        }

        @Override
        public int hashCode() {
            return hashCode;
        }
    }
}
