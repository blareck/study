package net.blareck.net.socket;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.SocketChannel;
import java.nio.channels.WritableByteChannel;

/**
 * 
 * @author blareck
 * @date 2013-8-18
 * @version 1.0 
 */
public class ChargenClient
{
	public static int DEFAULT_PORT = 19;
	
	public static void main(String[] args){
		int port = DEFAULT_PORT;
		try
		{
			SocketAddress address = new InetSocketAddress(args[0],port);
			SocketChannel client = SocketChannel.open(address);
			ByteBuffer buffer = ByteBuffer.allocate(74);
			WritableByteChannel out = Channels.newChannel(System.out);
			
			while(client.read(buffer) != -1){
				buffer.flip();
				out.write(buffer);
				buffer.clear();
			}
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
