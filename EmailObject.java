
public class EmailObject {

	public String senderEmail;
	public String toEmail;
	public String subject;
	public String dateAndTime;
	public String messageBody;
	public String messageID;
	
	
	public EmailObject(String senderEmail, String toEmail, String subject, String dateAndTime, String messageBody, String messageID) {
		this.senderEmail = senderEmail;
		this.toEmail = toEmail;
		this.subject = subject;
		this.dateAndTime = dateAndTime;
		this.messageBody = messageBody;
		this.messageID = messageID;
	}
}
