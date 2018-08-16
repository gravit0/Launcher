package launcher.helper;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import org.kamranzafar.jtar.TarEntry;
import org.kamranzafar.jtar.TarInputStream;
import org.kamranzafar.jtar.TarOutputStream;
import org.tukaani.xz.FilterOptions;
import org.tukaani.xz.LZMA2Options;
import org.tukaani.xz.XZInputStream;
import org.tukaani.xz.XZOutputStream;
import launcher.LauncherAPI;

@LauncherAPI
public class CompressorHelper {
	@LauncherAPI
	private CompressorHelper() {
	}

	@LauncherAPI
	public static final FilterOptions[] opts = new FilterOptions[] { new LZMA2Options() };

	@LauncherAPI
	public static InputStream wrapIn(InputStream in) throws IOException {
		return new XZInputStream(in);
	}

	@LauncherAPI
	public static OutputStream wrapOut(OutputStream out) throws IOException {
		return new XZOutputStream(out, opts);
	}

	@LauncherAPI
	public static void compressDir(Path dir, Path out) throws IOException {
		IOHelper.createParentDirs(out);
		out.toFile().createNewFile();
		try (CompressorDirVisitor visitor = new CompressorDirVisitor(dir,
				new TarOutputStream(new BufferedOutputStream(wrapOut(IOHelper.newOutput(out)))))) {
			IOHelper.walk(dir, visitor, true);
		}
	}

	@LauncherAPI
	public static void decompressDir(Path dir, Path archive) throws IOException {
		try (TarInputStream inp = new TarInputStream(new BufferedInputStream(wrapIn(IOHelper.newInput(archive))))) {
			TarEntry entry = null;
			String name = null;
			Path f = null;
			while ((entry = inp.getNextEntry()) != null) {
				name = entry.getName();
				f = dir.resolve(name);
				if (entry.isDirectory()) {
					IOHelper.createParentDirs(f);
					f.toFile().mkdir();
					continue;
				}
				IOHelper.createParentDirs(f);
				f.toFile().createNewFile();
				IOHelper.transfer(inp, f);
			}
		}
	}

	private static class CompressorDirVisitor extends SimpleFileVisitor<Path> implements AutoCloseable {
		private final Path dir;
		private final TarOutputStream out;

		private CompressorDirVisitor(Path dir, TarOutputStream out) {
			this.dir = dir;
			this.out = out;
		}

		@LauncherAPI
		@Override
		public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
			out.putNextEntry(new TarEntry(file.toFile(), this.dir.relativize(file).toString()));
			IOHelper.transfer(file, out);
			out.flush();
			return super.visitFile(file, attrs);
		}

		@LauncherAPI
		@Override
		public void close() throws IOException {
			out.flush();
			out.close();
		}
	}
}