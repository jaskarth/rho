package supercoder79.rho;

import net.fabricmc.loader.api.FabricLoader;

public class RemappingClassRefs {

    public static final Name CLASS_DENSITY_FUNCTION = new Name("net/minecraft/world/level/levelgen/DensityFunction", "net/minecraft/class_6910");
    public static final Name CLASS_FUNCTION_CONTEXT = new Name("net/minecraft/world/level/levelgen/DensityFunction$FunctionContext", "net/minecraft/class_6910$class_6912");
    public static final Name CLASS_CHUNKPOS = new Name("net/minecraft/world/level/ChunkPos", "net/minecraft/class_1923");
    public static final Name CLASS_MTH = new Name("net/minecraft/util/Mth", "net/minecraft/class_3532");
    public static final Name CLASS_CUBIC_SPLINE = new Name("net/minecraft/util/CubicSpline", "net/minecraft/class_6492");
    public static final Name CLASS_SPLINEPOINT = new Name("net/minecraft/world/level/levelgen/DensityFunctions$Spline$Point", "net/minecraft/class_6916$class_7076$class_7136");
    public static final Name CLASS_SIMPLEXNOISE = new Name("net/minecraft/world/level/levelgen/synth/SimplexNoise", "net/minecraft/class_3541");
    public static final Name CLASS_NORMALNOISE = new Name("net/minecraft/world/level/levelgen/synth/NormalNoise", "net/minecraft/class_5216");


    public static final Name METHOD_CHUNKPOS_ASLONG = new Name("asLong", "method_8331");

    public static final Name METHOD_FUNCTION_CONTEXT_BLOCKX = new Name("blockX", "comp_371");
    public static final Name METHOD_FUNCTION_CONTEXT_BLOCKY = new Name("blockY", "comp_372");
    public static final Name METHOD_FUNCTION_CONTEXT_BLOCKZ = new Name("blockZ", "comp_373");

    public static final Name METHOD_MTH_CLAMPEDMAP = new Name("clampedMap", "method_32854");
    public static final Name METHOD_SPLINE_APPLY = new Name("apply", "method_41296");
    public static final Name METHOD_NORMALNOISE_GETVALUE = new Name("getValue", "method_27406");
    public static final Name METHOD_DENSITY_FUNC_COMPUTE = new Name("compute", "method_40464");


    public static String remap(String dev, String intermediary) {
        // Hack for standalone test
        if (!RhoCompiler.DO_COMPILE) {
            return dev;
        }

        return FabricLoader.getInstance().isDevelopmentEnvironment() ? dev : intermediary;
    }

    public record Name(String dev, String intermediary) {
        public String get() {
            return RemappingClassRefs.remap(dev, intermediary);
        }

        public String getAsDescriptor() {
            return ClassRefs.descriptor(get());
        }
    }
}
