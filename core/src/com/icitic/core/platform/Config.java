package com.icitic.core.platform;

import java.util.List;

import com.icitic.core.util.Utils;

/**
 * 平台配置对象
 * 
 * @author lijinghui
 * 
 */
public class Config {

    /**
     * 平台ID
     */
    private int id;

    /**
     * JmxServer端口
     */
    private int jmxPort = 1109;

    /**
     * 初始启动的bundle列表,没有内容表示全部启动
     * 
     * 其中的项若以*结尾，如"sys.*"，则表示以 "sys."开头的bundle都会被启动
     */
    private List<String> on;

    /**
     * 初始不启动的bundle列表，*的意义同上
     */
    private List<String> off;
    
    /**
     * 项目名称，与此名称无关的bundle都不会被启动
     */
    private String project = Utils.NULL_STR;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getJmxPort() {
        return jmxPort;
    }

    public void setJmxPort(int jmxPort) {
        this.jmxPort = jmxPort;
    }

    public List<String> getOn() {
        return on;
    }

    public void setOn(List<String> on) {
        this.on = on;
    }

    public List<String> getOff() {
        return off;
    }

    public void setOff(List<String> off) {
        this.off = off;
    }

	public String getProject() {
		return project;
	}

	public void setProject(String project) {
		this.project = project;
	}

}