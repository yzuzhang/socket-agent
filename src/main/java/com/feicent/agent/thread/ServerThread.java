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

import org.slf4j.Logger;
import com.feicent.agent.SocketApplication;
import com.feicent.agent.entity.AgentEntity;
import com.feicent.agent.util.CloseUtil;
import com.feicent.agent.util.Constants;

/**
 * 负责处理socket请求
 * @author yzuzhang
 * @date 2017年11月3日
 */
public class ServerThread implements Runnable {
	private Socket client;
	private Logger logger = SocketApplication.logger;
    
	public ServerThread(Socket socket){
		this.client = socket;
	}

	@Override
	public void run() {
		logger.info("["+client.getInetAddress()+ ":" + client.getPort()+"] come in");
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
				uploadFile(entity.getFilePathRemote());
			} 
			else if(type == Constants.TYPE_FILE_DOWNLOAD){
				downloadFile(entity.getFilePathRemote());
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

			String[] cmd = new String[]{"/bin/sh", "-c", shell};
			builder = new ProcessBuilder(cmd);
			builder.redirectErrorStream(true);
			pos = builder.start();

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
			File parent = file.getParentFile();
			if(parent.isDirectory() && !parent.exists()){
				parent.mkdirs();
			}
			
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
			byte[] sendBytes = new byte[1024*1024];
			int length = 0;
			while((length=bis.read(sendBytes)) != -1){
				dos.write(sendBytes, 0, length);
				dos.flush();
			}
		} catch (Exception e) {
			logger.error("DownloadFile Exception: localFile-->{}", localFile, e);
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
