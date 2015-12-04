import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyStore;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.xml.bind.DatatypeConverter;

public class SMTPServer extends Thread {

	public static String getMailServer(String receiverEmail) {
		
		if(receiverEmail.contains(Constants.GMAIL_ADDRESS)) {
			return Constants.GMAIL_SMTP_HOST;
		}
		else {
			//TODO: This is dead code for now. Enter EC2 public IP here
			return "localhost";
		}
	}
	
	public void sendEmail(String senderEmail, String receiverEmail, String emailMessage) {
		
		try
		{
			System.setProperty("javax.net.ssl.trustStore", Constants.KEYSTORE_PATH);
		    System.setProperty("javax.net.ssl.trustStorePassword", Constants.KEYSTORE_PASSWORD);
		    
		    String mailServer = getMailServer(receiverEmail);
		     
			// Establish a TCP connection with the mail server.
		    Socket sendEmailSocket = new Socket(mailServer, Constants.SMTP_PORT);

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
			//TODO: remove hard coded IP or use EC2 public IP
			String command = Constants.HELO_COMMAND + " 129.21.86.199" + Constants.MESSAGE_TERMINATION;
			os.write(command.getBytes("US-ASCII"));
			response = br.readLine();

			System.out.println(response);

			if (!response.startsWith("250")) {
				throw new Exception("250 reply not received from server.");
			}
			
			// Send MAIL FROM command.
			command = Constants.TLS_COMMAND + " " + Constants.MESSAGE_TERMINATION;
			os.write(command.getBytes("US-ASCII"));
			response = br.readLine();
			System.out.println(response);

			String keystoreFile = Constants.KEYSTORE_PATH;

			SSLContext sslContext = SSLContext.getInstance("TLSv1");
			String defaultAlgorithm = KeyManagerFactory.getDefaultAlgorithm();

			KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(defaultAlgorithm);
			String defaultKeyStoreType = KeyStore.getDefaultType();
			KeyStore keyStore = KeyStore.getInstance(defaultKeyStoreType);

			FileInputStream in = null;
			File actualKeystoreFile = new File(keystoreFile);
			try {
				in = new FileInputStream(actualKeystoreFile);
				keyStore.load(in, Constants.KEYSTORE_PASSWORD.toCharArray());
			} catch (IOException e) {
				System.out.println("cannot open key file");
			} finally {
				if (in != null) {
					in.close();
				}
			}

			keyManagerFactory.init(keyStore, Constants.KEYSTORE_PASSWORD.toCharArray());
			KeyManager[] keyManagers = keyManagerFactory.getKeyManagers();

			sslContext.init(keyManagers, null, null);


			SSLSocketFactory ssf = sslContext.getSocketFactory();
			SSLSocket s = (SSLSocket) ssf.createSocket(sendEmailSocket, mailServer, Constants.SMTP_SECURE_PORT, true);
			String[] st = s.getEnabledProtocols();

			s.setEnabledProtocols(st);

			s.setNeedClientAuth(false);  

			
			s.startHandshake();


			System.out.println("Handshake COMPLETE!");

			is = s.getInputStream();
			isr = new InputStreamReader(is);
			br = new BufferedReader(isr);
			os = s.getOutputStream();

			//TODO: remove hard coded IP
			command = Constants.HELO_COMMAND + " 129.21.86.11" + Constants.MESSAGE_TERMINATION;
			os.write(command.getBytes("US-ASCII"));
			response = br.readLine();
			System.out.println(response);

			command = Constants.MAIL_FROM_COMMAND + " " + senderEmail + Constants.MESSAGE_TERMINATION;
			os.write(command.getBytes("US-ASCII"));
			response = br.readLine();
			System.out.println("Mail from " + response);

			command = Constants.RCPT_TO_COMMAND + " " + receiverEmail + Constants.MESSAGE_TERMINATION;
			os.write(command.getBytes("US-ASCII"));
			response = br.readLine();
			System.out.println("Receipt " + response);

			command = "DATA"+ Constants.MESSAGE_TERMINATION;
			os.write(command.getBytes("US-ASCII"));
			response = br.readLine();
			System.out.println("Data command " + response);

			command = "From: " + senderEmail + Constants.MESSAGE_TERMINATION;
			os.write(command.getBytes("US-ASCII"));
			
			command = "To: " + receiverEmail + Constants.MESSAGE_TERMINATION;
			os.write(command.getBytes("US-ASCII"));
			
			//TODO: add subject param to this method
			command = "Subject: This is a Hello Message" + Constants.MESSAGE_TERMINATION;
			os.write(command.getBytes("US-ASCII"));
			
			command = emailMessage + Constants.MESSAGE_TERMINATION;
			os.write(command.getBytes("US-ASCII"));


			command = Constants.SENDING_TERMINATION + Constants.MESSAGE_TERMINATION;
			os.write(command.getBytes("US-ASCII"));
			response = br.readLine();

			System.out.println("Message Sent Successfully " + response);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public void receiveEmail() {

		while (true) {

			try {
				//TODO: add synchronization for port 25
				ServerSocket receive = new ServerSocket(25);

				Socket socket = receive.accept();

				InputStream is = socket.getInputStream();
				InputStreamReader isr = new InputStreamReader(is);
				BufferedReader br = new BufferedReader(isr);
				OutputStream os = socket.getOutputStream();
				String command = null;

				command = Constants.SERVICE_READY + " SMTP service ready" + Constants.MESSAGE_TERMINATION;

				os.write(command.getBytes("US-ASCII"));

				System.out.println(br.readLine());

				command = Constants.OPERATION_COMPLETE + " Ready to go" + Constants.MESSAGE_TERMINATION;

				os.write(command.getBytes("US-ASCII"));

				String receivedResponse = "";

				while (receivedResponse.equals(Constants.QUIT_COMMAND) == false) {
					receivedResponse = br.readLine();
					System.out.println(receivedResponse);

					command = Constants.OPERATION_COMPLETE + " " + Constants.MESSAGE_TERMINATION;
					os.write(command.getBytes("US-ASCII"));
				}

				System.out.println("Received EMAIL Successfully!");
				socket.close();
				receive.close();
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public void run() {
		receiveEmail();
	}

	public static void main(String[] args) throws Exception {
		
		SMTPServer receivingThread = new SMTPServer();
		
		receivingThread.start();
		
		//TODO: call this from the UI
		new SMTPServer().sendEmail("<omkar@129.21.86.208>", "<omkarhegde2806@gmail.com>", "This is a new test");
		
	}
}