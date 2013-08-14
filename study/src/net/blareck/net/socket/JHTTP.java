package net.blareck.net.socket;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 
 * @author blareck
 * @date 2013-8-13
 * @version 1.0 
 */
public class JHTTP extends Thread
{
	private File documentRootDirectory;
	private String indexFileName = "index.html";
	private ServerSocket server;
	private int numberThreads = 50;
	
	public JHTTP(File documentRootDirectory,int port,String indexFileName) throws IOException{
		if(!documentRootDirectory.isDirectory()){
			throw new IOException(documentRootDirectory + " does not exist as a directory");
		}
		this.documentRootDirectory = documentRootDirectory;
		this.server = new ServerSocket(port);
		this.indexFileName = indexFileName;
	}
	
	public JHTTP(File documentRootDirectory,int port) throws IOException{
		this(documentRootDirectory, port, "index.html");
	}
	
	public JHTTP(File documentRootDirectory) throws IOException{
		this(documentRootDirectory, 80, "index.html");
	}
	
	public void run(){
		ExecutorService threadPool  = Executors.newScheduledThreadPool(numberThreads);
		for(int i = 0; i<numberThreads; i ++){
			threadPool.submit(new  RequestProcessor(this.documentRootDirectory,this.indexFileName));
		}
		System.out.println("Accepting connections on port" + this.server.getLocalPort());
		System.out.println("Document Root : " +documentRootDirectory);
		while(true){
			try{
				Socket request = this.server.accept();
				RequestProcessor.processRequest(request);
			}
			catch(IOException e){
				e.printStackTrace();
			}
		}
	}
}
