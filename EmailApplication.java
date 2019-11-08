import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

public class EmailApplication extends Application implements EventHandler<ActionEvent> {
	
	Stage EmailApplicationWindow;
	String user;
	String box;
	String emailPath;
	
	/********** LogInView **********/
	Scene LogInView;
	TextField username;
	TextField password;
	TextArea errorLog;
	Button logInButton;
	Button createButton;
	Credential credential;
	ChoiceBox<String> choiceBox;
	
	/********** EmailListView **********/
	Scene EmailListView;
	Button inboxButton;
	Button outboxButton;
	Button draftsButton;
	Button composeButton;
	Button logOutButton;
	ScrollPane scrollpane;
	
	/********** EmailView **********/
	Scene EmailView;
	TextField displayTo;
	TextField displayFrom;
	TextField displaySubject;
	TextArea displayMessage;
	Button replyButton;
	Button forwardButton;
	Button deleteButton;
	Button exitEmailViewButton;
	
	/********** EmailComposeView **********/
	Scene EmailComposeView;
	TextField inputTo;
	TextField inputSubject;
	TextArea inputMessage;
	Button sendButton;
	Button saveButton;
	Button discardButton;
	Button exitEmailComposeButton;
	
	public static void main(String[] args) throws Exception {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) {
		EmailApplicationWindow = primaryStage;
		EmailApplicationWindow.setTitle("Email Application: Log In");
		checkFiles();
		defineLogInView();
		EmailApplicationWindow.show();
		EmailApplicationWindow.setScene(LogInView);
	} //start
	
	@Override
	public void handle(ActionEvent event) {
		
		Object source = event.getSource();
		
		/********** LogInView **********/
		if (source == logInButton) {
			logIn();
		}
		else if (source == createButton) {
			createAccount();
		}
		
		/********** EmailListView **********/
		else if (source == inboxButton) {
			inbox();
		}
		else if (source == outboxButton) {
			outbox();
		}
		else if (source == draftsButton) {
			drafts();
		}
		else if (source == composeButton) {
			compose();
		}
		else if (source == logOutButton) {
			logOut();
		}
		else if (source instanceof EmailButton) {
			emailClick(source);
		}
		
		/********** EmailView **********/
		else if (source == replyButton) {
			reply();
		}
		else if (source == forwardButton) {
			forward();
		}
		else if (source == deleteButton) {
			delete();
		}
		else if (source == exitEmailViewButton) {
			exitEmail();
		}
		
		/********** EmailComposeView **********/
		else if (source == sendButton) {
			send();
		}
		else if (source == saveButton) {
			save();
		}
		else if (source == discardButton) {
			delete();
		}
		else if (source == exitEmailComposeButton) {
			exitCompose();
		}
		
	} //handle

	private void logIn() {
		credential = new Credential(username.getText(),password.getText(), choiceBox.getValue());
		if (credential.isValid()) {
			user = username.getText().toLowerCase() + choiceBox.getValue();
			checkDirectories(user);
			defineEmailListView(user);
			EmailApplicationWindow.setScene(EmailListView);
			box = "inbox";
			EmailApplicationWindow.setTitle(user+": Inbox");
		}
		else {
			errorLog.clear();
			errorLog.appendText(credential.messageLog);
		}
	} //logIn
	
	private void createAccount() {
		credential = new Credential(username.getText().toLowerCase(), password.getText(), choiceBox.getValue());
		errorLog.clear();
		if (credential.isNew() && credential.isReasonable()) {
			credential.append();
			errorLog.setText("Account created.");
		}
		else if (credential.isNew()) {
			/* Account information is new but not reasonable. */
			errorLog.setText(credential.messageLog);
		}
		else {
			/* Account information is not new. Inform the user. */
			errorLog.setText("Account already exists!");
		}
	} //createAccount
	
	private void inbox() {
		loadInbox(username.getText() + choiceBox.getValue());
	}
	
	private void outbox() {
		loadOutbox(username.getText() + choiceBox.getValue());
	}
	
