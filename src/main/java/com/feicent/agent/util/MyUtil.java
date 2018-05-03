package com.feicent.agent.util;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

/**
 * 通用工具类
 */
public class MyUtil {

	public static final String LONG_TIME  = "HH:mm:ss";
	public static final String SHORT_DATE = "yyyy-MM-dd";
	public static final String LONG_DATE  = "yyyy-MM-dd HH:mm:ss";

	public static final SimpleDateFormat DF_LONG = new SimpleDateFormat(LONG_DATE);
	public static final SimpleDateFormat DF_TIME = new SimpleDateFormat(LONG_TIME);

	/**
	 * @return 生成UUID字符串
	 */
	public static String uuid(){
		return UUID.randomUUID().toString().replaceAll("-", "");
	}

	/**
	 * @return 生成UUID字符串
	 */
	public static String UUID(){
		return UUID.randomUUID().toString();
	}
	
	/**
	 * 判断是否为null或者空字符串
	 * @param str
	 */
	public static boolean isEmpty(String str) {
		return null == str || str.trim().length() == 0;
	}

	public static boolean isNotEmpty(String str) {
		return !isEmpty(str);
	}
	
	public static String getNow(){
		return DF_LONG.format(new Date());
	}
	
	public static String getNow(long ts){
		return DF_LONG.format(ts);
	}
	
	/**
	 * 获取当前时间 HH:mm:ss
	 */
	public static String getNowTime(){
		return DF_TIME.format(new Date());
	}
	
	public static boolean isEmpty(final CharSequence cs) {
        return cs == null || cs.length() == 0;
    }
	
	public static boolean isNumeric(final CharSequence cs) {
        if (isEmpty(cs)) {
            return false;
        }
        final int sz = cs.length();
        for (int i = 0; i < sz; i++) {
            if (Character.isDigit(cs.charAt(i)) == false) {
                return false;
            }
        }
        return true;
    }
	
	/**
	 * 用户的当前工作目录路径
	 * D:\eclipse\workspaces\socket-agent\
	 */
	public static String getUserDir() {
		return Constants.USER_DIR + Constants.FILE_SEPARATOR;
	}

	public static void checkKey(String key) {
        if (key == null) {
            throw new NullPointerException("key can't be null");
        }
        if (key.equals("")) {
            throw new IllegalArgumentException("key can't be empty");
        }
    }

	public static String[] buildShell(String command) {
		if (isEmpty(command)) {
			throw new NullPointerException();
		}
		String[] cmdarray;
		String os = Constants.OS_NAME;

		if ("Windows 95".equals(os) || "Windows 98".equals(os) || "Windows ME".equals(os)){
			cmdarray = new String[]{"command.exe", "/C", command};
		} else if (os.startsWith("Windows")){
			cmdarray = new String[]{"cmd.exe", "/C", command};
		} else {
			cmdarray = new String[]{"/bin/sh", "-c", command};
		}
		
		return cmdarray;
	}

	/**
	 * 资源配置文件目录
	 * /D:/eclipse/workspaces/socket-agent/target/classes/
	 */
	public static String getResourcePath() {
		return MyUtil.class.getClass().getResource("/").getPath();
	}

	public static String getLocalServerIp() {
		String serverIp;
		try {
			InetAddress address = InetAddress.getLocalHost();
			serverIp = address.getHostAddress();
		} catch (UnknownHostException e) {
			serverIp = "127.0.0.1";
		}
		return serverIp;
	}

}
