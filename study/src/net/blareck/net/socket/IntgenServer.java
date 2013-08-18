package net.blareck.net.socket;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * 
 * @author blareck
 * @date 2013-8-25
 * @version 1.0 
 */
public class IntgenServer
{
	public static int DEFAULT_PORT = 1919;
	
	public static void main(String[] args){
		int port;
		
		try
		{
			port = Integer.parseInt(args[0]);
		}
		catch (NumberFormatException e)
		{
			// TODO Auto-generated catch block
			port = DEFAULT_PORT;
		}
		ServerSocketChannel serverChannel;
		Selector selector;
		
		try
		{
			serverChannel = ServerSocketChannel.open();
			ServerSocket ss = serverChannel.socket();
			InetSocketAddress address = new InetSocketAddress(port);
			ss.bind(address);
			serverChannel.configureBlocking(false);
			selector = Selector.open();
			serverChannel.register(selector, SelectionKey.OP_ACCEPT);
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		
		while(true){
			try{
				selector.select();
			}
			catch(Exception e){
				e.printStackTrace();
				break;
			}
			Set<?> readyKeys = selector.selectedKeys();
			Iterator<?> iterator = readyKeys.iterator();
			while(iterator.hasNext()){
				SelectionKey key = (SelectionKey)iterator.next();
				iterator.remove();
				try
				{
					if(key.isAcceptable()){
						ServerSocketChannel server = (ServerSocketChannel)key.channel();
						SocketChannel client = server.accept();
						System.out.println();
						client.configureBlocking(false);
						SelectionKey key2 = client.register(selector, SelectionKey.OP_WRITE);
						ByteBuffer output = ByteBuffer.allocate(4);
						output.putInt(0);
						output.flip();
						key2.attach(output);
					}
					else if(key.isWritable()){
						SocketChannel client = (SocketChannel)key.channel();
						ByteBuffer output = (ByteBuffer) key.attachment();
						if(!output.hasRemaining()){
							output.rewind();
							int value = output.getInt();
							output.clear();
							output.putInt(value+1);
							output.flip();
						}
						client.write(output);
					}
				}
				catch (Exception e)
				{
					// TODO Auto-generated catch block
					key.cancel();
					try{
						key.channel().close();
					}
					catch(IOException ex){
						ex.printStackTrace();
					}
					e.printStackTrace();
				}
			}
		}
	}
}