	private void drafts() {
		loadDrafts(username.getText() + choiceBox.getValue());
	}
	
	private void compose() {
		emailPath = "";
		defineEmailComposeView();
		EmailApplicationWindow.setScene(EmailComposeView);
		EmailApplicationWindow.setTitle(user+": Compose");
	}
	
	private void logOut() {
		EmailApplicationWindow.setScene(LogInView);
		password.clear();
		errorLog.clear();
		box = null;
		emailPath = null;
		EmailApplicationWindow.setTitle("Email Application: Log In");
	} //logOut
	
	private void loadInbox(String username) {
		VBox vbox = new VBox();
		vbox.setAlignment(Pos.CENTER);
		EmailButton tempButton;
		List<Email> list = new ArrayList<>();
		File[] emailFiles = new File(".\\Data\\"+username+"\\Inbox").listFiles();
		/* Add emails to the list. */
		for (File emailFile : emailFiles) {
			list.add(new Email(emailFile.getPath()));
		}
		/* Sort the list. */
		list.sort(new Comparator<Email>() {
			public int compare(Email e1, Email e2) {
				String timestamp1, timestamp2;
				timestamp1 = e1.timeStamp.substring(6, 10)
						+e1.timeStamp.substring(3, 5)
						+e1.timeStamp.substring(0, 2)
						+e1.timeStamp.substring(11, 13)
						+e1.timeStamp.substring(14, 16)
						+e1.timeStamp.substring(17, 19);
				timestamp2 = e2.timeStamp.substring(6, 10)
						+e2.timeStamp.substring(3, 5)
						+e2.timeStamp.substring(0, 2)
						+e2.timeStamp.substring(11, 13)
						+e2.timeStamp.substring(14, 16)
						+e2.timeStamp.substring(17, 19);
				return -1*timestamp1.compareTo(timestamp2);
			}
		});
		/* For every Email in the list,
		 * generate an EmailButton and add it to the VBox.*/
		for (Email email : list) {
			String buttonText = EmailInboxButtonText(email.from, email.subject, email.timeStamp);
			tempButton = makeEmailButton(buttonText,email.path,"inbox",575,25);
			vbox.getChildren().add(tempButton);
		}
		
		scrollpane.setContent(vbox);
		box = "inbox";
		EmailApplicationWindow.setTitle(user+": Inbox");
	}
	
	private void loadOutbox(String username) {

		VBox vbox = new VBox();
		vbox.setAlignment(Pos.CENTER);
		EmailButton tempButton;
		List<Email> list = new ArrayList<>();
		File[] emailFiles = new File(".\\Data\\"+username+"\\Outbox").listFiles();
		/* Add emails to the list. */
		for (File emailFile : emailFiles) {
			list.add(new Email(emailFile.getPath()));
		}
		/* Sort the list. */
		list.sort(new Comparator<Email>() {
			public int compare(Email e1, Email e2) {
				String timestamp1, timestamp2;
				timestamp1 = e1.timeStamp.substring(6, 10)
						+e1.timeStamp.substring(3, 5)
						+e1.timeStamp.substring(0, 2)
						+e1.timeStamp.substring(11, 13)
						+e1.timeStamp.substring(14, 16)
						+e1.timeStamp.substring(17, 19);
				timestamp2 = e2.timeStamp.substring(6, 10)
						+e2.timeStamp.substring(3, 5)
						+e2.timeStamp.substring(0, 2)
						+e2.timeStamp.substring(11, 13)
						+e2.timeStamp.substring(14, 16)
						+e2.timeStamp.substring(17, 19);
				return -1*timestamp1.compareTo(timestamp2);
			}
		});
		/* For every Email in the list,
		 * generate an EmailButton and add it to the VBox.*/
		for (Email email : list) {
			String buttonText = EmailOutBoxButtonText(email.to, email.subject, email.timeStamp);
			tempButton = makeEmailButton(buttonText,email.path,"outbox",575,25);
			vbox.getChildren().add(tempButton);
		}
		
		scrollpane.setContent(vbox);
		box = "outbox";
		EmailApplicationWindow.setTitle(user+": Outbox");
	}
	
