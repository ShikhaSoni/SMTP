package test;

import java.io.*;

import java.net.*;
import java.security.KeyStore;
import javax.net.ssl.*;
import javax.xml.bind.DatatypeConverter;

public class Test {

private final static int SMTP_PORT = 25;
private final static String MAIL_SERVER = "smtp.gmail.com";
private final static String SENDER_EMAIL = "<fcnprojectsmtp@gmail.com>";
private final static String RECEIVER_EMAIL = "<omkarhegde2806@gmail.com>";
private final static String EMAIL_MESSAGE = "This is a test email agent!";

public static void main(String[] args) throws Exception {

Socket socket = null;

try
{

// Establish a TCP connection with the mail server.
socket = new Socket(MAIL_SERVER, SMTP_PORT);

// Create a BufferedReader to read a line at a time.
InputStream is = socket.getInputStream();
InputStreamReader isr = new InputStreamReader(is);
BufferedReader br = new BufferedReader(isr);

// Read greeting from the server.
String response = br.readLine();
System.out.println(response);
if (!response.startsWith("220")) {
throw new Exception("220 reply not received from server.");
}

// Get a reference to the socket's output stream.
OutputStream os = socket.getOutputStream();

// Send HELO command and get server response.
String command = "HELO " + "129.21.84.87"+"\r\n";
os.write(command.getBytes("US-ASCII"));
response = br.readLine();

System.out.println(response);

if (!response.startsWith("250")) {
throw new Exception("250 reply not received from server.");
}
// Send MAIL FROM command.
command = "STARTTLS" +"\r\n";
os.write(command.getBytes("US-ASCII"));
response = br.readLine();
System.out.println(response);

String keystoreFile = "C:\\Program Files\\Java\\jre7\\lib\\security\\cacerts";

SSLContext sslContext = SSLContext.getInstance("TLSv1");
String defaultAlgorithm = KeyManagerFactory.getDefaultAlgorithm();

KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(defaultAlgorithm);
String defaultKeyStoreType = KeyStore.getDefaultType();
KeyStore keyStore = KeyStore.getInstance(defaultKeyStoreType);

FileInputStream in = null;
File actualKeystoreFile = new File(keystoreFile);
try {
    in = new FileInputStream(actualKeystoreFile);
    keyStore.load(in, "changeit".toCharArray());
} catch (IOException e) {
	System.out.println("cannot open key file");
} finally {
    if (in != null) {
        in.close();
    }
}

keyManagerFactory.init(keyStore, "changeit".toCharArray());
KeyManager[] keyManagers = keyManagerFactory.getKeyManagers();

sslContext.init(keyManagers, null, null);



SSLSocketFactory ssf = sslContext.getSocketFactory();//(SSLSocketFactory) SSLSocketFactory.getDefault();
SSLSocket s = (SSLSocket) ssf.createSocket(socket, "smtp.gmail.com", 465, true);
String[] st = s.getEnabledProtocols();

s.setEnabledProtocols(st);

s.setNeedClientAuth(false);  

s.startHandshake();


System.out.println("Handshake COMPLETE!");

is = s.getInputStream();
isr = new InputStreamReader(is);
br = new BufferedReader(isr);
os = s.getOutputStream();


command = "HELO " + "129.21.84.87"+"\r\n";
os.write(command.getBytes("US-ASCII"));
response = br.readLine();
System.out.println(response);

// Send RCPT TO command.
command = "AUTH LOGIN"+"\r\n";
os.write(command.getBytes("US-ASCII"));
response = br.readLine();
System.out.println(response);

String message = "fcnprojectsmtp";
String encoded = DatatypeConverter.printBase64Binary(message.getBytes("UTF-8"));


// Send DATA command.
command = encoded+"\r\n";
os.write(command.getBytes("US-ASCII"));
response = br.readLine();
System.out.println("Username Response " + response);


message = "thisisapassword";
encoded = DatatypeConverter.printBase64Binary(message.getBytes("UTF-8"));
// Send message data.
command = encoded+"\r\n";
os.write(command.getBytes("US-ASCII"));
response = br.readLine();
System.out.println("Password Response " +response);

command = "MAIL FROM: "+SENDER_EMAIL+"\r\n";
os.write(command.getBytes("US-ASCII"));
response = br.readLine();
System.out.println("Mail from " + response);

// End with line with a single period.
command = "RCPT TO: "+RECEIVER_EMAIL+"\r\n";
os.write(command.getBytes("US-ASCII"));
response = br.readLine();
System.out.println("Receipt " + response);


// Send QUIT command.
command = "DATA"+"\r\n";
os.write(command.getBytes("US-ASCII"));
response = br.readLine();
System.out.println("Data command " + response);

command = EMAIL_MESSAGE+"\r\n";
os.write(command.getBytes("US-ASCII"));
//response = br.readLine();
//System.out.println("Message " + response);

command = "\r\n."+"\r\n";
os.write(command.getBytes("US-ASCII"));
response = br.readLine();


//os.write(command.getBytes("US-ASCII"));
//response = br.readLine();
System.out.println("DONE " + response);

}
finally
{
// close the socket
if( socket != null )
socket.close();
}
}
}