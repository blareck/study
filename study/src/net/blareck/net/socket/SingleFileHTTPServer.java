package net.blareck.net.socket;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * 单文件服务器
 * @author blareck
 * @date 2013-8-7
 * @version 1.0 
 */
public class SingleFileHTTPServer extends Thread
{
	private byte[] content;
	private byte[] header;
	private int port = 80;
	
	private SingleFileHTTPServer(){
		new SingleFileHTTPServer();
	}
	
	public SingleFileHTTPServer(String data,String encoding,String MIMEType,int port) throws UnsupportedEncodingException{
		this(data.getBytes(encoding),encoding,MIMEType,port);
	}
	
	public SingleFileHTTPServer(byte[] data,String encoding,String MIMEType,int port) throws UnsupportedEncodingException{
		this.content = data;
		this.port = port;
		String header = "HTTP/1.0 200 OK\r\n" +
				"Server: OneFile 1.0\r\n" +
				"Content-length: "+this.content.length+"\r\n" +
						"Content-type: "+MIMEType+"\r\n\r\n";
		this.header = header.getBytes("ASCII");
	}
	
	public void run(){
		try
		{
			ServerSocket server = new ServerSocket(this.port);
			System.out.println("Accepting connections on port"+server.getLocalPort());
			System.out.println("Data to be send:");
			System.out.println(this.content);
			while(true){
				Socket connection = null;
				try{
					connection = server.accept();
					OutputStream out = new BufferedOutputStream(connection.getOutputStream());
					InputStream in = new BufferedInputStream(connection.getInputStream());
					StringBuffer request = new StringBuffer(80);
					while(true){
						int c = in.read();
						if(c=='\r'||c=='\n'||c==-1){
							break;
						}
						request.append((char)c);
					}
					if(request.toString().indexOf("HTTP/")!=-1){
						out.write(this.header);
					}
					out.write(this.content);
					out.flush();
				}
				catch(IOException ex){}
				finally{
					if(connection != null)
						connection.close();
				}
			}
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args){
		try
		{
			String _args0 = "D:\\test.html";
			//args[1] = "9999";
			String contentType  = "text/plain";
			if(_args0.endsWith(".html")||_args0.endsWith(".htm")){
				contentType = "text/html";
			}
			InputStream in = new FileInputStream(_args0);
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			int b;
			while((b = in.read()) != -1){
				out.write(b);
			}
			byte[] data = out.toByteArray();
			int port;
			try{
				port = Integer.parseInt(args[1]);
				//超过端口范围赋值为默认端口号
				if(port<1||port>65535){
					port = 80;
				}
			}
			catch(Exception e){
				port = 80;
			}
			String encoding = "ASCII";
			if(args.length>2){
				encoding = args[2];
			}
			Thread t = new SingleFileHTTPServer(data,encoding,contentType,port);
			t.start();
		}
		catch (FileNotFoundException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
