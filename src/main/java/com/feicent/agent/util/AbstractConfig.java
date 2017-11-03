package com.feicent.agent.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AbstractConfig {
	private static Properties p = new Properties();
	public static Logger logger = LoggerFactory.getLogger(AbstractConfig.class);

	static {
		init("config.properties");
	}
	
	protected static void init(String fileName) {
		InputStream in = null;
		try {
			String filePath = System.getProperty("user.dir");
			File propertyFile = new File(filePath, fileName);
			in = new FileInputStream(propertyFile);
			if (in != null){
				p.load(in);
			}
		} catch (IOException e) {
			logger.error("Load 【" + fileName + "】 into Constants error: {}", e.getMessage());
		} finally {
			CloseUtil.close(in);
		}
	}

	/**
	 * @param key
	 *            property key
	 * @return property value
	 */
	protected static String getValue(String key)
	{
		String value = p.getProperty(key);
		if (value == null) {
			logger.warn("'" + key + "' not fount!");
		}
		return value;
	}

	/**
	 * @param key
	 *            property key
	 * @param defaultValue
	 *            if property value isn't exist, use default value
	 * @return property value
	 */
	protected static String getValue(String key, String defaultValue)
	{
		return p.getProperty(key, defaultValue);
	}

	/**
	 * @param key
	 *            property key
	 * @return int value. if value isn't exist, return 0
	 */
	protected static int getIntValue(String key)
	{
		return getIntValue(key, 0);
	}

	/**
	 * @param key
	 *            property key
	 * @param defaultValue
	 *            if property value isn't exist, use default value
	 * @return int value
	 */
	protected static int getIntValue(String key, int defaultValue)
	{
		String value = getValue(key);

		return value == null ? defaultValue : Integer.parseInt(value);
	}
	
	protected static long getLongValue(String key)
	{
		return getLongValue(key, 0);
	}
	
	protected static long getLongValue(String key, long defaultValue)
	{
		String value = getValue(key);
		return value == null ? defaultValue : Long.valueOf(value);
	}

	/**
	 * @param key
	 *            property key
	 * @return boolean value. if value isn't exist, return false
	 */
	protected static boolean getBoolValue(String key)
	{
		return getBoolValue(key, false);
	}

	/**
	 * @param key
	 *            property key
	 * @param defaultValue
	 *            defaultValue if property value isn't exist, use default value
	 * @return boolean value
	 */
	protected static boolean getBoolValue(String key, boolean defaultValue)
	{
		String value = getValue(key);
		return value == null ? defaultValue : "true".equals(value);
	}
	
}
