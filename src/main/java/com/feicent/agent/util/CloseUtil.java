package com.feicent.agent.util;

import java.io.Closeable;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * 关闭资源工具类
 * @author yzuzhang
 * @date 2017年11月3日
 */
public class CloseUtil {

	public static void close(Closeable... closeables){
		if(closeables != null)
			for(Closeable c : closeables){
				try {
					if(c!=null){
						c.close();
					}
				} catch (IOException e) {
					// ignored
				}
			}
	}

	public static void close(Socket socket){
		if(socket != null){
			try {
				socket.close();
			} catch (IOException e) {
				// ignored
			}
		}
	}
	
	public static void closeQuietly(ServerSocket sock){
        if (sock != null){
            try {
                sock.close();
            } catch (IOException ioe) {
                // ignored
            }
        }
    }
	
}
