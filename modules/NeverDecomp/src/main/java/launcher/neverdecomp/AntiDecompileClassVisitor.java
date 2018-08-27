package launcher.neverdecomp;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class AntiDecompileClassVisitor extends ClassVisitor {
    public AntiDecompileClassVisitor(ClassWriter cv) {
        super(Opcodes.ASM5, cv);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc,
                                     String signature, String[] exceptions) {
        return new AntiDecompileMethodVisitor(access, name, desc);
    }
}
