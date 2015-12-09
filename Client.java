
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
	public void setUsername(String username) {
		this.username = username;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	
	@Override
	public boolean equals(Object obj) {
		return this.username.equals(((Client)obj).username);		
	}
	
	@Override
	public int hashCode() {
		return username.hashCode();
	}
}
