package launcher.neverdecomp;

import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.AdviceAdapter;

public class AntiDecompileMethodVisitor extends AdviceAdapter implements Opcodes {

	protected AntiDecompileMethodVisitor(int access, String name, String desc) {
		super(ASM5, null, access, name, desc);
	}

	@Override
	public void onMethodEnter() {
		Label l1 = this.newLabel(POP);
		
	}
	
	public Label newLabel(int instr) {
		Label l = newLabel();
		this.visitInsn(instr);
		return l;
	}
	
	public Label jumpLabel(Label to) {
		Label l = newLabel();
		this.visitJumpInsn(GOTO, to);
		return l;
	}
}