	private void loadDrafts(String username) {
		VBox vbox = new VBox();
		vbox.setAlignment(Pos.CENTER);
		EmailButton tempButton;
		List<Email> list = new ArrayList<>();
		File[] emailFiles = new File(".\\Data\\"+username+"\\Drafts").listFiles();
		/* Add emails to the list. */
		for (File emailFile : emailFiles) {
			list.add(new Email(emailFile.getPath()));
		}
		/* Sort the list. */
		list.sort(new Comparator<Email>() {
			public int compare(Email e1, Email e2) {
				String timestamp1, timestamp2;
				timestamp1 = e1.timeStamp.substring(6, 10)
						+e1.timeStamp.substring(3, 5)
						+e1.timeStamp.substring(0, 2)
						+e1.timeStamp.substring(11, 13)
						+e1.timeStamp.substring(14, 16)
						+e1.timeStamp.substring(17, 19);
				timestamp2 = e2.timeStamp.substring(6, 10)
						+e2.timeStamp.substring(3, 5)
						+e2.timeStamp.substring(0, 2)
						+e2.timeStamp.substring(11, 13)
						+e2.timeStamp.substring(14, 16)
						+e2.timeStamp.substring(17, 19);
				return -1*timestamp1.compareTo(timestamp2);
			}
		});
		/* For every Email in the list,
		 * generate an EmailButton and add it to the VBox.*/
		for (Email email : list) {
			String buttonText = EmailOutBoxButtonText(email.to, email.subject, email.timeStamp);
			tempButton = makeEmailButton(buttonText,email.path,"drafts",575,25);
			vbox.getChildren().add(tempButton);
		}
		
		scrollpane.setContent(vbox);
		box = "drafts";
		EmailApplicationWindow.setTitle(user+": Drafts");
		
	}
	
	private void emailClick(Object source) {
		EmailButton emailbutton = ((EmailButton)source);
		Email email = new Email(emailbutton.path);
		box = emailbutton.type.toLowerCase();
		emailPath = emailbutton.path;
		if (emailbutton.type.equalsIgnoreCase("inbox") || emailbutton.type.equalsIgnoreCase("outbox")) {
			defineEmailView(email);
			EmailApplicationWindow.setScene(EmailView);
			if (emailbutton.type.equalsIgnoreCase("inbox"))
				EmailApplicationWindow.setTitle(user+": Viewing email from "+email.from);
			else if (emailbutton.type.equalsIgnoreCase("outbox"))
				EmailApplicationWindow.setTitle(user+": Viewing email to "+email.to);
		}
		else if (emailbutton.type.equalsIgnoreCase("drafts")) {
			defineEmailComposeView();
			EmailApplicationWindow.setTitle(user+": Compose");
			inputTo.setText(email.to);
			inputSubject.setText(email.subject);
			inputMessage.setText(email.message);
			EmailApplicationWindow.setScene(EmailComposeView);
		}
	}
	
	private void reply() {
		defineEmailComposeView();
		/* Remove "To: " */
		inputTo.setText(displayFrom.getText().substring(6));
		/* Remove "Subject: " */
		inputSubject.setText("Re: "+displaySubject.getText().substring(9));
		/* Copy message exactly */
		inputMessage.setText("\n\n---\n"
				+"From: "+displayFrom.getText().substring(6)+"\n"
				+"To: "+displayTo.getText().substring(4)+"\n"+
				"Subject: "+displaySubject.getText().substring(9)+"\n"+
				displayMessage.getText());
		/* Change the view since the user is done with the email. */
		EmailApplicationWindow.setScene(EmailComposeView);
		emailPath = "";
		EmailApplicationWindow.setTitle(user+": Compose");
	}
	
