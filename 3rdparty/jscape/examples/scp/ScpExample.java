import java.io.*;
import com.jscape.inet.scp.Scp;
import com.jscape.inet.scp.events.*;
import com.jscape.inet.ssh.util.SshParameters;

public class ScpExample implements ScpEventListener {
	
	public ScpExample(String hostname, String username, String password, String filename, String destination) throws Exception {
		// create new SshParamters instance
		SshParameters params = new SshParameters(hostname,username,password);
		
		// create new Scp instance
		Scp scp = new Scp(params);
		
		// register event listener
		scp.addListener(this);
		
		// establish connection
		scp.connect();	
		
		// upload file
		scp.upload(new File(filename),destination);
		
		// disconnect
		scp.disconnect();
	}
	
	public void download(ScpFileDownloadedEvent evt) {
		System.out.println("Downloaded file: " + evt.getFilename());	
	}
	
	public void upload(ScpFileUploadedEvent evt) {
		System.out.println("Uploaded file: " + evt.getFilename());
	}
	
	public void progress(ScpTransferProgressEvent evt) {
		System.out.println("Transfer progress: " + evt.getFilename() + " " + evt.getTransferredBytes() + " bytes.");
	}
	
	public void connected(ScpConnectedEvent evt) {
		System.out.println("Connected to host: " + evt.getHost());
	}
	
	public void disconnected(ScpDisconnectedEvent evt) {
		System.out.println("Disconnected from host: " + evt.getHost());
	}
	
	public static void main(String[] args) {
		try {
			// collect connection details and launch example
			BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
			System.out.print("Enter SCP hostname: ");
			String hostname = reader.readLine();
			System.out.print("Enter SCP username: ");
			String username = reader.readLine();
			System.out.print("Enter SCP password: ");
			String password = reader.readLine();
			System.out.print("Enter file to upload e.g. c:/tmp/test.txt : ");
			String filename = reader.readLine();
			System.out.print("Enter destination directory with ending path separator e.g. /home/user/ : ");
			String destination = reader.readLine();
			ScpExample example = new ScpExample(hostname,username,password, filename, destination);			
		} catch(Exception e) {
			e.printStackTrace();
		}		
	}
}
