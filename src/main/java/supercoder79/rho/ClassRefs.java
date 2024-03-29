package supercoder79.rho;

public class ClassRefs {
    public static final String VOID = "V";
    public static final String INT = "I";
    public static final String DOUBLE = "D";
    public static final String LONG = "J";
    public static final String BOOLEAN = "Z";

    public static final String LIST = "java/util/List";
    public static final String OBJECT = "java/lang/Object";
    public static final String MATH = "java/lang/Math";

    public static final String RHO_CLASS = "supercoder79/rho/RhoClass";
    public static final String FLAT_CACHE_2 = "supercoder79/rho/FlatCache2";
    public static final String DENSITY_SUPPORT = "supercoder79/rho/DensitySupport";

    public static String descriptor(String name) {
        if (name.length() == 1) {
            return name;
        }

        return "L" + name + ";";
    }

    public static String methodDescriptor(String ret, String... args) {
        StringBuilder builder = new StringBuilder("(");

        for (String arg : args) {
            builder.append(descriptor(arg));
        }

        builder.append(")");
        builder.append(descriptor(ret));

        return builder.toString();
    }
}
