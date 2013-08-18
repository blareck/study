package net.blareck.net.socket;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.Socket;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 
 * @author blareck
 * @date 2013-8-14
 * @version 1.0
 */
public class RequestProcessor implements Runnable
{
	private static List<Socket> pool = new LinkedList<Socket>();
	private File documentRootDirectory;
	private String indexFileName = "index.html";
	private final static Lock lock = new ReentrantLock();

	public RequestProcessor(File documentRootDirectory, String indexFileName)
	{
		if (documentRootDirectory.isFile())
		{
			throw new IllegalArgumentException("documentRootDirectory must be a directory, not a file");
		}

		this.documentRootDirectory = documentRootDirectory;
		try
		{
			this.documentRootDirectory = documentRootDirectory.getCanonicalFile();
		}
		catch (IOException e)
		{
		}
		if (indexFileName != null)
			this.indexFileName = indexFileName;
	}

	public static void processRequest(Socket request)
	{
		lock.lock();
		try
		{
			pool.add(pool.size(), request);
			pool.notifyAll();
		}
		finally
		{
			lock.unlock();
		}
	}

	public static String guessContentTypeFromName(String name)
	{
		if (name.endsWith(".html") || name.endsWith(".htm"))
		{
			return "text/html";
		}
		else if (name.endsWith(".txt") || name.endsWith(".java"))
		{
			return "text/plain";
		}
		else if (name.endsWith(".gif"))
		{
			return "image/gif";
		}
		else if (name.endsWith(".class"))
		{
			return "application/octet-stream";
		}
		else if (name.endsWith(".jpg") || name.endsWith(".jpeg"))
		{
			return "image/jpeg";
		}
		else
		{
			return "text/plain";
		}
	}

	@Override
	public void run()
	{
		// TODO Auto-generated method stub
		String root = documentRootDirectory.getPath();

		while (true)
		{
			Socket connection;
			lock.lock();
			try
			{
				while (pool.isEmpty())
				{
					try
					{
						pool.wait();
					}
					catch (InterruptedException e)
					{

					}
				}
				connection = (Socket) pool.remove(0);
			}
			finally
			{
				lock.unlock();
			}

			try
			{
				String filename = "";
				String contentType;
				OutputStream raw = new BufferedOutputStream(connection.getOutputStream());
				Writer out = new OutputStreamWriter(raw);
				Reader in = new InputStreamReader(new BufferedInputStream(connection.getInputStream()), "ASCII");
				StringBuffer requestLine = new StringBuffer();
				int c;
				while (true)
				{
					c = in.read();
					if (c == '\r' || c == '\n')
						break;
					requestLine.append((char) c);
				}
				String get = requestLine.toString();
				System.out.println(get);

				StringTokenizer st = new StringTokenizer(get);
				String method = st.nextToken();
				String version = "";
				if ("GET".equals(method))
				{
					filename = st.nextToken();
					if (filename.endsWith("/"))
						filename += indexFileName;

					contentType = guessContentTypeFromName(filename);
					if (st.hasMoreTokens())
					{
						version = st.nextToken();
					}

					File theFile = new File(documentRootDirectory, filename.substring(1, filename.length()));
					if (theFile.canRead() && theFile.getCanonicalPath().startsWith(root))
					{
						DataInputStream fis = new DataInputStream(new BufferedInputStream(new FileInputStream(theFile)));
						byte[] theData = new byte[(int) theFile.length()];
						fis.readFully(theData);
						fis.close();
						if (version.startsWith("HTTP "))
						{
							out.write("HTTP/1.0 200 OK\r\n");
							Date now = new Date();
							out.write("Date: " + now + "\r\n");
							out.write("Server: JHTTP/1.0\r\n");
							out.write("Content-length: " + theData.length + "\r\n");
							out.write("Content-type: " + contentType + "\r\n\r\n");
							out.flush();
						}
						raw.write(theData);
						raw.flush();
					}
					else
					{
						if (version.startsWith("HTTP "))
						{
							out.write("HTTP/1.0 404 File Not Found\r\n");
							Date now = new Date();
							out.write("Date: " + now + "\r\n");
							out.write("Server: JHTTP/1.0\r\n");
							out.write("Content-type: text/html\r\n\r\n");
							out.flush();
						}
						out.write("<HTML>\r\n");
						out.write("<HEAD><TITLE>File Not Found</TITLE>\r\n");
						out.write("</HEAD>\r\n");
						out.write("<BODY>\r\n");
						out.write("<H1>HTTP Error 404 : File Not Found</H1>\r\n");
						out.write("</BODY></HTML>\r\n");
						out.flush();
					}
				}
				else
				{
					if (version.startsWith("HTTP "))
					{
						out.write("HTTP/1.0 501 Not Implemented\r\n");
						Date now = new Date();
						out.write("Date: " + now + "\r\n");
						out.write("Server: JHTTP/1.0\r\n");
						out.write("Content-type: text/html\r\n\r\n");
						out.flush();
					}
					out.write("<HTML>\r\n");
					out.write("<HEAD><TITLE>Not Implemented</TITLE>\r\n");
					out.write("</HEAD>\r\n");
					out.write("<BODY>\r\n");
					out.write("<H1>HTTP Error 501 : Not Implemented</H1>\r\n");
					out.write("</BODY></HTML>\r\n");
					out.flush();
				}
			}
			catch (IOException e)
			{
			}
			finally
			{
				try
				{
					connection.close();
				}
				catch (IOException e)
				{
				}
			}
		}
	}

}
