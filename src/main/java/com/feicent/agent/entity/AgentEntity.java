package com.feicent.agent.entity;

import java.io.Serializable;

import com.feicent.agent.util.Constants;

public class AgentEntity implements Serializable {
	private static final long serialVersionUID = -2151324266631515148L;
	
	private String shell;//命令行
	private String filePath;//客户端文件
	private String serverIp;//服务器ip地址
	private String filePathRemote;//服务端文件地址
	private int    type = Constants.TYPE_DEFAULT;
	
	public AgentEntity(int type) {
		super();
		this.type = type;
	}
	
	public AgentEntity(int type, String shell) {
		super();
		this.type = type;
		this.shell = shell;
	}
	
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public String getShell() {
		return shell;
	}
	public void setShell(String shell) {
		this.shell = shell;
	}
	public String getFilePath() {
		return filePath;
	}
	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}
	public String getServerIp() {
		return serverIp;
	}
	public void setServerIp(String serverIp) {
		this.serverIp = serverIp;
	}
	public String getFilePathRemote() {
		return filePathRemote;
	}
	public void setFilePathRemote(String filePathRemote) {
		this.filePathRemote = filePathRemote;
	}
	
}
