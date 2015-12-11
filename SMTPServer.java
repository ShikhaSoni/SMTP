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
import java.util.ArrayList;
import java.util.Hashtable;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.xml.bind.DatatypeConverter;

public class SMTPServer extends Thread {
	
	public static boolean isGmail = false;
	public static Hashtable<Client, ArrayList<Email>> emailStorage =  new Hashtable< Client, ArrayList<Email>>();

	public static String getMailServer(String receiverEmail) {
		
		if(receiverEmail.contains(Constants.GMAIL_ADDRESS)) {
			isGmail = true;
			return Constants.GMAIL_SMTP_HOST;
		}
		else {
			return "52.35.127.215";
		}
	}
	
	public void sendEmail(Email mailToSend) {
		
		try
		{

			System.setProperty("javax.net.ssl.trustStore", Constants.KEYSTORE_PATH);
		    System.setProperty("javax.net.ssl.trustStorePassword", Constants.KEYSTORE_PASSWORD);
		    
		    Client test = new Client(mailToSend.getFrom().trim());
		    System.out.println(test.getUsername() + ":" + emailStorage.size());
			if(emailStorage.containsKey(test)) {
				System.out.println("hitting");
				emailStorage.get(new Client(mailToSend.getFrom().trim())).add(mailToSend);
			}else {
				System.out.println("not added");
			}
		    
		    String mailServer = getMailServer(mailToSend.getTo());
		     
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
			
			if(isGmail == true ) {
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
				SSLSocket s = (SSLSocket) ssf.createSocket(sendEmailSocket, Constants.GMAIL_SMTP_HOST, Constants.SMTP_SECURE_PORT, true);
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
			}

			command = Constants.MAIL_FROM_COMMAND + " " + mailToSend.getFrom() + Constants.MESSAGE_TERMINATION;
			os.write(command.getBytes("US-ASCII"));
			response = br.readLine();
			System.out.println("Mail from " + response);

			command = Constants.RCPT_TO_COMMAND + " " + mailToSend.getTo() + Constants.MESSAGE_TERMINATION;
			os.write(command.getBytes("US-ASCII"));
			response = br.readLine();
			System.out.println("Receipt " + response);

			command = "DATA"+ Constants.MESSAGE_TERMINATION;
			os.write(command.getBytes("US-ASCII"));
			response = br.readLine();
			System.out.println("Data command " + response);

			command = "From: " + mailToSend.getFrom() + Constants.MESSAGE_TERMINATION;
			os.write(command.getBytes("US-ASCII"));
			
			command = "To: " + mailToSend.getTo() + Constants.MESSAGE_TERMINATION;
			os.write(command.getBytes("US-ASCII"));
			
			//TODO: add subject param to this method
			command = "Subject: " + mailToSend.getSubject() + Constants.MESSAGE_TERMINATION;
			os.write(command.getBytes("US-ASCII"));
			
			command = mailToSend.getContent() + Constants.MESSAGE_TERMINATION;
			os.write(command.getBytes("US-ASCII"));
			
			command = Constants.SENDING_TERMINATION + Constants.MESSAGE_TERMINATION;
			os.write(command.getBytes("US-ASCII"));
			response = br.readLine();

			command = Constants.QUIT_COMMAND + Constants.MESSAGE_TERMINATION;
			os.write(command.getBytes("US-ASCII"));

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
				String senderEmail = null, toEmail = null, subject = null, dateAndTime = null, messageBody = null, messageID = null;

				command = Constants.SERVICE_READY + " SMTP service ready" + Constants.MESSAGE_TERMINATION;

				os.write(command.getBytes("US-ASCII"));

				System.out.println(br.readLine());

				command = Constants.OPERATION_COMPLETE + " Ready to go" + Constants.MESSAGE_TERMINATION;

				os.write(command.getBytes("US-ASCII"));

				String receivedResponse = "";

				while (receivedResponse.equals(Constants.QUIT_COMMAND) == false) {
					receivedResponse = br.readLine();
					System.out.println(receivedResponse);
					
					String[] splitResponse = receivedResponse.split(":");
					
					if(splitResponse.length >= 2) {
						if(splitResponse[0].equals("MAIL FROM")) {
							senderEmail = splitResponse[1];
						}
						else if(splitResponse[0].equals("RCPT TO")) {
							toEmail = splitResponse[1];
						}
						else if(splitResponse[0].equals("Date")) {
							dateAndTime = splitResponse[1];
						}
						else if(splitResponse[0].equals("Message-ID")) {
							messageID = splitResponse[1];
						}
						else if(splitResponse[0].equals("Subject")) {
							subject = splitResponse[1];
							command = Constants.OPERATION_COMPLETE + " " + Constants.MESSAGE_TERMINATION;
							os.write(command.getBytes("US-ASCII"));
							messageBody = br.readLine();
							System.out.println("BODY: "+ messageBody);
						}					
					}

					command = Constants.OPERATION_COMPLETE + " " + Constants.MESSAGE_TERMINATION;
					os.write(command.getBytes("US-ASCII"));
					
				}
				
				Email receivedMail = new Email(messageID, toEmail, senderEmail, messageBody, dateAndTime, subject);

				System.out.println(receivedMail.getContent() + " " +receivedMail.getFrom() + " " + receivedMail.getTo() + " " + receivedMail.getSubject());
				sendEmail(receivedMail);

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
		
		//TODO: change IP to EC2 instance IP
		emailStorage.put(new Client("<omkar@129.21.85.33>","omkar"), new ArrayList<Email>());
		emailStorage.put(new Client("<shikha@129.21.85.33>","shikha"), new ArrayList<Email>());
		SMTPServer receivingThread = new SMTPServer();
		receivingThread.start();
		
		new IMAP().start();
		
	}
}