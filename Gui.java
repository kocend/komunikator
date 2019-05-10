package chatClient;


import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;
import java.util.StringTokenizer;


public class Gui extends Application {
	
	private static String serverAddress;
	private Socket socket;
	private PrintWriter socketOut;
	private BufferedReader socketIn;
	private String userName;
	private Stage primaryStage;
	private ConversationScene conversationScene;
	

	public static void main(String[] args) {
		serverAddress = "localhost";
		launch();
	}
	
	
	public void start(Stage stage) {
		configureConnection();
		
		this.primaryStage = stage;
		
		try{primaryStage.getIcons().add(new Image(this.getClass().getResource("icon.png").toString()));}
		catch(Exception ex) {System.out.println("brak icon.png w plikach bin programu");}
		
		stage.addEventHandler(WindowEvent.WINDOW_CLOSE_REQUEST, (WindowEvent event)-> {
				try {
					socket.shutdownOutput();
					stop();
				}
				catch(Exception ex) { ex.printStackTrace();}
			});
		
		Scene loginScene = new LoginScene(this).getInstance();
		primaryStage.setScene(loginScene);
		primaryStage.show();
	}
	
	public void loggedIn() {
		primaryStage.hide();
		
		conversationScene = new ConversationScene(this);
		Scene convScene = conversationScene.getInstance();
		primaryStage.setScene(convScene);
		
		primaryStage.show();
		
		Thread receiverThread = new Thread(new Receiver());
		receiverThread.start();
	}
	
	
	public void sendText() {
		String message = conversationScene.getTypingField().getText().trim();
		if(!"".equals(message)) {
			socketOut.println(message);
			socketOut.flush();
			conversationScene.getTypingField().setText("");
		}
	}
	
	private void configureConnection() {
		try {
			socket = new Socket(serverAddress, 5437);
			socketOut = new PrintWriter(socket.getOutputStream());
			socketIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		}
		catch(IOException ex) {
			Alert alert = new Alert(AlertType.ERROR);
			alert.initOwner(primaryStage);
			alert.setHeaderText(null); 
			alert.setContentText("Nie uda³o siê po³¹czyæ z serwerem, spróbuj ponownie póŸniej."); 
			alert.showAndWait();
			System.exit(1);
		}
	}
	
	
	
	public boolean authentication(String loginData) {
		loginData = loginData.trim();
		socketOut.println(loginData); 
		socketOut.flush();

		String serverResponse = "false";
		try {
			serverResponse = socketIn.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		if(("true").equals(serverResponse.trim()))
			return true;
		return false;
	}


	public String getUserName() {
		return userName;
	}


	public void setUserName(String userName) {
		this.userName = userName;
	}
	
	
	public class Receiver implements Runnable{
		public void run() {
			String message;
			try {
				while(((message = socketIn.readLine())!=null)&&(!socket.isInputShutdown())) {
					
					StringTokenizer str = new StringTokenizer(message);
					String command = str.nextToken("/");
					String text = str.nextToken("\n");
					text=text.substring(1);
					
					switch(command) {
					case "message": conversationScene.getConversationArea().appendText(text+"\n");
						break;
					case "online": addToOnlineUsers(text);
						break;
					case "offline": removeFromOnlineUsers(text);
						break;
					}
				}
			}
			catch(IOException ex) {
				Alert alert = new Alert(AlertType.ERROR);
				alert.initOwner(primaryStage);
				alert.setHeaderText(null);
				alert.setContentText("po³¹czenie z serwerem zerwane, spróbuj uruchomiæ aplikacjê jeszcze raz.");
				alert.showAndWait();
				System.exit(1);
			}
		}
		
		private void addToOnlineUsers(String user) {
			conversationScene.getActiveUsers().add(user);
		}
		
		private void removeFromOnlineUsers(String user) {
			conversationScene.getActiveUsers().remove(user);
		}
	}
}
