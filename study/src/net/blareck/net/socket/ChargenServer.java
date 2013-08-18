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
 * @date 2013-8-19
 * @version 1.0 
 */
public class ChargenServer
{
	private static int DEFAULT_PORT = 19;
	public static void main(String[] args){
		int port;
		try
		{			
			port = Integer.parseInt(args[0]);
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			port = DEFAULT_PORT;
		}
		System.out.println("Listening for connections on port " + port);
		byte[] rotation = new byte[95*2];
		for(byte i = ' ';i<='~';i++){
			rotation[i-' '] = i;
			rotation[i+95 - ' '] = i;
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
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		
		while(true){
			try{
				selector.select();
			}catch(Exception e){
				e.printStackTrace();
				break;
			}
			
			Set<?> readyKeys = selector.selectedKeys();
			for(Iterator<?> iterator = readyKeys.iterator();iterator.hasNext();){
				SelectionKey key = (SelectionKey)iterator.next();
				iterator.remove();
				try
				{
					if(key.isAcceptable()){
						ServerSocketChannel server = (ServerSocketChannel)key.channel();
						SocketChannel client = server.accept();
						System.out.println("");
						client.configureBlocking(false);
						SelectionKey key2 = client.register(selector, SelectionKey.OP_WRITE);
						ByteBuffer buffer = ByteBuffer.allocate(74);
						buffer.put(rotation,0,72);
						buffer.put((byte)'\r');
						buffer.put((byte)'\n');
						buffer.flip();
						key2.attach(buffer);
					}
					else if(key.isWritable()){
						SocketChannel client = (SocketChannel)key.channel();
						ByteBuffer buffer = (ByteBuffer) key.attachment();
						if(!buffer.hasRemaining()){
							buffer.rewind();
							int first = buffer.get();
							buffer.rewind();
							int position = first - ' ' +1;
							buffer.put(rotation,position,72);
							buffer.put((byte)'\r');
							buffer.put((byte)'\n');
							buffer.flip();
						}
						client.write(buffer);
					}
				}
				catch (Exception e)
				{
					// TODO Auto-generated catch block
					key.cancel();
					try{
						key.channel().close();
					}
					catch(IOException ex){}
					e.printStackTrace();
				}
			}
		}
	}
}
