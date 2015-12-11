
public class Client {

	private String username;
	private String password;
	
	public Client(String username, String password) {
		this.username = username;
		this.password = password;
	}
	
	public Client(String username) {
		this.username = username;
	}
	public String getUsername() {
		return username;
	}
	
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	
	@Override
	public boolean equals(Object obj) {
		String otherUser = ((Client)obj).username.trim();
		System.out.println(username + " "+otherUser);
		boolean val=  this.username.equals(otherUser);
		System.out.println(val);
		return val;
	}
	
	@Override
	public int hashCode() {
		return username.hashCode();
	}
}