	private void forward() {
		defineEmailComposeView();
		inputTo.clear();
		/* Append "Fwd: " to the beginning of the subject line. */
		inputSubject.setText("Fwd: "+displaySubject.getText().substring(9));
		/* Append some space for the user to create their forwarded message. */
		inputMessage.setText("\n\n---\n"
				+"From: "+displayFrom.getText().substring(6)+"\n"
				+"To: "+displayTo.getText().substring(4)+"\n"+
				"Subject: "+displaySubject.getText().substring(9)+"\n"
				+displayMessage.getText());
		EmailApplicationWindow.setScene(EmailComposeView);
		emailPath = "";
		EmailApplicationWindow.setTitle(user+": Compose");
	}
	
	private void delete() {
		/* If the emailPath is not empty,
		 * delete the file associated with it.
		 * Otherwise, the deletion is not linked to a file.
		 * No file should not deleted when:
		 * composing a new mail,
		 * replying from an old mail,
		 * and forwarding an old mail. */
		EmailComposeView = null;
		File file = new File(emailPath);
		if (file.exists()) {
			file.delete();
			
		}
		/* Return the user to the box they came from. */
		if (box == "inbox") {
			loadInbox(user);
		}
		else if (box == "outbox") {
			loadOutbox(user);
		}
		else if (box == "drafts") {
			loadDrafts(user);
		}
		emailPath = "";
		EmailApplicationWindow.setScene(EmailListView);
	}
	
	private void exitEmail() {
		emailPath = "";
		EmailView = null;
		EmailApplicationWindow.setScene(EmailListView);
	}
	
	private void send() {
		
		String SendNotSuccess = "";

		try {
			if (inputTo.getText().isEmpty()) {
				
				UserFeedbackBox.display("Email Not Sent", "Please Enter a Valid Email Address");
				return;
			}
			/* Check for email send list for invalid emails.
			 * For each valid email:
			 * create a file
			 * write to the proper directory. */
			String[] sendList = inputTo.getText().split(",");
			for (int i=0; i<sendList.length; i++) {
				sendList[i] = sendList[i].trim().toLowerCase();
				if (isValidEmailAddress(sendList[i])) {

					checkDirectories(sendList[i]);
					final long time = System.currentTimeMillis();
					BufferedWriter bw = new BufferedWriter(new FileWriter(".\\Data\\"+sendList[i]+"\\Inbox\\"+time+".txt",false));
					bw.write(sendList[i]);
					bw.newLine();
					bw.write(user);
					bw.newLine();
					bw.write(new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date()));
					bw.newLine();
					bw.write(inputSubject.getText());
					bw.newLine();
					bw.write(inputMessage.getText());
					bw.close();

					bw = new BufferedWriter(new FileWriter(".\\Data\\"+user+"\\Outbox\\"+time+".txt"));
					bw.write(sendList[i]);
					bw.newLine();
					bw.write(user);
					bw.newLine();
					bw.write(new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date()));
					bw.newLine();
					bw.write(inputSubject.getText());
					bw.newLine();
					bw.write(inputMessage.getText());
					bw.close();

					if (box == "drafts") {
						delete();
					}

				}
				else {
					
					SendNotSuccess += sendList[i] + "\n";
					
				}
			}

			if(SendNotSuccess.length() > 0) {
				UserFeedbackBox.display("Email Not Sent", "Email Not Present in DataBase: \n\n" + SendNotSuccess);
				save();
			}

