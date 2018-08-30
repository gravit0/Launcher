package launcher.neverdecomp.asm;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.AdviceAdapter;

public class AntiDecompileMethodVisitor extends AdviceAdapter implements Opcodes {

	protected AntiDecompileMethodVisitor(int access, MethodVisitor mw, String name, String desc) {
		super(ASM5, mw, access, name, desc);
	}

	// в начале каждого метода
	// убивает декомпиляторы
	@Override
	public void onMethodEnter() {
		antiDecomp();		
	}
	
	private void antiDecomp() {
		Label lbl1 = this.newLabel(), lbl15 = this.newLabel(), lbl2 = this.newLabel(), lbl3 = this.newLabel(), lbl35 = this.newLabel(), lbl4 = this.newLabel();
		
		// try-catch блок с lbl1 до lbl2 с переходом на lbl15 при java/lang/Exception
		this.visitException(lbl1, lbl2, lbl15);
		// try-catch блок с lbl3 до lbl4 с переходом на lbl3 при java/lang/Exception
		this.visitException(lbl3, lbl4, lbl3);
		
		// lbl1: goto lbl2
		this.visitLabel(lbl1);
		this.jumpLabel(lbl2);
		// lbl15: pop
		this.visitLabel(lbl15);
		this.visitInsn(POP);
		// lbl2: goto lbl35
		this.visitLabel(lbl2);
		this.jumpLabel(lbl35);
		// lbl3: pop
		this.visitLabel(lbl3);
		this.visitInsn(POP);
		// lbl35: nop
		this.visitLabel(lbl35);
		this.visitInsn(NOP);
		// lbl4: nop
		this.visitLabel(lbl4);
		this.visitInsn(NOP);
	}
	
	private void visitException(Label st, Label en, Label h) {
		this.visitTryCatchBlock(st, en, h, "java/lang/Exception");
	}

	// experemental
	@Override
	public void visitInsn(int opcode) {
		super.visitInsn(POP);
		super.visitInsn(NOP);
		super.visitInsn(opcode);
	}

	// experemental
	@Override
	public void onMethodExit(int opcode) {
		super.onMethodExit(opcode);
		antiDecomp();
	}
	
	public void jumpLabel(Label to) {
		this.visitJumpInsn(GOTO, to);
	}
}
