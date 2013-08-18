package net.blareck.net.socket;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

/**
 * 
 * @author blareck
 * @date 2013-9-5
 * @version 1.0
 */
public class NonblockingSingleFileHTTPServer
{
	private ByteBuffer contentBuffer;
	private int port = 80;

	public NonblockingSingleFileHTTPServer(ByteBuffer data, String encoding, String MIMEType, int port)
			throws UnsupportedEncodingException
	{
		this.port = port;
		String header = "HTTP/1.0 200 OK\r\n";
		byte[] headerData = header.getBytes("ASCII");

		ByteBuffer buffer = ByteBuffer.allocate(data.limit() + headerData.length);
		buffer.put(headerData);
		buffer.put(data);
		buffer.flip();
		this.contentBuffer = buffer;
	}

	public void run() throws IOException
	{
		ServerSocketChannel serverChannel = ServerSocketChannel.open();
		ServerSocket serverSocket = serverChannel.socket();
		Selector selector = Selector.open();
		InetSocketAddress localPort = new InetSocketAddress(port);
		serverSocket.bind(localPort);
		serverChannel.configureBlocking(false);
		serverChannel.register(selector, SelectionKey.OP_ACCEPT);

		while (true)
		{
			selector.select();
			Iterator<SelectionKey> keys = selector.selectedKeys().iterator();
			while (keys.hasNext())
			{
				SelectionKey key = keys.next();
				try
				{
					if (key.isAcceptable())
					{
						ServerSocketChannel server = (ServerSocketChannel) key.channel();
						SocketChannel channel = server.accept();
						channel.configureBlocking(false);
						SelectionKey newKey = channel.register(selector, SelectionKey.OP_READ);
					}
					else if (key.isReadable())
					{
						SocketChannel channel = (SocketChannel) key.channel();
						ByteBuffer output = ByteBuffer.allocate(4096);
						channel.read(output);
						key.interestOps(SelectionKey.OP_WRITE);
						key.attach(contentBuffer.duplicate());
					}
					else if (key.isWritable())
					{
						SocketChannel channel = (SocketChannel) key.channel();
						ByteBuffer buffer = (ByteBuffer) key.attachment();
						if (buffer.hasRemaining())
						{
							channel.write(buffer);
						}
						else
						{
							channel.close();
						}
					}
				}
				catch (IOException e)
				{

				}
			}
		}
	}
	
	public static void main(String[] args){
		if(args.length == 0){
			return;
		}
		
		try{
			String contentType = "text/plain";
			if(args[0].endsWith(".html")||args[0].endsWith(".htm"))
			{
				contentType = "text/html";
			}
			
			FileInputStream fin = new FileInputStream(args[0]);
			FileChannel in = fin.getChannel();
			ByteBuffer input = in.map(FileChannel.MapMode.READ_ONLY, 0, in.size());
			int port;
			try{
				port = Integer.parseInt(args[1]);
				if(port <1 || port > 65535)
					port = 80;
			}
			catch(Exception e){
				port = 80;
			}
			String encoding = "ASCII";
			if(args.length>2) 
				encoding =args[2];
			NonblockingSingleFileHTTPServer server = new NonblockingSingleFileHTTPServer(input,encoding,contentType,port);
			server.run();
		}
		catch(IOException e)
		{
			
		}
	}
}
