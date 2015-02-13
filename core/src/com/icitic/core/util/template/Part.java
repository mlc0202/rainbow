package com.icitic.core.util.template;

import java.util.List;

public class Part {

	private boolean token;

	private boolean loop;

	private String content;

	private List<Part> sub;

	public boolean isToken() {
		return token;
	}

	public void setToken(boolean token) {
		this.token = token;
	}

	public boolean isLoop() {
		return loop;
	}

	public void setLoop(boolean loop) {
		this.loop = loop;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public List<Part> getSub() {
		return sub;
	}

	public void setSub(List<Part> sub) {
		this.sub = sub;
	}

	private Part() {
	}

	public static Part newToken(String token) {
		Part part = new Part();
		part.setToken(true);
		part.setContent(token);
		return part;
	}

	public static Part newLoop(String flag, List<Part> sub) {
		Part part = new Part();
		part.setLoop(true);
		part.setContent(flag);
		part.setSub(sub);
		return part;
	}
	
	public static Part newText(String text) {
		Part part = new Part();
		part.setContent(text);
		return part;
	}
}
