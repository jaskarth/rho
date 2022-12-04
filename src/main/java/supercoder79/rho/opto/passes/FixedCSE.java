package supercoder79.rho.opto.passes;

import org.objectweb.asm.Opcodes;
import supercoder79.rho.ClassRefs;
import supercoder79.rho.RemappingClassRefs;
import supercoder79.rho.ast.Node;
import supercoder79.rho.ast.Var;
import supercoder79.rho.ast.low.*;
import supercoder79.rho.gen.CodegenContext;
import supercoder79.rho.opto.NodeVisitor;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

public class FixedCSE {
    public static Node elimCommonSubexpressions(CodegenContext ctx, Node node) {
        for (CSType type : CSType.values()) {
            List<CSRec> recs = new ArrayList<>();

            NodeVisitor.visitNodeTree(node, n -> findSubexpressions(n, recs, type));
            if (recs.size() > 1) {
                Var var = ctx.getNextVar();
                ctx.addLocalVar(var, type.type);

                node = NodeVisitor.visitNodeTreeReplacing(node, n -> replaceSubexpressions(n, var, type));

                node = node.replaceNode(node.children().get(0), new SequenceNode(
                        type.creator.apply(var),
                        node.children().get(0)
                ));
            }
        }

        return node;
    }

    private static void findSubexpressions(Node node, List<CSRec> records, CSType type) {
        if (type.matcher.test(node)) {
            records.add(new CSRec(node, type));
        }
    }

    private static Node replaceSubexpressions(Node node, Var var, CSType type) {
        if (type.matcher.test(node)) {
            return new VarReferenceNode(var, type.loadOp);
        }

        return node;
    }

    private enum CSType {
        AS_LONG(ClassRefs.LONG,
                Opcodes.LLOAD,
            varJ -> {
                return new VarAssignNode(varJ, new InvokeNode(
                    Opcodes.INVOKESTATIC, RemappingClassRefs.CLASS_CHUNKPOS.get(), RemappingClassRefs.METHOD_CHUNKPOS_ASLONG.get(), "(II)J",
                    new ContextBlockInsnNode(CodegenContext.Type.X, false), new ContextBlockInsnNode(CodegenContext.Type.Z, false)), Opcodes.LSTORE);
            },
            node -> {
                if (node instanceof InvokeNode invoke) {
                    if (invoke.type() == Opcodes.INVOKESTATIC && invoke.name().equals(RemappingClassRefs.METHOD_CHUNKPOS_ASLONG.get()) && invoke.clazz().equals(RemappingClassRefs.CLASS_CHUNKPOS.get())) {
                        if (invoke.children().size() == 2) {
                            Node x = invoke.children().get(0);
                            Node z = invoke.children().get(1);

                            if (x instanceof ContextBlockInsnNode xi && z instanceof ContextBlockInsnNode zi) {
                                if (xi.type() == CodegenContext.Type.X && zi.type() == CodegenContext.Type.Z && !xi.asDouble() && !zi.asDouble()) {
                                    return true;
                                }
                            }
                        }
                    }
                }

                return false;
            }
        ),
        CTX_Z(ClassRefs.DOUBLE,
                Opcodes.DLOAD,
                varX -> {
                    return new VarAssignNode(varX, new ContextBlockInsnNode(CodegenContext.Type.Z, true), Opcodes.DSTORE);
                },
                node -> {
                    if (node instanceof ContextBlockInsnNode ctx) {
                        return ctx.type() == CodegenContext.Type.Z && ctx.asDouble();
                    }

                    return false;
                }
        ),
        CTX_Y(ClassRefs.DOUBLE,
                Opcodes.DLOAD,
                varX -> {
                    return new VarAssignNode(varX, new ContextBlockInsnNode(CodegenContext.Type.Y, true), Opcodes.DSTORE);
                },
                node -> {
                    if (node instanceof ContextBlockInsnNode ctx) {
                        return ctx.type() == CodegenContext.Type.Y && ctx.asDouble();
                    }

                    return false;
                }
        ),
        CTX_X(ClassRefs.DOUBLE,
                Opcodes.DLOAD,
                varX -> {
                    return new VarAssignNode(varX, new ContextBlockInsnNode(CodegenContext.Type.X, true), Opcodes.DSTORE);
                },
                node -> {
                    if (node instanceof ContextBlockInsnNode ctx) {
                        return ctx.type() == CodegenContext.Type.X && ctx.asDouble();
                    }

                    return false;
                }
        ),
        ;

        private final String type;
        private final int loadOp;
        private final Function<Var, Node> creator;
        private final Predicate<Node> matcher;

        CSType(String type, int loadOp, Function<Var, Node> creator, Predicate<Node> matcher) {
            this.type = type;
            this.loadOp = loadOp;
            this.creator = creator;
            this.matcher = matcher;
        }
    }

    private record CSRec(Node node, CSType type) {

    }
}
