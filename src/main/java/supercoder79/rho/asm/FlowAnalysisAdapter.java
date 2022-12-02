package supercoder79.rho.asm;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.*;

public class FlowAnalysisAdapter extends MethodVisitor {
    String owner;
    MethodVisitor next;
    public FlowAnalysisAdapter(String owner, int access, String name, String desc, MethodVisitor mv) {
        super(Opcodes.ASM9, new TreeVisitorAdapter(access, name, desc, mv));
        this.owner = owner;
        next = mv;
    }

    @Override
    public void visitEnd() {
        MethodNode mn = ((TreeVisitorAdapter) mv).node;
        Analyzer<BasicValue> a = new Analyzer<>(new SimpleVerifier());

        try {
            a.analyze(owner, mn);
        } catch (AnalyzerException e) {
            throw new RuntimeException(e);
        }

        System.out.println("Ran checks");

//        mn.accept(next);
    }
}
