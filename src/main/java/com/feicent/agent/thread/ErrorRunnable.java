package com.feicent.agent.thread;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import org.slf4j.Logger;

import com.feicent.agent.SocketApplication;
import com.feicent.agent.util.CloseUtil;
import com.feicent.agent.util.Constants;

/**
 * 用于处理Runtime.getRuntime().exec()产生的错误流
 */
public class ErrorRunnable implements Runnable {
	private Logger logger = SocketApplication.logger;;
	private BufferedReader bReader = null;
	private PrintWriter printWriter = null;
    
    /**
     * 构造函数
     * @param is
     */
    public ErrorRunnable(InputStream is, PrintWriter printWriter){  
       try{   
    	   this.printWriter = printWriter; 
           bReader = new BufferedReader(new InputStreamReader(new BufferedInputStream(is), "UTF-8"));  
       }catch(Exception e){
    	   logger.error("Structure Exception: ",e);
       }  
    }
    
    @Override
	public void run() {
	   String line = null;   
       try { 
    	   while(bReader.ready() && (line=bReader.readLine())!=null)  
           {
    		   logger.error("Error stream: "+line);
        	   if (printWriter != null) {
        		   printWriter.println(Constants.ERROR_MSG + line);
        	   }
           }
       } catch(Exception e) {
    	   logger.error("ErrorStream Thread Exception: ", e);
       } finally {
    	   CloseUtil.close(bReader);
       }
	}
    
}
