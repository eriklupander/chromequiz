package com.squeed.chromequiz.channel;

import java.util.HashMap;

public class Event implements Message {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String id;
	private HashMap<String,String> params = new HashMap<String, String>();
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public HashMap<String, String> getParams() {
		return params;
	}
	public void setParams(HashMap<String, String> params) {
		this.params = params;
	}
	
	public void addParameter(String key, String value) {
		params.put(key, value);
	}
}
