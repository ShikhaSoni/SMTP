import java.io.IOException;
import java.io.PrintWriter;
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

	public void run() {
		while (true) {
			try {
				clientSocket = IMAP_Socket.accept();
				new ClientRequest(clientSocket).start();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	class ClientRequest extends Thread {
		Socket clientSocket;
		private Scanner in;
		private PrintWriter out;
		private ArrayList<Email> emails = null;
		private ArrayList<Email> InboxEmails = null;
		private ArrayList<Email> sentEmails = null;

		public ClientRequest(Socket clientSocket) {
			this.clientSocket = clientSocket;
			try {
				in = new Scanner(clientSocket.getInputStream());
				out = new PrintWriter(clientSocket.getOutputStream(), true);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		public void run() {
			System.out.println(">" + Thread.currentThread().toString()
					+ clientSocket.getPort() + " "
					+ clientSocket.getLocalPort());
			System.out.println("* OK ready for requests");
			String auth = in.nextLine();
			String loginString = "a1 login";
			String listCom = "a2 LIST \"\" \"*\"";
			String examineCom = "EXAMINE";
			String user;
			String pass;
			String[] userpass = null;
			if (!auth.contains(loginString)) {
				out.println("Wrong command");
				return;
			}
			userpass = auth.substring(loginString.length()).split(" ");
			user = userpass[0];
			pass = userpass[1];
			boolean exists = false;
			for (Client c : SMTPServer.emailStorage.keySet()) {
				if (c.getUsername().equals(user)) {
					if (c.getPassword().equals(pass)) {
						out.println("> a1 OK " + user + " Logged in SUCCESS ");
						emails = SMTPServer.emailStorage.get(c);
					}
					exists = true;
					break;
				}
			}
			if (!exists) {
				out.println(">No user found");
				return;
			}
			for (Email email : emails) {
				if (email.getFrom().equals(user))
					sentEmails.add(email);
				else
					InboxEmails.add(email);
			}
			if (in.nextLine().equalsIgnoreCase(listCom))
				out.println(">* LIST \".\" \" INBOX\" \"SENT\" ");

			String next = in.nextLine();
			String selected = null;
			if (next.contains(examineCom) && next.startsWith("a3")) {
				if (next.contains("INBOX")) {
					out.println(">* OK Read-only mailboxe");
					selected = "INBOX";
					out.println(">* " + InboxEmails.size() + " EXITS");
					out.println("> a3 OK [READ-ONLY] Select completed.");
				} else if (next.contains("SENT")) {
					out.println(">* OK Read-only mailboxe");
					selected = "SENT";
					out.println(">* " + sentEmails.size() + " EXITS");
					out.println("> a3 OK [READ-ONLY] Select completed.");
				}
			}
			String com=null;
			if ((com=in.nextLine()).contains("FETCH")&& com.startsWith("a4")) {
				//com.substring(com.indexOf("FETCH"));
				if (selected.equals("INBOX")) {
					out.println(">Inbox");
					for (Email email : InboxEmails) {
						out.println(email.getID() + ":" + email.getTimeStamp()
								+ ":" + email.getFrom() + ":"
								+ email.getContent());
					}
				} else {
					out.println(">Sent Mails");
					for (Email email : sentEmails) {
						out.println(email.getID() + ":" + email.getTimeStamp()
								+ ":" + email.getTo() + ":"
								+ email.getContent());
					}
				}
			}
			out.println("a4 OK Fetching complete");
		}
	}
}
