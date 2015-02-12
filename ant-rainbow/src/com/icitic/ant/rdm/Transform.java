package com.icitic.ant.rdm;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;

import com.icitic.core.db.model.Entity;
import com.icitic.core.db.model.Model;
import com.icitic.core.util.XmlBinder;

public class Transform extends Task {

	private final static XmlBinder<Model> binder = Model.getXmlBinder();

	private static Map<String, Transformer> transformerMap;

	private List<FileSet> fileSets = new LinkedList<FileSet>();

	private List<Exclude> excludes = new LinkedList<Exclude>();

	private File templateDir;

	private List<Template> templates = new LinkedList<Template>();

	private File todir;

	public void setTodir(File todir) {
		this.todir = todir;
	}

	public void setTemplateDir(File templateDir) {
		this.templateDir = templateDir;
	}

	/**
	 * Add a set of files to copy.
	 * 
	 * @param set
	 *            a set of files to copy.
	 */
	public void addFileset(FileSet set) {
		fileSets.add(set);
	}

	public void addExclude(Exclude exclude) {
		excludes.add(exclude);
	}

	public void addTemplate(Template template) {
		templates.add(template);
	}

	@Override
	public void execute() throws BuildException {
		if (todir == null)
			throw new BuildException("The toDir attribute must be present", getLocation());
		if (!todir.isDirectory())
			throw new BuildException("The toDir attribute must be a directory", getLocation());
		if (!templateDir.isDirectory())
			throw new BuildException("The templateDir attribute must be a directory", getLocation());
		File[] templateFiles = templateDir.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(".xsl");
			}
		});
		if (templateFiles.length == 0)
			throw new BuildException("no template found under " + templateDir.getAbsolutePath(), getLocation());

		TransformerFactory tf = TransformerFactory.newInstance();
		transformerMap = new HashMap<String, Transformer>(templateFiles.length);
		for (File file : templateFiles) {
			try {
				String key = file.getName();
				key = key.substring(0, key.length() - 4);
				transformerMap.put(key, tf.newTransformer(new StreamSource(file)));
			} catch (TransformerConfigurationException e) {
				throw new BuildException("read template failed->" + file.getName(), e);
			}
		}

		for (Template template : templates) {
			if (template.getName() == null || template.getName().isEmpty())
				throw new BuildException("xslt need a name");
			if (!transformerMap.containsKey(template.getName()))
				throw new BuildException("template " + template.getName() + " not found");
			if (template.getSuffix() == null || template.getSuffix().isEmpty())
				template.setSuffix("sql");
		}

		Map<String, Map<String, Entity>> maps = readModelFiles();
		try {
			doOutputWork(maps);
		} catch (Exception e) {
			throw new BuildException(e, getLocation());
		}
	}

	private Map<String, Map<String, Entity>> readModelFiles() {
		Map<String, Map<String, Entity>> maps = new TreeMap<String, Map<String, Entity>>();
		for (FileSet fileSet : fileSets) {
			DirectoryScanner ds = null;
			try {
				ds = fileSet.getDirectoryScanner(getProject());
			} catch (BuildException e) {
				if (!getMessage(e).endsWith(DirectoryScanner.DOES_NOT_EXIST_POSTFIX)) {
					throw e;
				} else {
					log("Warning: " + getMessage(e), Project.MSG_ERR);
					continue;
				}
			}

			File fromDir = fileSet.getDir(getProject());
			for (String s : ds.getIncludedFiles()) {
				File file = new File(fromDir, s);
				try {
					com.icitic.core.db.model.Model dbModel = binder.unmarshal(file);
					Map<String, Entity> entityMap = maps.get(dbModel.getName());
					if (entityMap == null) {
						entityMap = new TreeMap<String, Entity>();
						maps.put(dbModel.getName(), entityMap);
					}
					for (Entity entity : dbModel.getEntities()) {
						entityMap.put(entity.getDbName(), entity);
					}
				} catch (Exception e) {
					throw new BuildException(s, e);
				}
			}
		}
		for (Exclude exclude : excludes) {
			Map<String, Entity> entityMap = maps.get(exclude.getModel());
			if (entityMap != null) {
				entityMap.remove(exclude.getEntity());
			}
		}
		return maps;
	}

	private void doOutputWork(Map<String, Map<String, Entity>> maps) throws Exception {
		for (String modelName : maps.keySet()) {
			Map<String, Entity> entityMap = maps.get(modelName);
			if (entityMap.isEmpty()) {
				System.out.println(String.format("model [%s] has no entity", modelName));
				continue;
			}
			List<Entity> entities = new ArrayList<Entity>(entityMap.size());
			entities.addAll(entityMap.values());
			Model model = new Model();
			model.setName(modelName);
			model.setEntities(entities);
			byte[] xml = binder.marshal(model);
			System.out.println(String.format("transform model [%s] ", modelName));
			saveFile(modelName, xml);
		}

	}

	private void saveFile(String modelName, byte[] modelXml) throws TransformerException, IOException {
		for (Template template : templates) {
			String fileName = String.format("%s_%s.%s", modelName, template.getName(), template.getSuffix());
			File file = new File(todir, fileName);
			FileOutputStream fos = new FileOutputStream(file);
			Transformer t = transformerMap.get(template.getName());
			t.transform(new StreamSource(new ByteArrayInputStream(modelXml)), new StreamResult(fos));
			fos.close();
		}
	}

	/**
	 * Handle getMessage() for exceptions.
	 * 
	 * @param ex
	 *            the exception to handle
	 * @return ex.getMessage() if ex.getMessage() is not null otherwise return
	 *         ex.toString()
	 */
	private String getMessage(Exception ex) {
		return ex.getMessage() == null ? ex.toString() : ex.getMessage();
	}
}
