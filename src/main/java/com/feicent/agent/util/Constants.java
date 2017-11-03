package com.feicent.agent.util;

public class Constants extends AbstractConfig {

	public static final String UTF_8 = "UTF-8";
	public static final String SERVER_IP = "127.0.0.1";
	public static final String ERROR_MSG = "ErrorMessage:";
	public static final String ERROR_TOKEN = "SOCKET TOKEN ERROR!";
	
	public static final int TIMEOUT = 60000;
	public static final int TYPE_DEFAULT = 0;
	public static final int TYPE_CMD = 1;
	public static final int TYPE_SHELL = 2;
	public static final int TYPE_FILE_UPLOAD = 3;
	public static final int TYPE_FILE_DOWNLOAD = 4;
	public static final int SOCKET_PORT = getIntValue("socket.port", 10240);
	
}
