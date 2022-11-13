package supercoder79.rho;

public class AquiferItrOrder {
    public static void main(String[] args) {
        for (int y1 = -1; y1 <= 1; ++y1) {
            for(int x1 = 0; x1 <= 1; ++x1) {
                for (int z1 = 0; z1 <= 1; ++z1) {
                    System.out.println("(" + x1 + ", " + y1 + ", " + z1 + ")");
                }
            }
        }

        System.out.println("=====================");
        for (int i = 0; i < 12; i++) {
            int y1 = (i >> 2) - 1;
            int x1 = (i & 3) >> 1;
            int z1 = i & 1;

            System.out.println("(" + x1 + ", " + y1 + ", " + z1 + ")");
        }
    }
}
