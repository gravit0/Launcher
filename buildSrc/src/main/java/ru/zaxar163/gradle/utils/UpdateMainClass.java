package ru.zaxar163.gradle.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

public class UpdateMainClass extends DefaultTask {
	@InputDirectory
	private File src;
	
	@InputFile
	private File mappings;
	
	@Input
	private String mainName;
	
	public String getMainName() {
		return mainName;
	}

	public void setMainName(String mainName) {
		this.mainName = mainName;
	}

	@OutputFile
	private File meta;

	public File getMeta() {
		return meta;
	}

	public void setMeta(File meta) {
		this.meta = meta;
	}

	public File getMappings() {
		return mappings;
	}

	public void setMappings(File mappings) {
		this.mappings = mappings;
	}

	public File getSrc() {
		return src;
	}

	public void setSrc(File src) {
		this.src = src;
	}
	
	@TaskAction
	public void doTask() throws IOException {
		List<String> lines = Files.readAllLines(mappings.toPath(), StandardCharsets.UTF_8);
		String obfName = null;
		for (String line : lines) {
			if (line.startsWith(mainName)) {
				String tmp = line.substring(mainName.length()+4);
				obfName = tmp.replace(":", "");
				break;
			}
		}
		
		File metaInf = new File (src, "META-INF/MANIFEST.MF");
		List<String> linesMeta = Files.readAllLines(metaInf.toPath(), StandardCharsets.UTF_8);
		List<String> toWrite = new ArrayList<>();
		for (String lineM : linesMeta) {
			toWrite.add(lineM.replace(mainName, obfName));
		}
		PrintWriter wr = new PrintWriter(new OutputStreamWriter(new FileOutputStream(metaInf), StandardCharsets.UTF_8));
		for (String lineW : toWrite) {
			wr.println(lineW);
		}
		wr.close();
	}
}
