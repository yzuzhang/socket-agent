package com.feicent.agent.entity;

import java.io.Serializable;

import com.feicent.agent.util.Constants;

public class AgentEntity implements Serializable {

	private static final long serialVersionUID = 1L;
	private String  shell;     //命令行
	private String  clientFile;//客户端文件
	private String  serverIp;  //服务器ip地址
	private String  serverFile;//服务端文件地址
	private Integer type = Constants.TYPE_DEFAULT;
	
	public AgentEntity(Integer type) {
		super();
		this.type = type;
	}
	public AgentEntity(Integer type, String shell) {
		super();
		this.type = type;
		this.shell = shell;
	}

	public Integer getType() {
		return type;
	}
	public void setType(Integer type) {
		this.type = type;
	}
	public String getShell() {
		return shell;
	}
	public void setShell(String shell) {
		this.shell = shell;
	}
	public String getClientFile() {
		return clientFile;
	}
	public void setClientFile(String clientFile) {
		this.clientFile = clientFile;
	}
	public String getServerIp() {
		return serverIp;
	}
	public void setServerIp(String serverIp) {
		this.serverIp = serverIp;
	}
	public String getServerFile() {
		return serverFile;
	}
	public void setServerFile(String serverFile) {
		this.serverFile = serverFile;
	}

	@Override
	public String toString() {
		return "AgentEntity [shell=" + shell + ", clientFile=" + clientFile
				+ ", serverIp=" + serverIp + ", serverFile="
				+ serverFile + ", type=" + type + "]";
	}
	
}
