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
 * @date 2013-9-3
 * @version 1.0
 */
public class EchoServer
{
	public static int DEFAULT_PORT = 7;

	public static void main(String[] args)
	{
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

		while (true)
		{
			try
			{
				selector.select();
			}
			catch (IOException e)
			{
				e.printStackTrace();
				break;
			}
			Set<SelectionKey> ReadyKeys = selector.selectedKeys();
			for (Iterator<SelectionKey> it = ReadyKeys.iterator(); it.hasNext();)
			{
				SelectionKey key = it.next();
				it.remove();
				try
				{
					if (key.isAcceptable())
					{
						ServerSocketChannel server = (ServerSocketChannel) key.channel();
						SocketChannel client = server.accept();
						client.configureBlocking(false);
						SelectionKey clientkey = client
								.register(selector, SelectionKey.OP_WRITE | SelectionKey.OP_READ);
						ByteBuffer buffer = ByteBuffer.allocate(100);
						clientkey.attach(buffer);
					}
					if (key.isReadable())
					{
						SocketChannel client = (SocketChannel) key.channel();
						ByteBuffer output = (ByteBuffer) key.attachment();
						client.read(output);
					}
					if (key.isWritable())
					{
						SocketChannel client = (SocketChannel) key.channel();
						ByteBuffer output = (ByteBuffer) key.attachment();
						output.flip();
						client.write(output);
						output.compact();
					}
				}
				catch (IOException e)
				{
					key.cancel();
					try
					{
						key.channel().close();
					}
					catch (IOException ex)
					{

					}
				}
			}
		}

	}
}
