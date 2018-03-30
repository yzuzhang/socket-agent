package com.feicent.agent.client;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

import javax.net.SocketFactory;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.feicent.agent.entity.AgentEntity;
import com.feicent.agent.util.CloseUtil;
import com.feicent.agent.util.Constants;
import com.feicent.agent.util.MyUtil;

/**
 * 客户端工具类
 * @author yzuzhang
 * @date 2017年11月3日
 */
public class SocketClient {
	Logger logger = LoggerFactory.getLogger(SocketClient.class);
	
	public static final int BUFFER_SIZE = 4 * 1024;
	public static final int SO_TIMED_OUT = Constants.TIME_OUT;
	public static final String separator = File.separator;
	public static final String READ_TIMED_OUT = "Read timed out";
	
	private String ip;//服务端地址
	private Socket socket = null;
    private int port = Constants.SOCKET_PORT; //服务端端口

    public SocketClient() {
		super();
	}
	public SocketClient(String ip) {
		this.ip = ip;
	} 
	public SocketClient(String ip, int port) {
		this.ip = ip;  
        this.port = port;
	}
	
    /** 
     * 创建socket连接 
     */
    protected void CreateConnection(){   
        try {
            socket = SocketFactory.getDefault().createSocket();
            SocketAddress socketAddress = new InetSocketAddress(ip, port);
            
            socket.connect(socketAddress, 300);
            socket.setTcpNoDelay(true);
            socket.setKeepAlive(true);
            socket.setSoTimeout(SO_TIMED_OUT);
        } catch (Exception e) { 
            throw new RuntimeException("【"+ip+"】的socket连接异常:"+e.getMessage());
        }
    }

    /**
     * 验证socket客户端连接是否正常
     */
    public void checkConnection(){
    	try {
			CreateConnection();
			socket.sendUrgentData(0xFF);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		} finally {
			closeSocket();
    	}
    }
    
    /**
     * 执行后不用返回信息
     * @param cmd
     */
    public void exeCommand(String cmd){
		ObjectOutputStream output = null;
		try {
			CreateConnection();
			output = new ObjectOutputStream(socket.getOutputStream());
			AgentEntity entity = new AgentEntity(Constants.TYPE_CMD, cmd);
			
		    output.writeObject(entity);  
		    output.flush();
		} catch(IOException ioe){
			throw new RuntimeException("脚本执行异常,原因:"+ ioe.getMessage());
		} finally {
			CloseUtil.close(output);
			closeSocket();
		}
    }
    
    /**
     * 执行后返回信息
     * @param shell
     */
    public String executeShell(String shell){
    	String result = "";
		ObjectOutputStream output = null;
		BufferedReader reader = null;
		StringBuffer sb = new StringBuffer("");
		try {
			CreateConnection();
			output = new ObjectOutputStream(socket.getOutputStream());
			AgentEntity entity = new AgentEntity(Constants.TYPE_SHELL, shell);
		    output.writeObject(entity);  
		    output.flush();
		    
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            while ((result = reader.readLine()) != null) {
        		if(result.startsWith(Constants.ERROR_MSG)){
        			result = result.substring(Constants.ERROR_MSG.length());
        		}
        		sb.append(result).append("\n");
            }
		} catch(IOException ioe) {
			throw new RuntimeException("脚本执行异常,原因:"+ioe.getMessage());
		} catch(Exception e) {
			throw new RuntimeException(e.getMessage());
		} finally {
			CloseUtil.close(output,reader);
			closeSocket();
		}
		
		if(sb.length() > 0){
			sb.deleteCharAt(sb.length()-1);
		}
		return sb.toString();
    }
    
    /**
	 * 本地文件上传到远程服务器
	 * @param localFile       本地文件路径
	 * @param remoteDirectory 远程文件目录
	 */
	public void copyLocalFileToRemote(File file, String remoteDirectory){
		copyLocalFileToRemote(file.getAbsolutePath(), remoteDirectory);
	}
	
	/**
	 * 本地文件上传到远程服务器
	 * @param localFile  本地文件路径
	 * @param remoteDirectory 远程文件目录
	 */
	public void copyLocalFileToRemote(String localFile, String remoteDirectory){
		copyLocalFileToRemote(localFile, remoteDirectory, null);
	}
    
