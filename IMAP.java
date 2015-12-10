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
	/*public static void main(String args[]){
		new IMAP().start();
	}*/

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
		private ArrayList<Email> InboxEmails = new ArrayList<>();
		private ArrayList<Email> sentEmails = new ArrayList<>();

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
				String auth = reader.readLine();
				System.out.println(auth);
				String loginString = "a1 LOGIN";
				String user;
				String pass;
				String[] userpass = null;
				if (!auth.contains(loginString)) {
					writer.writeBytes(("Wrong command" + '\n'));
					return;
				}
				userpass = auth.substring(loginString.length()).split(" ");
				user = userpass[1];
				pass = userpass[2];
				boolean exists = false;
				writer.writeBytes("> a1 OK " + user + " Logged in SUCCESS "+ '\n');
				/*for (Client c : SMTPServer.emailStorage.keySet()) {
					if (c.getUsername().equals(user)) {
						if (c.getPassword().equals(pass)) {
							writer.writeBytes("> a1 OK " + user + " Logged in SUCCESS "+ '\n');
							System.out.println("SUCCESS sent");
							emails = SMTPServer.emailStorage.get(c);
						}
						exists = true;
						break;
					}
				}*/
				emails = SMTPServer.emailStorage.get(new Client("<omkar@129.21.85.33>"));
				System.out.println("Emails rec: "+emails.size()+"--------------");
				/*if (!exists) {
					writer.writeBytes(">No user found");
					return;
				}*/
				/*for (Email email : emails) {
					System.out.println(email.getFrom()+":"+user);
					if (email.getFrom().equals(user)){
						sentEmails.add(email);
					}
					else
						InboxEmails.add(email);
				}*/
				//System.out.println("Segragated");
				/*if (reader.readLine().equalsIgnoreCase("a2 LIST \"\" \"*\""))
					writer.write((">* LIST \".\" \" INBOX\" \"SENT\" ").getBytes());
*/
				String next = reader.readLine();
				System.out.println("Client: "+next);
				String selected = null;
				//this should be twice
				if (next.contains("EXAMINE") && next.startsWith("a3")) {
					System.out.println("Examine command"+'\n');
					//if (next.contains("INBOX")) {
						writer.write((">* OK Read-only mailbox"+'\n').getBytes());
						selected = "INBOX";
						writer.write((">* " + emails.size() + " EXITS"+'\n').getBytes());
						writer.write(("> a3 OK [READ-ONLY] Select completed."+'\n').getBytes());
					/*} else if (next.contains("SENT")) {
						writer.write((">* OK Read-only mailbox"+'\n').getBytes());
						selected = "SENT";
						writer.write((">* " + sentEmails.size() + " EXITS"+'\n').getBytes());
						writer.write(("> a3 OK [READ-ONLY] Select completed."+'\n').getBytes());
					}*/
				}
				
				if ((next=reader.readLine()).contains("FETCH")&& next.startsWith("a4")) {
					//com.substring(com.indexOf("FETCH"));
					System.out.println();
					int i=1;
					//if (selected.equals("INBOX")) {
						writer.write((">Inbox"+'\n').getBytes());
						for (Email email : emails) {
							writer.write(("* " + (i + 1) + " FETCH (BODY[]"+'\n').getBytes());
							i++;
							writer.write((email.getID() + ":" + email.getTimeStamp()
									+ ":" + email.getFrom() + ":"+email.getTo()+":"
									+ email.getContent()+":"+email.getSubject()+'\n').getBytes());
						/*}
					} else {
						writer.write((">Sent Mails"+'\n').getBytes());
						for (Email email : sentEmails) {
							writer.write(("* " + (i + 1) + " FETCH (BODY[]"+'\n').getBytes());
							i++;
							writer.write((email.getID() + ":" + email.getTimeStamp()
									+ ":" + email.getTo() + ":"
									+ email.getContent()+'\n').getBytes());
						}*/
					}
				}
				writer.write(("a4 OK Fetching complete"+'\n').getBytes());
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
