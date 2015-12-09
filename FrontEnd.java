import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

class FrontEnd {
	private Email[] inbox = { new Email("Shikha", "Soniiiiiiii", "Helllo") };
	private Email[] Sent = { new Email("ketki", "Shikha", "hey") };
	
	JFrame mainFrame = new JFrame();
	JFrame composeFrame = new JFrame("E-mail Sender");
	
	JPanel mainPanel = new JPanel();
	
	JButton openButton = new JButton("Open Mail");
	JButton composeButton = new JButton("Compose new");
	private JButton buttonSend = new JButton("SEND");
	
	private JList<Email> Inbox = new JList<Email>(inbox);
	private JList<Email> sent = new JList<Email>(Sent);
	
	private JLabel labelTo = new JLabel("To: ");
	private JLabel labelSubject = new JLabel("Subject: ");

	private JTextField fieldTo = new JTextField(30);
	private JTextField fieldSubject = new JTextField(30);

	private JTextArea textAreaMessage = new JTextArea(10, 30);

	String To, Subject, Content;
	
	String username, password;

	private GridBagConstraints constraints = new GridBagConstraints();
	JFrame frame;

	public FrontEnd() {
		frame = new JFrame("Demo application");
		frame.setSize(300, 150);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JPanel panel = new JPanel();
		frame.add(panel);
		placeComponents(panel);
		frame.setVisible(true);

	}

	private void placeComponents(JPanel panel) {

		panel.setLayout(null);

		JLabel userLabel = new JLabel("User");
		userLabel.setBounds(10, 10, 80, 25);
		panel.add(userLabel);

		JTextField userText = new JTextField(20);
		userText.setBounds(100, 10, 160, 25);
		panel.add(userText);

		JLabel passwordLabel = new JLabel("Password");
		passwordLabel.setBounds(10, 40, 80, 25);
		panel.add(passwordLabel);

		JPasswordField passwordText = new JPasswordField(20);
		passwordText.setBounds(100, 40, 160, 25);
		panel.add(passwordText);

		JButton loginButton = new JButton("login");
		loginButton.setBounds(10, 80, 80, 25);
		panel.add(loginButton);
		loginButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// aunthenticate the user and return true
				username=userText.getText();
				password=passwordText.getText();
				frame.setVisible(false);
				connectIMAP();
				mainWindow();
			}
		});

		JButton registerButton = new JButton("register");
		registerButton.setBounds(180, 80, 80, 25);
		panel.add(registerButton);
	}

	public static void main(String[] args) {
		new FrontEnd();
	}
	
	private void connectIMAP() {
		
		Socket IMAPsocket;
		BufferedReader reader = null;
		DataOutputStream writer= null;
		System.out.println("Connecting to IMAP");
		//follow the protocol
		try {
			IMAPsocket= new Socket(Constants.IMAP_IPADDRESS, Constants.IMAP_PORT);
			
			reader= new BufferedReader(new InputStreamReader(IMAPsocket.getInputStream()));
			writer= new DataOutputStream(IMAPsocket.getOutputStream());
			
			System.out.println("Streams made");
			writer.writeBytes("Sending req"+ '\n');
			String command=reader.readLine();
			System.out.println("Server: "+command);
			if(command.contains("OK")){
				writer.writeBytes("a1 LOGIN "+ username+" "+ password+ '\n');
				System.out.println("OK found"+ username+":"+password);
			}
			else if(command.equals("Wrong command")){
				System.out.println("Wrong command");
				System.exit(0);
			}
			else if(command.contains("No user found")){
				//Do something here
			}
			command=reader.readLine();
			if(command.contains("OK") && command.contains("SUCCESS")){
				writer.writeBytes("a3 EXAMINE INBOX"+ '\n');
			}
			command=reader.readLine();
			if(command.contains("OK")){
				System.out.println("Successfully examined");
				writer.writeBytes("a4 FETCH BODY");
			}
			while(true){
				System.out.println(reader.readLine());
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void mainWindow() {
		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mainFrame.setSize(200, 200);
		mainFrame.add(mainPanel);
		mainPanel.setLayout(new GridLayout(2, 2));
		composeButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					UIManager.setLookAndFeel(UIManager
							.getSystemLookAndFeelClassName());
				} catch (Exception ex) {
					ex.printStackTrace();
				}

				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						composeMail();
						mainFrame.setVisible(false);
					}
				});
			}
		});
		openButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				mainFrame.setVisible(false);
				Email email = Inbox.getSelectedValue();
				Email email1 = sent.getSelectedValue();
				Email openmail;
				if (email != null)
					openmail = email;
				else
					openmail = email1;
				JFrame frame = new JFrame(openmail.getSubject());
				frame.setSize(400, 400);
				JPanel panel = new JPanel();
				frame.add(panel);
				JLabel label = new JLabel(openmail.getContent());
				JButton back = new JButton("Back");
				panel.add(back);
				panel.add(label);
				frame.setVisible(true);
				back.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent arg0) {
						frame.setVisible(false);
						mainFrame.setVisible(true);
					}
				});
			}
		});

		Inbox.setVisibleRowCount(4);
		sent.setVisibleRowCount(4);
		JScrollPane pane = new JScrollPane(Inbox);
		JScrollPane pane1 = new JScrollPane(sent);
		mainPanel.add(openButton);
		mainPanel.add(pane/* , BorderLayout.NORTH */);
		mainPanel.add(composeButton/* , BorderLayout.SOUTH */);
		mainPanel.add(pane1);
		mainFrame.setVisible(true);
	}

	public void composeMail() {
		composeFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		composeFrame.setLayout(new GridBagLayout());
		constraints.anchor = GridBagConstraints.WEST;
		constraints.insets = new Insets(5, 5, 5, 5);

		setupForm();
		composeFrame.setVisible(true);
		composeFrame.pack();
		composeFrame.setLocationRelativeTo(null); // center on screen
		composeFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	private void setupForm() {
		constraints.gridx = 0;
		constraints.gridy = 0;
		composeFrame.add(labelTo, constraints);

		constraints.gridx = 1;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		composeFrame.add(fieldTo, constraints);

		constraints.gridx = 0;
		constraints.gridy = 1;
		composeFrame.add(labelSubject, constraints);

		constraints.gridx = 1;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		composeFrame.add(fieldSubject, constraints);

		constraints.gridx = 2;
		constraints.gridy = 0;
		constraints.gridheight = 2;
		constraints.fill = GridBagConstraints.BOTH;
		buttonSend.setFont(new Font("Arial", Font.BOLD, 16));
		composeFrame.add(buttonSend, constraints);

		buttonSend.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				// add event
				// if(!validateFields()){
				To = fieldTo.getText();
				Subject = fieldSubject.getText();
				Content = textAreaMessage.getText();
				composeFrame.setVisible(false);
				mainFrame.setVisible(true);
				// }

			}
		});

		constraints.gridx = 0;
		constraints.gridy = 2;
		constraints.gridheight = 1;
		constraints.gridwidth = 3;
		constraints.gridy = 3;
		constraints.weightx = 1.0;
		constraints.weighty = 1.0;

		composeFrame.add(new JScrollPane(textAreaMessage), constraints);
	}
}