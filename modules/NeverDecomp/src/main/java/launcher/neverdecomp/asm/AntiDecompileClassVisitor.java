package launcher.neverdecomp.asm;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class AntiDecompileClassVisitor extends ClassVisitor {
    public AntiDecompileClassVisitor(ClassVisitor writer) {
        super(Opcodes.ASM5, writer);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc,
                                     String signature, String[] exceptions) {
        return new AntiDecompileMethodVisitor(access, super.visitMethod(access, name, desc, signature, exceptions), name, desc);
    }
}
