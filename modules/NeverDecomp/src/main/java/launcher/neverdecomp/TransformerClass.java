package launcher.neverdecomp;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

import launchserver.manangers.BuildHookManager.Transformer;

public class TransformerClass implements Transformer {

	@Override
	public byte[] transform(byte[] input, CharSequence classname) {
		ClassReader classReader = new ClassReader(input);
		ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
		classReader.accept(new AntiDecompileClassVisitor(writer), 0);
		return writer.toByteArray();
	}
}