			EmailApplicationWindow.setScene(EmailListView);
			EmailApplicationWindow.setTitle(user+": "+Character.toUpperCase(box.charAt(0))+box.substring(1));
			EmailComposeView = null;
		}
		catch (IOException e) {
			System.out.println("Something went wrong sending an email.");
			UserFeedbackBox.display("Email Not Sent", "There is a problem with saving the data");
		}
	}
	
	private void save() {
		BufferedWriter bw;
		try {
			bw = new BufferedWriter(new FileWriter(".\\Data\\"+user+"\\Drafts\\"+System.nanoTime()+".txt",false));
			bw.write(inputTo.getText());
			bw.newLine();
			bw.write(user);
			bw.newLine();
			bw.write(new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date()));
			bw.newLine();
			bw.write(inputSubject.getText());
			bw.newLine();
			bw.write(inputMessage.getText());
			bw.close();
			loadDrafts(user);
			EmailApplicationWindow.setScene(EmailListView);
			EmailComposeView = null;
			if (box == "drafts") {
				/* Delete the old draft.
				 * Do not keep saving new drafts of a simply edited draft. */
				delete();
			}
		}
		catch (IOException e) {
			UserFeedbackBox.display("Email Save", "Unable to save the Email");
			System.out.println("Unable to save file.");
		}
	}

	private void exitCompose() {
		save();
		EmailComposeView = null;
		EmailApplicationWindow.setScene(EmailListView);
	}
	
	private boolean isValidEmailAddress(String emailAddress) {
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader("Credentials.txt"));
			String line;
			while ((line = br.readLine()) != null) {
				if (line.toLowerCase().equalsIgnoreCase(emailAddress)) {
					br.close();
					return true;
				}
				/* Skip password. */
				line = br.readLine();
			}
			br.close();
			return false;
		}
		catch (IOException e) {
			System.out.println("Unable to read from file: Credentials.txt");
			return false;
		}
	}
	
	private String EmailInboxButtonText(String from, String subject, String timeStamp){
		String fromPart = new String();
		String subjectPart = "";
		String finalString = "";
		String tab = "\t";

		if(from.length() > 20){
			fromPart = from.substring(0, 19);
		} else if(from.length() <= 20){
			int leftSpaces = 20 - from.length();
			fromPart = from;
			for(int i = 0; i < leftSpaces; i++){
				fromPart += new String(" ");
			}
		}

		if(subject.length() > 60){
			subjectPart = subject.substring(0, 59);
		} else if(subject.length() <= 60){
			subjectPart = subject;
			int leftSpaces = 60 - subject.length();
			for(int i = 0; i < leftSpaces; i++){
				subjectPart += new String(" ");
			}
		}

		finalString = "From: " + fromPart + tab + "Subject: " + subjectPart + tab + "Time: " + timeStamp;

		return finalString;
	}

	private String EmailOutBoxButtonText(String to, String subject, String timeStamp){

		String toPart = new String();
		String subjectPart = "";
		String finalString = "";
		String tab = "\t";

		if(to.length() > 20){
			toPart = to.substring(0, 19);
		} else if(to.length() <= 20){
			int leftSpaces = 20 - to.length();
			toPart = to;
			toPart = to;
			for(int i = 0; i < leftSpaces; i++){
				toPart += new String(" ");
			}
		}

		if(subject != null) {
			if (subject.length() > 60) {
				subjectPart = subject.substring(0, 59);
			} else if (subject.length() <= 60) {
				subjectPart = subject;
				int leftSpaces = 60 - subject.length();
				for (int i = 0; i < leftSpaces; i++) {
					subjectPart += new String(" ");
				}
			}
		}

		finalString = "To: " + toPart + tab + "Subject: " + subjectPart + tab + "Time: " + timeStamp;
		return  finalString;
	}
	
	private void checkDirectories(String username) {
		File dir;
		dir = new File(".\\Data");
		// check if the data folder exists
		if (!dir.exists()) {
			dir.mkdir();
		}
		dir = new File(".\\Data\\"+username);
		//check if the user's folder exists
		if (!dir.exists()) {
			dir.mkdir();
		}
		//check for the boxes
		dir = new File(".\\Data\\"+username+"\\Inbox");
		if (!dir.exists()) {
			dir.mkdir();
		}
		dir = new File(".\\Data\\"+username+"\\Outbox");
		if (!dir.exists()) {
			dir.mkdir();
		}
		dir = new File(".\\Data\\"+username+"\\Drafts");
		if (!dir.exists()) {
			dir.mkdir();
		}
	} //checkDirectories
	
	private void checkFiles() {
		
		/* ADDED TO WORK WITH JAR FILE
		 * TEMPORARY FIX */
		
		BufferedWriter bw;
		try {
			bw = new BufferedWriter(new FileWriter("Domains.txt"));
			bw.write("yg.com"); bw.newLine();
			bw.write("cq.edu"); bw.newLine();
			bw.write("lnb.gov");
			bw.close();
		}
		catch (IOException e){
			System.out.println("Error writing to Domains.txt");
		}
		
		try {
			File file;
			file = new File(".\\Credentials.txt");
			if (!file.exists()) {
				file.createNewFile();
			}
			file = new File(".\\Domains.txt");
			if (!file.exists()) {
				file.createNewFile();
			}
		}
		catch (IOException e) {
			System.out.println("Problem checking for files.");
		}
		
	}
	
	private void defineLogInView() {
		
		//set properties for layout
		GridPane layout = new GridPane();
		layout.setAlignment(Pos.CENTER);
		layout.setPadding(new Insets(50.0, 50.0, 50.0, 50.0));
		layout.setHgap(10.0);
		layout.setVgap(10.0);
		
		//define username text field
		username = new TextField();
		username.setPromptText("Enter username.");
		username.setPrefColumnCount(25);
		layout.add(username, 0, 0);
		
		//define password field
		password = new PasswordField();
		password.setPromptText("Enter password.");
		password.setPrefColumnCount(25);
		layout.add(password, 0, 1);
		
		//define a login button
		logInButton = new Button("Log in");
		logInButton.setOnAction(this);
		layout.add(logInButton, 1, 1);
		
		//define a new account button
		createButton = new Button("Create");
		createButton.setOnAction(this);
		layout.add(createButton, 2, 1);

		//define dropDownMenu
		choiceBox = new ChoiceBox<>();
		//add any Domain here! ya it's that easy.
		choiceBox.getItems().add("@cq.edu");
		choiceBox.getItems().add("@yg.com");
		choiceBox.getItems().add("@lnb.gov");
		//Default Value.
		choiceBox.setValue("@cq.edu");
		layout.add(choiceBox, 1 ,0);
		//to get the value stored in the choiceBox we need {choiceBox.getValue();}
		
		//define a message area
		errorLog = new TextArea();
		errorLog.setPrefColumnCount(50);
		errorLog.setMaxSize(300,100);
		errorLog.setPromptText("No message.");
		errorLog.setVisible(true);
		layout.add(errorLog, 0, 2);
		
		LogInView = new Scene(layout, 600,300);
		
	} //defineLogInViewer
	
	private void defineEmailListView(String username) {
		
		GridPane layout = new GridPane();
		Label currentUser = new Label("Current User: " + username);
		
		layout.setPadding(new Insets(50.0, 50.0, 50.0, 50.0));
		layout.setHgap(25.0);
		layout.setVgap(10.0);
		layout.setAlignment(Pos.TOP_CENTER);
		
		inboxButton = makeButton("Inbox",100,25);
		outboxButton = makeButton("Outbox",100,25);
		draftsButton = makeButton("Drafts",100,25);
		composeButton = makeButton("Compose",100,25);
		logOutButton = makeButton("Log out",100,25);

		layout.add(inboxButton, 0, 0);
		layout.add(outboxButton, 1, 0);
		layout.add(draftsButton, 2, 0);
		layout.add(composeButton, 3, 0);
		layout.add(logOutButton, 4, 0);
		
		scrollpane = new ScrollPane();
		loadInbox(username);
		scrollpane.setVbarPolicy(ScrollBarPolicy.ALWAYS);
		scrollpane.setPadding(new Insets(5,5,5,5));
		GridPane.setColumnSpan(scrollpane, 5);
		GridPane.setRowSpan(scrollpane, 25);
		layout.add(scrollpane, 0, 1);
		
		EmailListView = new Scene(layout);
		
	} //defineEmailListViewer
	
	private void defineEmailView(Email email) {
		
		GridPane layout = new GridPane();
		layout.setAlignment(Pos.CENTER);
		layout.setPadding(new Insets(50.0, 50.0, 50.0, 50.0));
		layout.setHgap(25.0);
		layout.setVgap(10.0);
		
		displayTo = new TextField();
		displayFrom = new TextField();
		displaySubject = new TextField();
		displayMessage = new TextArea();
		
		replyButton = makeButton("Reply", 100, 25);
		forwardButton = makeButton("Forward", 100, 25);
		deleteButton = makeButton("Delete", 100, 25);
		exitEmailViewButton = makeButton("Back", 100, 25);
		
		displayTo.setEditable(false);
		displayFrom.setEditable(false);
		displaySubject.setEditable(false);
		displayMessage.setEditable(false);
		
		displayTo.setPrefSize(430, 25);
		displayFrom.setPrefSize(430, 25);
		displaySubject.setPrefSize(430, 25);
		displayMessage.setPrefSize(430, 215);
		
		displayTo.setText("To: "+email.to);
		displayFrom.setText("From: "+email.from);
		displaySubject.setText("Subject: "+email.subject);
		displayMessage.setText(email.message);
		
		displayTo.setVisible(true);
		displayFrom.setVisible(true);
		displaySubject.setVisible(true);
		displayMessage.setVisible(true);
		
		GridPane.setColumnSpan(displayTo, 4);
		GridPane.setColumnSpan(displayFrom, 4);
		GridPane.setColumnSpan(displaySubject, 4);
		GridPane.setColumnSpan(displayMessage, 4);
		GridPane.setRowSpan(displayMessage, 10);
		
		if (box == "inbox")
			layout.add(displayFrom, 0, 2);
		else if (box == "outbox")
			layout.add(displayTo, 0, 2);
		layout.add(displaySubject, 0, 3);
		layout.add(displayMessage, 0, 4);
		
		layout.add(replyButton, 0, 0);
		layout.add(forwardButton, 1, 0);
		layout.add(deleteButton, 2, 0);
		layout.add(exitEmailViewButton, 3, 0);
		EmailView = new Scene(layout);
		
	} //defineEmailViewer
	
	private void defineEmailComposeView() {
		
		GridPane layout = new GridPane();
		layout.setAlignment(Pos.CENTER);
		layout.setPadding(new Insets(50.0, 50.0, 50.0, 50.0));
		layout.setHgap(25.0);
		layout.setVgap(10.0);
		
		inputTo = new TextField();
		inputSubject = new TextField();
		inputMessage = new TextArea();
		
		sendButton = makeButton("Send", 100, 25);
		saveButton = makeButton("Save", 100, 25);
		discardButton = makeButton("Discard", 100, 25);
		exitEmailComposeButton = makeButton("Exit", 100, 25);
		
		inputTo.setPromptText("To:");
		inputSubject.setPromptText("Subject:");
		inputMessage.setPromptText("Message:");
		
		inputTo.setPrefSize(440, 25);
		inputSubject.setPrefSize(440, 25);
		inputMessage.setPrefSize(440, 220);
		
		GridPane.setColumnSpan(inputTo, 4);
		GridPane.setColumnSpan(inputSubject, 4);
		GridPane.setColumnSpan(inputMessage, 4);
		GridPane.setRowSpan(inputMessage, 10);
		
		layout.add(sendButton, 0, 0);
		layout.add(saveButton, 1, 0);
		layout.add(discardButton, 2, 0);
		layout.add(exitEmailComposeButton, 3, 0);
		
		layout.add(inputTo, 0, 1);
		layout.add(inputSubject, 0, 2);
		layout.add(inputMessage, 0, 3);
		
		EmailComposeView = new Scene(layout);
		
	}
	
	private Button makeButton(String s, int x, int y) {
		Button button = new Button(s);
		button.setTextAlignment(TextAlignment.CENTER);
		button.setPrefSize(x, y);
		button.setOnAction(this);
		return button;
	}
	
	private EmailButton makeEmailButton(String text, String path, String type, int x, int y) {
		EmailButton button = new EmailButton(text,path,type,x,y);
		button.setOnAction(this);
		return button;
	}
	
}