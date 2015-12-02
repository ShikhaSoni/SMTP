
import java.io.*;

import java.net.*;
import java.security.KeyStore;
import javax.net.ssl.*;
import javax.xml.bind.DatatypeConverter;

public class SMTPServer extends Thread {

	private final static int SMTP_PORT = 25;
	private final static String MAIL_SERVER = "smtp.gmail.com";
	private final static String SENDER_EMAIL = "<fcnprojectsmtp@gmail.com>";
	private final static String RECEIVER_EMAIL = "<omkarhegde2806@gmail.com>";
	private final static String EMAIL_MESSAGE = "This is a test email agent!";
	
	private int threadID;
	
	public SMTPServer(int threadID) {
		this.threadID = threadID;
	}
	
	public void sendEmail() {
		
		try
		{
			System.setProperty("javax.net.ssl.trustStore","C:\\Program Files\\Java\\jre7\\lib\\security\\cacerts");
		    System.setProperty("javax.net.ssl.trustStorePassword","changeit");
		     
			// Establish a TCP connection with the mail server.
		    Socket sendEmailSocket = new Socket(MAIL_SERVER, SMTP_PORT);

			// Create a BufferedReader to read a line at a time.
			InputStream is = sendEmailSocket.getInputStream();
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr);

			// Read greeting from the server.
			String response = br.readLine();
			System.out.println(response);
			if (!response.startsWith("220")) {
				throw new Exception("220 reply not received from server.");
			}

			// Get a reference to the socket's output stream.
			OutputStream os = sendEmailSocket.getOutputStream();

			// Send HELO command and get server response.
			String command = "HELO " + "129.21.86.199"+"\r\n";
			os.write(command.getBytes("US-ASCII"));
			response = br.readLine();

			System.out.println(response);

			if (!response.startsWith("250")) {
				throw new Exception("250 reply not received from server.");
			}
			// Send MAIL FROM command.
			command = "STARTTLS" +"\r\n";
			os.write(command.getBytes("US-ASCII"));
			response = br.readLine();
			System.out.println(response);

			String keystoreFile = "C:\\Program Files\\Java\\jre7\\lib\\security\\cacerts";

			SSLContext sslContext = SSLContext.getInstance("TLSv1");
			String defaultAlgorithm = KeyManagerFactory.getDefaultAlgorithm();

			KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(defaultAlgorithm);
			String defaultKeyStoreType = KeyStore.getDefaultType();
			KeyStore keyStore = KeyStore.getInstance(defaultKeyStoreType);

			FileInputStream in = null;
			File actualKeystoreFile = new File(keystoreFile);
			try {
				in = new FileInputStream(actualKeystoreFile);
				keyStore.load(in, "changeit".toCharArray());
			} catch (IOException e) {
				System.out.println("cannot open key file");
			} finally {
				if (in != null) {
					in.close();
				}
			}

			keyManagerFactory.init(keyStore, "changeit".toCharArray());
			KeyManager[] keyManagers = keyManagerFactory.getKeyManagers();

			sslContext.init(keyManagers, null, null);



			SSLSocketFactory ssf = sslContext.getSocketFactory();//(SSLSocketFactory) SSLSocketFactory.getDefault();
			SSLSocket s = (SSLSocket) ssf.createSocket(sendEmailSocket, "smtp.gmail.com", 465, true);
			String[] st = s.getEnabledProtocols();

			s.setEnabledProtocols(st);

			s.setNeedClientAuth(false);  
			
			s.startHandshake();


			System.out.println("Handshake COMPLETE!");

			is = s.getInputStream();
			isr = new InputStreamReader(is);
			br = new BufferedReader(isr);
			os = s.getOutputStream();


			command = "HELO " + "129.21.84.87"+"\r\n";
			os.write(command.getBytes("US-ASCII"));
			response = br.readLine();
			System.out.println(response);

			// Send RCPT TO command.
			command = "AUTH LOGIN"+"\r\n";
			os.write(command.getBytes("US-ASCII"));
			response = br.readLine();
			System.out.println(response);

			String message = "fcnprojectsmtp";
			String encoded = DatatypeConverter.printBase64Binary(message.getBytes("UTF-8"));


			// Send DATA command.
			command = encoded+"\r\n";
			os.write(command.getBytes("US-ASCII"));
			response = br.readLine();
			System.out.println("Username Response " + response);


			message = "thisisapassword";
			encoded = DatatypeConverter.printBase64Binary(message.getBytes("UTF-8"));
			// Send message data.
			command = encoded+"\r\n";
			os.write(command.getBytes("US-ASCII"));
			response = br.readLine();
			System.out.println("Password Response " +response);

			command = "MAIL FROM: "+SENDER_EMAIL+"\r\n";
			os.write(command.getBytes("US-ASCII"));
			response = br.readLine();
			System.out.println("Mail from " + response);

			// End with line with a single period.
			command = "RCPT TO: "+RECEIVER_EMAIL+"\r\n";
			os.write(command.getBytes("US-ASCII"));
			response = br.readLine();
			System.out.println("Receipt " + response);


			// Send QUIT command.
			command = "DATA"+"\r\n";
			os.write(command.getBytes("US-ASCII"));
			response = br.readLine();
			System.out.println("Data command " + response);

			command = EMAIL_MESSAGE+"\r\n";
			os.write(command.getBytes("US-ASCII"));
			//response = br.readLine();
			//System.out.println("Message " + response);

			command = "\r\n."+"\r\n";
			os.write(command.getBytes("US-ASCII"));
			response = br.readLine();


			//os.write(command.getBytes("US-ASCII"));
			//response = br.readLine();
			System.out.println("DONE " + response);

		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public void receiveEmail() {
		
		try {
			ServerSocket receive = new ServerSocket(25);
			Socket test = receive.accept();
	
			InputStream is = test.getInputStream();
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr);
			OutputStream os = test.getOutputStream();
	
			os.write("220 129.21.85.163 SMTP service ready\r\n".getBytes("US-ASCII"));
	
			System.out.println(br.readLine());
			
			os.write("250 Ready to go\r\n".getBytes("US-ASCII"));
			
			while(true) {
				System.out.println(br.readLine());
				
				os.write("250\r\n".getBytes("US-ASCII"));
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public void run() {
		if(threadID == 1) {
			sendEmail();
		}
		else {
			receiveEmail();
		}
	}

	public static void main(String[] args) throws Exception {
		
		SMTPServer sendingThread = new SMTPServer(1);
		SMTPServer receivingThread = new SMTPServer(2);
		
		sendingThread.start();
		receivingThread.start();
		
	}
}