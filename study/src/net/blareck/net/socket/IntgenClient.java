package net.blareck.net.socket;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.channels.SocketChannel;

/**
 * 
 * @author blareck
 * @date 2013-8-29
 * @version 1.0 
 */
public class IntgenClient
{
	public static int DEFAULT_PORT = 1919;
	
	public static void main(String[] args){
		if(args.length==0){
			System.out.println("U");
			return;
		}
		
		int port;
		try{
			port = Integer.parseInt(args[1]);
		}
		catch(Exception e){
			port = DEFAULT_PORT;
		}
		
		try
		{
			SocketAddress address = new InetSocketAddress(args[0],port);
			SocketChannel client = SocketChannel.open(address);
			ByteBuffer buffer = ByteBuffer.allocate(4);
			IntBuffer view = buffer.asIntBuffer();
			for(int expected = 0;;expected++){
				client.read(buffer);
				int actual = view.get();
				buffer.clear();
				view.rewind();
				if(actual != expected){
					System.err.println();
					break;
				}
				System.out.println(actual);
			}
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
}
