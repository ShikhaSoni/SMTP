
public class Email {
	private int ID;
	private String To;
	private String From;
	private String content;
	private String timeStamp;
	private String subject;
	public Email(String To, String subject, String content){
		this.content=content;
		this.To=To;
		this.subject=subject;
	}
	public int getID() {
		return ID;
	}
	public void setID(int iD) {
		ID = iD;
	}
	public String getTo() {
		return To;
	}
	public void setTo(String to) {
		To = to;
	}
	public String getFrom() {
		return From;
	}
	public void setFrom(String from) {
		From = from;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public String getTimeStamp() {
		return timeStamp;
	}
	public void setTimeStamp(String timeStamp) {
		this.timeStamp = timeStamp;
	}
	public String getSubject() {
		return subject;
	}
	public void setSubject(String subject) {
		this.subject = subject;
	}
	@Override
	public String toString(){
		return To+":"+subject+":"+content;
	}

}
