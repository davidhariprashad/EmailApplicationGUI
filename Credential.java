import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class Credential {
	
	/* Credential class handles the input from the user in the LogInViewer. */
	
	protected String username;
	protected String password;
	protected String domain;
	protected String messageLog;
	private boolean accountExists;
	private boolean correctCredentials;
	private boolean reasonableCredentials;
	BufferedReader br;
	BufferedWriter bw;
	
	public Credential(String input1, String input2, String domainChosen) {
		
		accountExists = false;
		correctCredentials = false;
		reasonableCredentials = false;
		messageLog = "";
		username = input1.toLowerCase();
		password = input2;
		domain = domainChosen;
		
		/* Confirm the username is reasonable. */
		checkUsername();
		/* Confirm the password is reasonable. */
		checkPassword();
		/* See if the account exists.
		 * If the account exists, see if the password exists. */
		checkAccount();
		
	}

	private void checkUsername() {
		
		/* Given the username from the user,
		 * check the different constraints. */
		
		if (username == null || username.isEmpty()) {
			logMessage("A username is required.");
			reasonableCredentials = false;
			return;
		}
		
		if (username.length()+domain.length() > 20) {
			logMessage("Maximum username length: 20");
			reasonableCredentials = false;
			return;
		}
		
		for (int i=0; i<username.length(); i++) {
			if (! Character.isLetterOrDigit(username.charAt(i))) {
				logMessage("The username is not alphanumeric.");
				reasonableCredentials = false;
				return;
			}
		}
		
		reasonableCredentials = true;
		
	}

	private void checkPassword() {
		
		/* Given the password from the user,
		 * check the different constraints. */
		
		if (password == null || password.isEmpty()) {
			logMessage("A password is required.");
			reasonableCredentials = false;
			return;
		}
		
		if (password.length() < 4) {
			logMessage("A password must be at least 4 characters.");
			reasonableCredentials = false;
			return;
		}
		
		if (password.length() > 12) {
			logMessage("A password must be at most 12 characters.");
			reasonableCredentials = false;
			return;
		}
		
		reasonableCredentials = true;
		
	}
	
	private void checkAccount() {
		
		try {
			br = new BufferedReader(new FileReader("Credentials.txt"));
			String temp;
			/* Read each line.
			 * Search for a username match.
			 * If the username matches, check if the following password matches.
			 * 
			 * If the username is never found,
			 * or the correct password does not match,
			 * change appropriate boolean fields. */
			while ((temp = br.readLine()) != null) {
				/* Correct username. */
				
				if ((username+domain).equals(temp)) {
					accountExists = true;
					/* Read corresponding password. */
					temp = br.readLine();
					if (password.equals(temp)) {
						correctCredentials = true;
						br.close();
						return;
					}
					else {
						logMessage("Wrong password.");
						br.close();
						return;
					}
				}
				/* Wrong username. */
				else {
					/* Skip the password. */
					br.readLine();
				}
			} /* Finished reading file lines. */
			/* Since all lines were read,
			 * the account does not exist at this point.
			 * 
			 * The remainder of this method will only be reachable after
			 * the while loop fails to find an email address match. */
			logMessage("The account does not exist.");
			br.close();
		}
		
		catch (IOException e) {
			System.out.println("Unable to check account.");
		}
		
	}
	
	protected void append() {
		
		try {
			/* Check if the file is empty. */
			boolean emptyFile = true;
			String line;
			br = new BufferedReader(new FileReader("Credentials.txt"));
			if ((line = br.readLine()) != null)
				emptyFile = false;
			br.close();
			/* Append the new credentials to the end of the file. */
			bw = new BufferedWriter(new FileWriter("Credentials.txt",true));
			if (!emptyFile)
				bw.newLine();
			bw.write(username+domain);
			bw.newLine();
			bw.write(password);
			bw.close();
		}
		
		catch (IOException e) {
			System.out.println("Unable to append new credentials.");
		}
		
	}
	
	public boolean isNew() {
		/* If the account does not exist,
		 * the email address is new. */
		return (! accountExists);
	}
	
	public boolean isValid() {
		/* Returns true where an email/password combination
		 * was found in the Credentials.txt file. */
		return correctCredentials;
	}
	
	public boolean isReasonable() {
		return reasonableCredentials;
	}
	
	private void logMessage(String log) {
		messageLog = messageLog + log + "\n";
	}
	
}