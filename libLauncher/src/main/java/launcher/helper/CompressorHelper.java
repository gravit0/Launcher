package launcher.helper;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
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
	public static final FilterOptions[] opts = new FilterOptions[] {
		new LZMA2Options()
	};
	
	@LauncherAPI
	public static InputStream wrapIn(InputStream in) throws IOException {
		return new XZInputStream(in);
	}
	@LauncherAPI
	public static OutputStream wrapOut(OutputStream out) throws IOException {
		return new XZOutputStream(out, opts);
	}
	@LauncherAPI
	public static void compressDir(File dir, File out) throws IOException {
		try(CompressorDirVisitor visitor = new CompressorDirVisitor(dir.toPath(), new TarOutputStream(new BufferedOutputStream(wrapOut(new FileOutputStream(out)))))) {
			IOHelper.walk(dir.toPath(), visitor, true);
		}
	}
	@LauncherAPI
	public static void unCompress(File dir, File archive) throws IOException {
		try(TarInputStream inp = new TarInputStream(new BufferedInputStream(wrapIn(new FileInputStream(archive))))) {
            TarEntry entry = null;
            String name = null;
            File f = null;
            byte data[] = new byte[2048];
            while((entry=inp.getNextEntry())!=null){
                  
                name = entry.getName();
                f = new File(dir, name);
                if (entry.isDirectory()) {
                	f.mkdirs();
                	continue;
                }

                if (f.getParentFile() != null) f.getParentFile().mkdirs();
                f.createNewFile();
                FileOutputStream fout = new FileOutputStream(f);   
				int count = -1;
				while ((count = inp.read(data)) != -1) {
					fout.write(data, 0, count);
				}
                fout.flush();
                fout.close();
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
		public FileVisitResult preVisitDirectory(Path file, BasicFileAttributes attrs) throws IOException {
			out.putNextEntry(new TarEntry(file.toFile(), this.dir.relativize(dir).toString()));
			return super.preVisitDirectory(file, attrs);
		}
		@LauncherAPI
		@Override
		public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
			out.putNextEntry(new TarEntry(file.toFile(), this.dir.relativize(dir).toString()));
			byte data[] = new byte[2048];
			try (FileInputStream fis = new FileInputStream(file.toFile())) {
				int count = -1;
				while ((count = fis.read(data)) != -1) {
					out.write(data, 0, count);
				}
				out.flush();
			}
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
