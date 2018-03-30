package com.feicent.agent.thread;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.Charset;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;

import com.feicent.agent.SocketApplication;
import com.feicent.agent.entity.AgentEntity;
import com.feicent.agent.util.CloseUtil;
import com.feicent.agent.util.Constants;
import com.feicent.agent.util.MyUtil;

/**
 * 负责处理socket请求
 * @author yzuzhang
 * @date 2017年11月3日
 */
public class ServerThread implements Runnable {
	private Socket client;
	private String clientIp;
	
	private Logger logger = SocketApplication.logger;
	private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;
    
	public ServerThread(Socket socket){
		this.client = socket;
		this.clientIp = client.getInetAddress().getHostAddress();
	}

	@Override
	public void run() {
		logger.info("["+ clientIp + ":" + client.getPort()+"] come in");
		ObjectInputStream objInStream = null;

		try {
			objInStream = new ObjectInputStream(new BufferedInputStream(client.getInputStream()));
			Object readObj = objInStream.readObject();	
			AgentEntity entity = (AgentEntity) readObj;

			int type = entity.getType();
			if(type == Constants.TYPE_CMD){
				exeCommand(entity.getShell());
			} 
			else if(type == Constants.TYPE_SHELL){
				handlerShell(entity.getShell());
			} 
			else if(type == Constants.TYPE_FILE_UPLOAD){
				uploadFile(entity.getServerFile());
			} 
			else if(type == Constants.TYPE_FILE_DOWNLOAD){
				downloadFile(entity.getServerFile());
			}
			
		} catch (Exception e) {
			logger.error("Socket服务端异常:{}", e.getMessage(), e);
		} finally {
			CloseUtil.close(objInStream);
			CloseUtil.close(client);
		}
	}

	/**
	 * 执行后不返回信息
	 * @param cmd
	 */
	private void exeCommand(String shell){
		Process pos = null;
		try {
			String[] sh = new String[]{"/bin/sh", "-c", shell};
			pos = Runtime.getRuntime().exec(sh);
		} catch (Exception e) {
			logger.error("exeCommand Exception:", e);
		} finally {
			destroy(pos);
			CloseUtil.close(client);
		}
	}
	
	/**
	 * 执行后返回结果信息
	 * @param shell
	 */
	private void handlerShell(String shell){
		Process pos = null;
		PrintWriter out = null;
		LineNumberReader input = null;
		ProcessBuilder builder = null;

		try {
			out = new PrintWriter(client.getOutputStream(), true);

			String[] cmd = MyUtil.buildShell(shell);
			builder = new ProcessBuilder(cmd);
			builder.redirectErrorStream(true);
			pos = builder.start();

			/* 为错误输出流单独开一个线程读取,否则会造成标准输出流的阻塞 */  
            //Thread thread = new Thread(new ErrorRunnable(pos.getErrorStream(), out));  
            //thread.start();
            
			input = new LineNumberReader(new InputStreamReader(pos.getInputStream()));
			String line = null;
			while ((line=input.readLine()) != null) {
				out.println(line);//输出到socket客户端
			}
			pos.waitFor();
		} catch (Exception e) {
			logger.error("shellExcu Exception:", e);
		} finally {
			destroy(pos);
			CloseUtil.close(input,out);
			CloseUtil.close(client);
		}
	}
	
	/**
	 * socket client端文件上传到本地
	 * @param fileName 传到本地文件
	 */
	private void uploadFile(String fileName){
		DataInputStream dis = null;
		FileOutputStream fos = null;
		DataOutputStream dos = null;
		
		try {
			File file = new File(fileName);
			if(file.exists()){
				file.delete();
			}
			FileUtils.deleteQuietly(file);
			FileUtils.forceMkdir(file.getParentFile());
			
			fos = new FileOutputStream(file);
			dis = new DataInputStream(client.getInputStream());
			dos = new DataOutputStream(client.getOutputStream());
			
			int len = -1;
			byte[] buffer = new byte[1024*1024];
			while((len=dis.read(buffer)) != -1){
				fos.write(buffer, 0, len);
			}
			
			fos.flush();
			dos.writeBoolean(true);
		} catch (Exception e) {
			try {
				dos.writeBoolean(false);//通知socket客户端文件上传失败
				dos.writeUTF(Constants.ERROR_MSG + e.getMessage());
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			logger.error("UploadFile Exception: "+ fileName, e);
		} finally {
			CloseUtil.close(dos,dis,fos);
			CloseUtil.close(client);
		}
	}
	
	/**
	 * 本地文件发送到socket客户端
	 * @param localFile 本地文件
	 */
	private void downloadFile(String localFile){
		try {
			File file = new File(localFile);
			if (!file.exists() || !file.isFile()) {
				IOUtils.write(Constants.ERROR_MSG+ localFile +" is not exist", client.getOutputStream(), Charset.defaultCharset());
				return;
			}
			
			FileUtils.copyFile(file, client.getOutputStream());
		} catch (Exception e) {
			logger.error("Client <{}> download file <{}> Exception:", clientIp, localFile, e);
		} finally {
			CloseUtil.close(client);
		}
	}

	/**
	 * 本地文件发送到socket客户端
	 * @param localFile 本地文件
	 */
	protected void downloadFile2(String localFile){
		// 向客户端传送文件
		FileInputStream fis = null;
		BufferedInputStream bis = null;
		DataOutputStream dos = null;
		
		File file = new File(localFile);
		try {
			fis = new FileInputStream(file);
			bis = new BufferedInputStream(fis);
			dos = new DataOutputStream(client.getOutputStream());

			// 传输文件
			byte[] sendBytes = new byte[DEFAULT_BUFFER_SIZE];
			int length = 0;
			while((length=bis.read(sendBytes)) != -1){
				dos.write(sendBytes, 0, length);
				dos.flush();
			}
		} catch (Exception e) {
			logger.error("Client <{}> download file <{}> Exception:", clientIp, localFile, e);
		} finally {
			CloseUtil.close(dos, bis, fis);
			CloseUtil.close(client);
		}
	}

	private void destroy(Process ps) {
		if (ps != null) {
			ps.destroy();
		}
	}
	
}
