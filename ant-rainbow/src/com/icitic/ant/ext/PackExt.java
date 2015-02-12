package com.icitic.ant.ext;

import static com.google.common.base.Preconditions.checkArgument;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.LinkedList;
import java.util.List;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;

import com.google.common.base.CharMatcher;
import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.io.Closeables;
import com.google.common.io.Files;

public class PackExt extends Task {

	private List<String> requireList = new LinkedList<String>();

	private File webDir;

	private Writer writer;

	private List<FileSet> fileSets = new LinkedList<FileSet>();

	private List<Namespace> namespaces = Lists.newLinkedList();

	private boolean verbose = false;

	public void setWebDir(File webDir) {
		this.webDir = webDir;
	}

	public void addFileset(FileSet fileset) {
		fileSets.add(fileset);
	}

	public void addNamespace(Namespace namespace) {
		namespaces.add(namespace);
	}

	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}

	@Override
	public void execute() throws BuildException {
		if (webDir == null)
			throw new BuildException("The webDir attribute must be present", getLocation());
		if (fileSets.size() == 0)
			throw new BuildException("fileSet must be set.");

		try {
			for (FileSet fileSet : fileSets) {
				fileSet.setErrorOnMissingDir(false);
				DirectoryScanner ds = fileSet.getDirectoryScanner(getProject());
				for (String s : ds.getIncludedFiles()) {
					processEntryFile(new File(fileSet.getDir(getProject()), s));
				}
			}
		} catch (Exception e) {
			throw new BuildException(e);
		}
	}

	private String loadFile(File file) throws IOException {
		String content = Files.toString(file, Charsets.UTF_8);
		content = content.replaceAll("\n|\r|\\s|\t", "");
		return content;
	}

	private File getClassFile(String cls) {
		File file = null;
		for (Namespace namespace : namespaces) {
			if (cls.startsWith(namespace.getNs())) {
				file = new File(webDir, namespace.getDir());
				String path = cls.substring(namespace.getNs().length()).replace('.', '/') + ".js";
				file = new File(file, path);
			}
		}
		checkArgument(file != null && file.exists(), "class [%s] file not exist, forget namespace?", cls);
		return file;
	}

	void processEntryFile(File file) throws Exception {
		System.out.println(file.getPath());
		requireList.clear();
		File outputFile = new File(file.getParentFile(), "app-all.js");
		if (outputFile.exists() && !outputFile.delete())
			throw new BuildException("cannot init app-all.js for " + file);

		try {
			writer = Files.newWriter(outputFile, Charsets.UTF_8);
			String content = loadFile(file);
			processNormalFile(content);
			if (content.contains("Ext.application({"))
				processApplication(content);
			outputFile(file);
		} finally {
			Closeables.close(writer, false);
			writer = null;
		}
	}

	private void outputFile(File file) throws IOException {
		writer.write(Files.toString(file, Charsets.UTF_8));
		if (verbose) {
			System.out.println(file.getAbsolutePath().substring(webDir.getAbsolutePath().length()));
		}
	}

	private void processNormalClass(String cls) throws IOException {
		if (requireList.contains(cls))
			return;
		requireList.add(cls); // 写在前面可以避免循环依赖，更好的作法应该是检测出循环依赖然后抛异常
		File file = getClassFile(cls);
		String content = loadFile(file);
		processNormalFile(content);
		int index = cls.indexOf(".controller.");
		if (index > 0) {
			String namespace = cls.substring(0, index);
			processController(content, namespace);
		}
		outputFile(file);
	}

	private void processNormalFile(String content) throws IOException {
		List<String> list = new LinkedList<String>() {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean add(String e) {
				if (contains(e))
					return false;
				return ignoreNameSpace(e) ? false : super.add(e);
			}
		};
		for (RequireKey key : RequireKey.values()) {
			key.readRequire(content, list);
		}
		for (String cls : list) {
			processNormalClass(cls);
		}
	}

	private void processController(String content, String namespace) throws IOException {
		for (String model : ControllerKey.model.read(content)) {
			String name = ControllerKey.model.getFullName(model, namespace);
			processNormalClass(name);
		}
		for (String store : ControllerKey.store.read(content)) {
			String name = ControllerKey.store.getFullName(store, namespace);
			processNormalClass(name);
		}
		for (String view : ControllerKey.view.read(content)) {
			String name = ControllerKey.view.getFullName(view, namespace);
			processNormalClass(name);
		}
	}

	private void processApplication(String content) throws IOException {
		int index = content.indexOf("name:'");
		checkArgument(index > 0, "application not define name");
		index += 6;
		int endIndex = content.indexOf('\'', index);
		checkArgument(endIndex > index, "application not define name");
		String name = content.substring(index, endIndex);
		processController(content, name);
		if (content.indexOf("autoCreateViewport:true") > 0)
			processNormalClass(name + ".view.ViewPort");

		// 没必要看是不是定义了namespace
		// List<String> nsList = ControllerKey.namespace.read(content);
		// 反正controller自己应该带着namespace

		for (String controller : ControllerKey.controller.read(content)) {
			if (controller.contains(".") && CharMatcher.JAVA_UPPER_CASE.apply(controller.charAt(0))) {
				processNormalClass(controller);
			} else {
				processNormalClass(name + ".controller." + controller);
			}
		}
	}

	/**
	 * 检查一个类是否需要忽略
	 * 
	 * @param cls
	 * @return true 忽略之
	 */
	private boolean ignoreNameSpace(String cls) {
		if (cls.startsWith("Ext."))
			return true;
		if (namespaces == null)
			return false;
		for (Namespace namespace : namespaces) {
			if (namespace.isIgnore() && cls.startsWith(namespace.getNs()))
				return true;
		}
		return false;
	}

}