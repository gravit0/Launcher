package ru.zaxar163.gradle.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.FileCollection;
import org.gradle.api.tasks.CacheableTask;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.api.tasks.TaskAction;

import com.google.common.io.ByteStreams;

import groovy.lang.Closure;

@CacheableTask
public class ExtractTask extends DefaultTask {
	private final Matcher matcher = new Matcher();

	@Input
	@Optional
	private boolean clean = false;

	@OutputDirectory
	private File destinationDir = null;
	@Optional
	@Input
	private List<Closure<Boolean>> excludeCalls = new LinkedList<Closure<Boolean>>();
	@Optional
	@Input
	private List<String> excludes = new LinkedList<String>();
	@Optional
	@Input
	private boolean includeEmptyDirs = true;
	@Optional
	@Input
	private List<String> includes = new LinkedList<String>();

	@PathSensitive(value = PathSensitivity.ABSOLUTE)
	@InputFiles
	private LinkedHashSet<File> sourcePaths = new LinkedHashSet<File>();

	private void delete(File f) throws IOException {
		if (f.isDirectory()) {
			for (File c : f.listFiles()) {
				this.delete(c);
			}
		}
		f.delete();
	}

	@TaskAction
	public void doTask() throws IOException {
		File dest = this.destinationDir;

		if (this.shouldClean()) {
			this.delete(dest);
		}

		dest.mkdirs();

		for (File source : this.sourcePaths) {

			ZipFile input = new ZipFile(source);
			try {
				Enumeration<? extends ZipEntry> itr = input.entries();

				while (itr.hasMoreElements()) {
					ZipEntry entry = itr.nextElement();
					if (this.shouldExtract(entry.getName())) {
						File out = new File(dest, entry.getName());
						if (entry.isDirectory()) {
							if (this.includeEmptyDirs && !out.exists()) {
								out.mkdirs();
							}
						} else {
							File outParent = out.getParentFile();
							if (!outParent.exists()) {
								outParent.mkdirs();
							}

							FileOutputStream fos = new FileOutputStream(out);
							InputStream ins = input.getInputStream(entry);

							ByteStreams.copy(ins, fos);

							fos.close();
							ins.close();
						}
					}
				}
			} finally {
				input.close();
			}
		}
	}

	public void exclude(Closure<Boolean> c) {
		this.excludeCalls.add(c);
	}

	public ExtractTask exclude(String... paterns) {
		for (String patern : paterns) {
			this.excludes.add(patern);
		}
		return this;
	}

	public ExtractTask from(File... paths) {
		for (File path : paths) {
			this.sourcePaths.add(path);
		}
		return this;
	}

	public File getDestinationDir() {
		return this.destinationDir;
	}

	public List<Closure<Boolean>> getExcludeCalls() {
		return this.excludeCalls;
	}

	public List<String> getExcludes() {
		return this.excludes;
	}

	public List<String> getIncludes() {
		return this.includes;
	}

	public FileCollection getSourcePaths() {
		FileCollection collection = this.getProject().files(new Object[] {});

		for (File file : this.sourcePaths) {
			collection = collection.plus(this.getProject().files(file));
		}

		return collection;
	}

	public ExtractTask include(String... paterns) {
		for (String patern : paterns) {
			this.includes.add(patern);
		}
		return this;
	}

	public ExtractTask into(File target) {
		this.destinationDir = target;
		return this;
	}

	public boolean isIncludeEmptyDirs() {
		return this.includeEmptyDirs;
	}

	public void setClean(boolean clean) {
		this.clean = clean;
	}

	public ExtractTask setDestinationDir(File target) {
		this.destinationDir = target;
		return this;
	}

	public void setIncludeEmptyDirs(boolean includeEmptyDirs) {
		this.includeEmptyDirs = includeEmptyDirs;
	}

	public boolean shouldClean() {
		return this.clean;
	}

	private boolean shouldExtract(String path) {
		for (String exclude : this.excludes) {
			if (this.matcher.matches(exclude, path)) {
				return false;
			}
		}

		for (Closure<Boolean> exclude : this.excludeCalls) {
			if (exclude.call(path).booleanValue()) {
				return false;
			}
		}

		for (String include : this.includes) {
			if (this.matcher.matches(include, path)) {
				return true;
			}
		}

		return this.includes.size() == 0; // If it gets to here, then it matches nothing. default to true, if no
											// includes
											// were specified
	}
}
