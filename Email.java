import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Email {

	protected String[] toList;
	protected String to;
	protected String from;
	protected String subject;
	protected String message;
	protected String timeStamp;
	protected String path;
	protected boolean isValid;

	/*		Email File Format

	To:
	From:
	TimeStamp:
	Subject:
	Email Body:

	*/
	
	public Email(String p) {
		BufferedReader br;
		String line;
		toList = null;
		to = "";
		from = "";
		subject = "";
		message = "";
		timeStamp = "";
		path = p;
		try {
			br = new BufferedReader(new FileReader(path));
			line = br.readLine();
			toList = line.split(",");
			line = br.readLine();
			from = line;
			line = br.readLine();
			timeStamp = line;
			subject = br.readLine();
			while ((line = br.readLine()) != null) {
				message = message + line;
				message = message + "\n";
			}
			br.close();
		}
		catch (IOException e) {
			System.out.println("Issue reading from: "+path);
		}

		if (toList.length == 1) {
			to = toList[0];
		} else {
			for (int i = 0; i < toList.length - 1; i++) {
				to = to + toList[i] + ",";
			}
			to = to + toList[toList.length - 1];
		}

	}
	
	public void print() {
		System.out.print("To: ");
		if (toList.length == 1) {
			System.out.println(toList[0]);
		}
		else {
			for (int i=0; i<toList.length-1; i++) {
				System.out.print(toList[i]+", ");
			}
			System.out.println(toList[toList.length-1]);
		}
		System.out.println("From: "+from);
		System.out.println("Time: " + timeStamp);
		System.out.println(subject);
		System.out.println(message);
	}
}