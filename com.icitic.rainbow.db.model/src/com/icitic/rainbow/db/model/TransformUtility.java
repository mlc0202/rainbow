package com.icitic.rainbow.db.model;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import com.icitic.core.db.model.Model;
import com.icitic.core.util.XmlBinder;
import com.icitic.rainbow.db.model.editors.ModelEditor;

public abstract class TransformUtility {
	private static XmlBinder<Model> binder = Model.getXmlBinder();
	public static Map<String, Transformer> transformerMap;

	static {
		transformerMap = new HashMap<String, Transformer>(5);
		TransformerFactory tf = TransformerFactory.newInstance();
		InputStream is = null;
		try {
			is = ModelEditor.class.getResourceAsStream("pretty.xsl");
			transformerMap.put("SOURCE", tf.newTransformer(new StreamSource(is)));
			is.close();
			is = ModelEditor.class.getResourceAsStream("db2.xsl");
			transformerMap.put("DB2", tf.newTransformer(new StreamSource(is)));
			is.close();
			is = ModelEditor.class.getResourceAsStream("oracle.xsl");
			transformerMap.put("ORACLE", tf.newTransformer(new StreamSource(is)));
			is.close();
			is = ModelEditor.class.getResourceAsStream("greenplum.xsl");
			transformerMap.put("GreenPlum", tf.newTransformer(new StreamSource(is)));
			is.close();
			is = ModelEditor.class.getResourceAsStream("gbase.xsl");
			transformerMap.put("GBASE", tf.newTransformer(new StreamSource(is)));
			is.close();
		} catch (TransformerConfigurationException e) {
			// should not happen
		} catch (IOException e) {
			// should not happen
		}
	}

	public static Model readModel(InputStream is, boolean autoClose) throws JAXBException {
		return binder.unmarshal(is, autoClose);
	}

	public static String getTransformText(String transform, Model model) {
		try {
			Transformer t = transformerMap.get(transform);
			if (t == null)
				return transform + " not defined";
			byte[] content = binder.marshal(model);
			StringWriter stringWriter = new StringWriter();
			t.transform(new StreamSource(new ByteArrayInputStream(content)), new StreamResult(stringWriter));
			return stringWriter.toString();
		} catch (Exception e) {
			return e.toString();
		}
	}

	public static void transform(String transform, Model model, OutputStream os) {
		Transformer t = transformerMap.get(transform);
		if (t == null)
			throw new IllegalArgumentException(transform + " not defined");
		try {
			byte[] content = binder.marshal(model);
			t.transform(new StreamSource(new ByteArrayInputStream(content)), new StreamResult(os));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
