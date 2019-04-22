package GUI;


import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageBuilder;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;
import java.util.StringTokenizer;

import javax.swing.JOptionPane;


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
		
		primaryStage.setTitle("Komunikator");
		try {
		primaryStage.getIcons().add(new Image("GUI/icon.png"));
		}
		catch(Exception ex) {
			Alert alert = new Alert(AlertType.ERROR);
			alert.setContentText("coœ z icon.png");
			alert.showAndWait();
			System.exit(1);
		}
		
		
		stage.addEventHandler(WindowEvent.WINDOW_CLOSE_REQUEST, new EventHandler<WindowEvent>() {
			
			@Override
			public void handle(WindowEvent event) {
				try {
					socket.shutdownOutput();
				}
				catch(IOException ex) {
					  ex.printStackTrace(); 
				}
			}
		
		});
		
		Scene loginScene = new LoginScene(this).getInstance();
		primaryStage.setScene(loginScene);
		primaryStage.setResizable(false);
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
			ex.printStackTrace(); 
			Alert alert = new Alert(AlertType.ERROR);
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

		String serverResponse = null;
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
				ex.printStackTrace();
				Alert alert = new Alert(AlertType.ERROR);
				alert.setHeaderText(null);
				alert.setContentText("po³¹czenie z serwerem zerwane, spróbuj uruchomiæ aplikacjê jeszcze raz.");
				alert.showAndWait();
				System.exit(1);
			}
		}
	}
	
	
	
	private void addToOnlineUsers(String user) {
		Platform.runLater(new Runnable() {
			
			@Override
			public void run() {
				conversationScene.getOnlineUsers().getItems().add(user);
			}
		});
	}
	
	private void removeFromOnlineUsers(String user) {
		Platform.runLater(new Runnable() {
			
			@Override
			public void run() {
				conversationScene.getOnlineUsers().getItems().remove(user);
			}
		});
	}
	
	
}
