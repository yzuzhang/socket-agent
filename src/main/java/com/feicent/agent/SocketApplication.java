package com.feicent.agent;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.feicent.agent.thread.ServerThread;
import com.feicent.agent.util.CloseUtil;
import com.feicent.agent.util.Constants;
import com.feicent.agent.util.MyUtil;

/**
 * 代理服务器启动类
 * @author yzuzhang
 * @date 2017年11月3日
 */
public class SocketApplication {
	public static Logger logger = LoggerFactory.getLogger("log");

	public static void main(String[] args) {
		ServerSocket server = null;
		ExecutorService threadPool = null;

		try {
			threadPool = Executors.newCachedThreadPool();
        	server = new ServerSocket(Constants.SOCKET_PORT); 
        	logger.info(MyUtil.getNow()+" creates a server socket success!");
        	
            while (true) {
                Socket socket = server.accept();
                socket.setTcpNoDelay(true);
                
                //当有请求时，线程池处理
                ServerThread thread = new ServerThread(socket);
                threadPool.execute(thread);
            }
        } catch (IOException e) {
        	logger.error(e.getMessage());
        } finally {
        	CloseUtil.close(server);
        	if (threadPool != null) {
        		threadPool.shutdown();
			}
        }
	}

}
