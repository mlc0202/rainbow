package com.icitic.core.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.xml.sax.SAXException;

import com.google.common.base.Throwables;
import com.google.common.io.Closeables;

/**
 * XmlBinder封装了对JAXB的调用。提供了XML/Java对象的相互转换和验证。
 * 
 * 如果需要验证，那么应该提供schema
 * 
 * @author lijinghui
 * 
 * @param <T>
 */
public class XmlBinder<T> {

    public JAXBContext context;

    private Schema schema;

    /**
     * 构造一个指定上下文路径的XmlBinder
     * 
     * @param contextPath
     */
    public XmlBinder(String contextPath, ClassLoader classLoader) {
        try {
            context = JAXBContext.newInstance(contextPath, classLoader);
        } catch (JAXBException e) {
            throw Throwables.propagate(e);
        }
    }

    /**
     * 构造一个指定上下文路径及Schema的XmlBinder
     * 
     * @param contextPath
     * @param url
     */
    public XmlBinder(String contextPath, ClassLoader classLoader, URL url) {
        this(contextPath, classLoader);
        schema = createSchema(url);
    }

    /**
     * 根据一个类定义构造一个简单的XmlBinder
     * 
     * @param classToBeBound
     */
    public XmlBinder(Class<T> classToBeBound) {
        try {
            context = JAXBContext.newInstance(classToBeBound);
        } catch (JAXBException e) {
            throw Throwables.propagate(e);
        }
    }

    /**
     * 根据一个类定义和schema构造一个简单的XmlBinder
     * 
     * @param classToBeBound
     * @param url
     */
    public XmlBinder(Class<T> classToBeBound, URL url) {
        this(classToBeBound);
        schema = createSchema(url);
    }

    /**
     * 获得XML编组器
     * 
     * @return
     * @throws JAXBException
     */
    protected Marshaller getMarshaller() throws JAXBException {
        return context.createMarshaller();
    }

    /**
     * 获得XML解码器
     * 
     * @return
     * @throws JAXBException
     */
    protected Unmarshaller getUnmarshaller() throws JAXBException {
        Unmarshaller unmarshaller = context.createUnmarshaller();
        if (schema != null)
            unmarshaller.setSchema(schema);
        return unmarshaller;
    }

    /**
     * 从URL获得一个schema对象
     * 
     * @param url
     * @return
     */
    private static Schema createSchema(URL url) {
        try {
            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            return schemaFactory.newSchema(url);
        } catch (SAXException e) {
            throw Throwables.propagate(e);
        }
    }

    /**
     * 编组一个对象到输出流中
     * 
     * @param object
     * @param os
     * @throws Exception
     */
    public void marshal(T object, OutputStream os) throws Exception {
        try {
            getMarshaller().marshal(object, os);
        } catch (JAXBException e) {
            throw new Exception("对象转为XML失败", e);
        }
    }

    /**
     * 编组一个对象到字节数组中
     * 
     * @param object
     * @return
     * @throws Exception
     */
    public byte[] marshal(T object) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        marshal(object, baos);
        return baos.toByteArray();
    }

    /**
     * 编组一个对象到字符串
     * 
     * @param object
     * @return
     * @throws JAXBException 
     * @throws Exception
     */
    public String marshalString(T object) throws JAXBException {
        StringWriter writer = new StringWriter();
        getMarshaller().marshal(object, writer);
        return writer.toString();
    }

    /**
     * 将xml转换为对象
     * 
     * @param is
     * @param autoClose
     * @return
     * @throws JAXBException
     */
    @SuppressWarnings("unchecked")
    public T unmarshal(InputStream is, boolean autoClose) throws JAXBException {
        try {
            return (T) getUnmarshaller().unmarshal(is);
        } finally {
            if (autoClose)
				try {
					Closeables.close(is, true);
				} catch (IOException e) {
				}
        }
    }

    public T unmarshal(File file) throws JAXBException, FileNotFoundException {
        FileInputStream is = new FileInputStream(file);
        return unmarshal(is, true);
    }

    /**
     * 将xml转换为对象
     * 
     * @param xml
     * @return
     * @throws JAXBException
     */
    @SuppressWarnings("unchecked")
    public T unmarshal(String xml) throws JAXBException {
        Reader reader = new StringReader(xml);
        return (T) getUnmarshaller().unmarshal(reader);
    }

}
