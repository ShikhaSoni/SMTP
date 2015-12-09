import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

public class IMAP extends Thread {

	ServerSocket IMAP_Socket;
	Socket clientSocket;

	public IMAP() {
		try {
			IMAP_Socket = new ServerSocket(Constants.IMAP_PORT);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public static void main(String args[]){
		new IMAP().start();
	}

	public void run() {
		while (true) {
			try {
				clientSocket = IMAP_Socket.accept();
				System.out.println(clientSocket);
				new ClientRequest(clientSocket).start();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	class ClientRequest extends Thread {
		Socket clientSocket;
		private BufferedReader reader;
		private DataOutputStream writer;
		private ArrayList<Email> emails = null;
		private ArrayList<Email> InboxEmails = null;
		private ArrayList<Email> sentEmails = null;

		public ClientRequest(Socket clientSocket) {
			this.clientSocket = clientSocket;
			try {
				writer= new DataOutputStream(clientSocket.getOutputStream());
				reader= new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		public void run() {
			System.out.println(">" + Thread.currentThread().toString()
					+ clientSocket.getPort() + " "
					+ clientSocket.getLocalPort());
			try {
				System.out.println(reader.readLine());
				writer.write(("* OK ready for requests"+ '\n').getBytes("US-ASCII"));
				writer.flush();
				System.out.println("waiting");
				String auth = reader.readLine();
				System.out.println(auth);
				String loginString = "a1 login";
				String user;
				String pass;
				String[] userpass = null;
				if (!auth.contains(loginString)) {
					writer.writeBytes(("Wrong command" + '\n'));
					return;
				}
				userpass = auth.substring(loginString.length()).split(" ");
				user = userpass[0];
				pass = userpass[1];
				boolean exists = false;
				for (Client c : SMTPServer.emailStorage.keySet()) {
					if (c.getUsername().equals(user)) {
						if (c.getPassword().equals(pass)) {
							writer.writeBytes("> a1 OK " + user + " Logged in SUCCESS "+ '\n');
							emails = SMTPServer.emailStorage.get(c);
						}
						exists = true;
						break;
					}
				}
				if (!exists) {
					writer.writeBytes(">No user found");
					return;
				}
				for (Email email : emails) {
					if (email.getFrom().equals(user))
						sentEmails.add(email);
					else
						InboxEmails.add(email);
				}
				if (reader.readLine().equalsIgnoreCase("a2 LIST \"\" \"*\""))
					writer.write((">* LIST \".\" \" INBOX\" \"SENT\" ").getBytes());

				String next = reader.readLine();
				String selected = null;
				
				//this should be twice
				if (next.contains("EXAMINE") && next.startsWith("a3")) {
					if (next.contains("INBOX")) {
						writer.write((">* OK Read-only mailbox").getBytes());
						selected = "INBOX";
						writer.write((">* " + InboxEmails.size() + " EXITS").getBytes());
						writer.write(("> a3 OK [READ-ONLY] Select completed.").getBytes());
					} else if (next.contains("SENT")) {
						writer.write((">* OK Read-only mailbox").getBytes());
						selected = "SENT";
						writer.write((">* " + sentEmails.size() + " EXITS").getBytes());
						writer.write(("> a3 OK [READ-ONLY] Select completed.").getBytes());
					}
				}
				
				if ((next=reader.readLine()).contains("FETCH")&& next.startsWith("a4")) {
					//com.substring(com.indexOf("FETCH"));
					int i=1;
					if (selected.equals("INBOX")) {
						writer.write((">Inbox").getBytes());
						for (Email email : InboxEmails) {
							writer.write(("* " + (i + 1) + " FETCH (BODY[]").getBytes());
							i++;
							writer.write((email.getID() + ":" + email.getTimeStamp()
									+ ":" + email.getFrom() + ":"
									+ email.getContent()).getBytes());
						}
					} else {
						writer.write((">Sent Mails").getBytes());
						for (Email email : sentEmails) {
							writer.write(("* " + (i + 1) + " FETCH (BODY[]").getBytes());
							i++;
							writer.write((email.getID() + ":" + email.getTimeStamp()
									+ ":" + email.getTo() + ":"
									+ email.getContent()).getBytes());
						}
					}
				}
				writer.write(("a4 OK Fetching complete").getBytes());
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
