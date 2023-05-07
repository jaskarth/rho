package supercoder79.rho;

import net.fabricmc.loader.api.FabricLoader;

public class RemappingClassRefs {

    public static final ClassName CLASS_DENSITY_FUNCTION = new ClassName("net/minecraft/world/level/levelgen/DensityFunction", "net/minecraft/class_6910");
    public static final ClassName CLASS_FUNCTION_CONTEXT = new ClassName("net/minecraft/world/level/levelgen/DensityFunction$FunctionContext", "net/minecraft/class_6910$class_6912");
    public static final ClassName CLASS_CHUNKPOS = new ClassName("net/minecraft/world/level/ChunkPos", "net/minecraft/class_1923");
    public static final ClassName CLASS_MTH = new ClassName("net/minecraft/util/Mth", "net/minecraft/class_3532");
    public static final ClassName CLASS_CUBIC_SPLINE = new ClassName("net/minecraft/util/CubicSpline", "net/minecraft/class_6492");
    public static final ClassName CLASS_SPLINEPOINT = new ClassName("net/minecraft/world/level/levelgen/DensityFunctions$Spline$Point", "net/minecraft/class_6916$class_7076$class_7136");
    public static final ClassName CLASS_SIMPLEXNOISE = new ClassName("net/minecraft/world/level/levelgen/synth/SimplexNoise", "net/minecraft/class_3541");
    public static final ClassName CLASS_NORMALNOISE = new ClassName("net/minecraft/world/level/levelgen/synth/NormalNoise", "net/minecraft/class_5216");


    public static final MethodName METHOD_CHUNKPOS_ASLONG = new MethodName("asLong", "method_8331", "net/minecraft/class_1923", "(II)J");

    public static final MethodName METHOD_FUNCTION_CONTEXT_BLOCKX = new MethodName("blockX", "comp_371", "net/minecraft/class_6910$class_6912", "()I");
    public static final MethodName METHOD_FUNCTION_CONTEXT_BLOCKY = new MethodName("blockY", "comp_372", "net/minecraft/class_6910$class_6912", "()I");
    public static final MethodName METHOD_FUNCTION_CONTEXT_BLOCKZ = new MethodName("blockZ", "comp_373", "net/minecraft/class_6910$class_6912", "()I");

    public static final MethodName METHOD_MTH_CLAMPEDMAP = new MethodName("clampedMap", "method_32854", "net/minecraft/class_3532", "(DDDDD)D");
    public static final MethodName METHOD_SPLINE_APPLY = new MethodName("apply", "method_41296", "net/minecraft/class_6501", "(Ljava/lang/Object;)F");
    public static final MethodName METHOD_NORMALNOISE_GETVALUE = new MethodName("getValue", "method_27406", "net/minecraft/class_5216", "(DDD)D");
    public static final MethodName METHOD_DENSITY_FUNC_COMPUTE = new MethodName("compute", "method_40464", "net/minecraft/class_6910", "(Lnet/minecraft/class_6910$class_6912;)D");


    public static String remap(ClassName className) {
        // Hack for standalone test
        if (!RhoCompiler.DO_COMPILE) {
            return className.dev;
        }

        return FabricLoader.getInstance().getMappingResolver().mapClassName("intermediary", className.intermediary.replace('/', '.')).replace('.', '/');
    }

    public static String remap(MethodName methodName) {
        // Hack for standalone test
        if (!RhoCompiler.DO_COMPILE) {
            return methodName.dev;
        }

        return FabricLoader.getInstance().getMappingResolver().mapMethodName("intermediary", methodName.intermediaryOwner.replace('/', '.'), methodName.intermediary, methodName.intermediaryDesc);
    }

    public record ClassName(String dev, String intermediary) {
        public String get() {
            return RemappingClassRefs.remap(this);
        }

        public String getAsDescriptor() {
            return ClassRefs.descriptor(get());
        }
    }

    public record MethodName(String dev, String intermediary, String intermediaryOwner, String intermediaryDesc) {
        public String get() {
            return RemappingClassRefs.remap(this);
        }

    }

}
