package net.blareck.net.socket;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;

/**
 * 
 * @author blareck
 * @date 2013-8-18
 * @version 1.0 
 */
public class SecureOrderTaker
{
	public final static int DEFAULT_PORT = 7000;
	public final static String algorithm = "SSL";
	
	public static void main(String[] args){
		int port = DEFAULT_PORT;
		try{
			SSLContext context = SSLContext.getInstance(algorithm);
			KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
			KeyStore ks = KeyStore.getInstance("JKS");
			char[] password = "".toCharArray();
			ks.load(new FileInputStream(""),password);
			kmf.init(ks,password);
			context.init(kmf.getKeyManagers(), null, null);
			SSLServerSocketFactory factory = context.getServerSocketFactory();
			
			SSLServerSocket server = (SSLServerSocket) factory.createServerSocket(port); 
			String[] supported = server.getSupportedCipherSuites();
			String[] anonCipherSuitesSupported = new String[supported.length];
			int numAnonCipherSuitesSupported = 0;
			for(int i = 0;i<supported.length;i++){
				if(supported[i].indexOf("_anon_")>0){
					anonCipherSuitesSupported[numAnonCipherSuitesSupported++] = supported[i];
				}
			}
		}
		catch(IOException e){
			
		}
		catch(KeyManagementException e){
			
		}
		catch(KeyStoreException e){
			
		}
		catch (NoSuchAlgorithmException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (CertificateException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (UnrecoverableKeyException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
