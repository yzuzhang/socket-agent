package com.feicent.agent.util;

public class Constants extends AbstractConfig {

	public static final String UTF_8 = "UTF-8";
	public static final String SERVER_IP = "127.0.0.1";
	public static final String ERROR_MSG = "ErrorMessage:";
	public static final String ERROR_TOKEN = "SOCKET TOKEN ERROR!";

	public static final int TIME_OUT = 60000;
	public static final int TYPE_DEFAULT = 0;
	public static final int TYPE_CMD = 1;	//执行后不返回信息
	public static final int TYPE_SHELL = 2;	//执行后返回结果信息
	public static final int TYPE_FILE_UPLOAD = 3;  //客户端上传文件
	public static final int TYPE_FILE_DOWNLOAD = 4;//客户端下载文件
	public static final int SOCKET_PORT = getIntValue("socket.port", 10240);
	
}
