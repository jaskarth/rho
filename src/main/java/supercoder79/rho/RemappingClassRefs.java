package supercoder79.rho;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.MappingResolver;

public class RemappingClassRefs {

    public static String remap(String dev, String intermediary) {
        return FabricLoader.getInstance().isDevelopmentEnvironment() ? dev : intermediary;
    }
}
