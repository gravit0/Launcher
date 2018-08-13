package ru.zaxar163.gradle.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.jar.JarInputStream;
import java.util.jar.Pack200;
import java.util.zip.GZIPOutputStream;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

public class Pack200Task extends DefaultTask {
	@InputFile
	private File jar;
	
	@OutputFile
	private File packed;

	public File getJar() {
		return jar;
	}

	public void setJar(File jar) {
		this.jar = jar;
	}

	public File getPacked() {
		return packed;
	}

	public void setPacked(File packed) {
		this.packed = packed;
	}
	
	@TaskAction
	public void doTask() throws IOException {
		JarInputStream in = new JarInputStream(new FileInputStream(jar));
		packed.createNewFile();
		GZIPOutputStream out = new GZIPOutputStream(new FileOutputStream(packed));
		Pack200.newPacker().pack(in, out);
		in.close();
		out.finish();
		out.close();
	}
}
