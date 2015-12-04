
public class Constants {

	public final static int SMTP_PORT = 25;
	public final static int SMTP_SECURE_PORT = 465;
	
	public static final String KEYSTORE_PATH = "C:\\Program Files\\Java\\jre7\\lib\\security\\cacerts";
	public static final String KEYSTORE_PASSWORD = "changeit";
	
	public static final String SERVICE_READY = "220";
	public static final String OPERATION_COMPLETE = "250";
	public static final String SYNTAX_ERROR = "501";
	public static final String COMMAND_NOT_FOUND = "502";
	public static final String SERVICE_NOT_AVAILABLE = "550";
	public static final String TRANSACTION_FAILED = "554";
	
	public static final String HELO_COMMAND = "HELO";
	public static final String TLS_COMMAND = "STARTTLS";
	public static final String LOGIN_COMMAND = "AUTH LOGIN";
	public static final String MAIL_FROM_COMMAND = "MAIL FROM:";
	public static final String RCPT_TO_COMMAND = "RCPT TO:";
	public static final String DATA_COMMAND = "DATA";
	public static final String QUIT_COMMAND = "QUIT";
	public static final String MESSAGE_TERMINATION = "\r\n";
	public static final String SENDING_TERMINATION = "\r\n.";
	
	public static final String GMAIL_ADDRESS = "gmail.com";
	public static final String GMAIL_SMTP_HOST = "aspmx.l.google.com";
	
}