	/**
	 * 本地文件上传到远程服务器
	 * @param localFile  本地文件路径
	 * @param remoteDirectory 远程文件目录
	 * @param remoteFileName 远程文件名称 为null时取本地文件名
	 */
	public void copyLocalFileToRemote(String localFile, String remoteDirectory, String remoteFileName){
		AgentEntity entity = null;
		DataOutputStream dos = null;
		ObjectOutputStream output = null;
		DataInputStream dis = null;
		BufferedInputStream bis = null;
		File file = new File(localFile);
		
    	try {
    		CreateConnection();
            if(!file.exists()){
            	throw new RuntimeException("文件不存在:"+ localFile);
            }
    		if(MyUtil.isEmpty(remoteFileName)){
    			remoteFileName = FilenameUtils.getName(localFile);
    		}
    		remoteDirectory += separator + remoteFileName;
    		
    		entity = new AgentEntity(Constants.TYPE_FILE_UPLOAD);
			entity.setServerFile(remoteDirectory);
			
    		dis = new DataInputStream(socket.getInputStream());
			output = new ObjectOutputStream(socket.getOutputStream());
		    output.writeObject(entity);
		 	output.flush();
		 	
		 	//向目标服务端传送本地文件
            bis = new BufferedInputStream(new FileInputStream(file));
            dos = new DataOutputStream(socket.getOutputStream());
            dos.writeUTF("文件名:"+ file.getName() +",文件大小:"+ file.length());
            
            boolean result = dis.readBoolean();
            if( !result ) {
            	throw new RuntimeException("远程服务器["+ ip +"]异常");
            }
            
            int len = -1;
            byte[] buffer = new byte[BUFFER_SIZE];
            while((len = bis.read(buffer)) != -1){
            	dos.write(buffer, 0, len);
            	dos.flush();
            }
            //通知server端输出完毕(必须)
            socket.shutdownOutput();

            if(!dis.readBoolean()) {
            	throw new RuntimeException(dis.readUTF());
            }
		} catch(IOException ioe) {
			throw new RuntimeException("上传文件["+file.getName()+"]到服务器["+ ip +"]异常,原因:"+ ioe.getMessage());
		} finally {
			CloseUtil.close(dos, bis, output);
			closeSocket();
		}
	}
	
	/**
	 * 本地文件上传到远程服务器
	 * @throws IOException
	 */
	public void copyLocalFileToRemote(InputStream is, String remoteFile) throws IOException{
		AgentEntity entity = null;
		DataOutputStream dos = null;
		ObjectOutputStream output = null;
		BufferedReader bufferedReader = null;
		
    	try {
    		CreateConnection();
    		output = new ObjectOutputStream(socket.getOutputStream());
			
			entity = new AgentEntity(Constants.TYPE_FILE_UPLOAD);
			entity.setServerFile(remoteFile);
			output.writeObject(entity);
			output.flush();
		 	
		 	//向目标服务端传送本地文件
		 	int length = -1;
            byte[] sendBytes = new byte[BUFFER_SIZE];
            dos = new DataOutputStream(socket.getOutputStream());
            while((length = is.read(sendBytes, 0, sendBytes.length)) >0){
                dos.write(sendBytes, 0, length);
                dos.flush();
            }
            socket.shutdownOutput();
            
            String result = null;
            bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        	while ((result=bufferedReader.readLine()) != null) {
        		if(result.indexOf(Constants.ERROR_MSG) != -1){
    				throw new RuntimeException(result);
        		}
            }
		}catch(IOException ioe){
			throw new RuntimeException("上传文件到服务器["+ ip +"]异常,原因:"+ioe.getMessage());
		} finally {
			IOUtils.closeQuietly(bufferedReader, dos, output);
			closeSocket();
		}
	}
	
	/**
	 * 远程服务器文件下载到本地
	 * @param remoteFile 远程文件路径
	 * @param localFile  本地文件目录
	 */
	public void copyRemoteFileToLocal(String remoteFile, String localFileDirectory) {
		copyRemoteFileToLocal(remoteFile, localFileDirectory, null);
	}

	/**
	 * 远程服务器文件下载到本地可自定义文件名称
	 * @param remoteFile 远程文件路径
	 * @param localFileDirectory  本地文件目录
	 * @param localFilename   下载到本地的名称(为null时取原名称)
	 */
	public void copyRemoteFileToLocal(String remoteFile,String localFileDirectory,String localFilename) {
		AgentEntity entity = null;
		DataInputStream dis = null;
		FileOutputStream fos = null;
		ObjectOutputStream output = null;
    	
		try {
    		CreateConnection();
    		if(MyUtil.isEmpty(localFilename)){
    			localFilename = FilenameUtils.getName(remoteFile);
    		}
    		localFileDirectory += separator + localFilename;

    		output = new ObjectOutputStream(socket.getOutputStream());
    		entity = new AgentEntity(Constants.TYPE_FILE_DOWNLOAD);
			entity.setServerFile(remoteFile);
			output.writeObject(entity);
			output.flush();
    		
    		dis = new DataInputStream(socket.getInputStream());
    		fos = new FileOutputStream(localFileDirectory);
    		
    		int len = -1;
    		byte[] buffer = new byte[BUFFER_SIZE];
    		while((len=dis.read(buffer)) != -1){
    			fos.write(buffer, 0, len);
    			fos.flush();
    		}

		} catch (IOException ioe){
			throw new RuntimeException("从服务器["+ ip +"]下载文件异常,原因:"+ioe.getMessage());
		} finally {
			CloseUtil.close(dis, fos, output);
			closeSocket();
		}
	}
	
    //关闭socket连接  
    public void closeSocket() {  
        try {  
            if (socket != null)  
                socket.close();  
        } catch (Exception e) {
        }  
    }

    public static void main(String[] args) {
		SocketClient client = new SocketClient("127.0.0.1");
		System.out.println("执行语句: java -version");
		String msg = client.executeShell("java -version");
		System.out.println(msg);
	}
}
