package GUI;

import java.util.LinkedList;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;

public class LoginScene {
	
	private Gui gui;
	private Scene loginScene;

	public LoginScene(Gui obj) {
		gui=obj;
		Text loginLabel = new Text("Login");
	    Text passwordLabel = new Text("Password"); 
	       
	    TextField loginField = new TextField();         
	    PasswordField passwordField = new PasswordField();  
	     
	    Button signInButton = new Button("Sign in"); 
	    Button registerButton = new Button("Register");  
	    
	    GridPane gridPane = new GridPane();    
	    
	    gridPane.setMinSize(300, 150); 
	    
	    gridPane.setPadding(new Insets(10, 10, 10, 10)); 
	    
	    gridPane.setVgap(10); 
        gridPane.setHgap(10);       
        
	    gridPane.setAlignment(Pos.CENTER); 
	     
	    gridPane.add(loginLabel, 0, 0); 
	    gridPane.add(loginField, 1, 0); 
	    gridPane.add(passwordLabel, 0, 1);       
	    gridPane.add(passwordField, 1, 1); 
	    gridPane.add(signInButton, 0, 2); 
	    gridPane.add(registerButton, 1, 2);
	    
	    loginField.addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {

			@Override
			public void handle(KeyEvent event) {
				if(event.getCode()==KeyCode.ENTER) {
	    			String login = loginField.getText().trim();
					String password = passwordField.getText().trim();
					logIn("l", login, password);
	    		}
			}
	    	
		});
	       
	    passwordField.addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
	    	
	    	@Override
			public void handle(KeyEvent event) {
	    		if(event.getCode()==KeyCode.ENTER) {
	    			String login = loginField.getText().trim();
					String password = passwordField.getText().trim();
					logIn("l", login, password);
	    		}
			}
	    	
		});
	      
	    signInButton.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<Event>() {
	    	
			@Override
			public void handle(Event event) {
				String login = loginField.getText().trim();
				String password = passwordField.getText().trim();
				logIn("l", login, password);
			}
			
		});
	      
	    registerButton.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
	    	
	    	@Override
			public void handle(MouseEvent event) {
	    		String login = loginField.getText().trim();
				String password = passwordField.getText().trim();
				logIn("r", login, password);
	    	}
 
		});
	    
	    // text1.setStyle("-fx-font: normal bold 20px 'serif' "); 
	    // text2.setStyle("-fx-font: normal bold 20px 'serif' ");  
	    gridPane.setStyle("-fx-background-color: rgba(100, 149, 237,0.4);"); 
	    //100, 149, 237,0.4 niebieski
	    //176 217 236 niebieski
	    //92,183,92,0.5 zielony
	    loginScene = new Scene(gridPane); 
	}
	
	public Scene getInstance() {
		return loginScene;
	}
	
	private void logIn(String marker, String login, String password) {
		Alert alert = new Alert(AlertType.INFORMATION);
		alert.setHeaderText(null);
		
		
		if("".equals(login)||"".equals(password)) return;
		
		else {
			if(testCorrectnessOfLogin(login)&&testCorrectnessOfPassword(password)) {
				if(gui.authentication(marker+"/"+login+"/"+password)) {
					
					if(marker.equals("r")) alert.setTitle("pomyœlna rejestracja!");
					else if(marker.equals("l")) alert.setTitle("pomyœlne logowanie!");
					
					if(marker.equals("r")) alert.setContentText("Witaj "+login+" zosta³eœ pomyœlnie zarejestrowany!");
					else if(marker.equals("l")) alert.setContentText("Witaj "+login+" zosta³eœ pomyœlnie zalogowany!");
					
					alert.showAndWait();
					
					gui.setUserName(login);
					gui.loggedIn();
				}
				else {
					if(marker.equals("r")) alert.setContentText("u¿ytkownik ju¿ istnieje.");
					else if(marker.equals("l")) alert.setContentText("niepoprawne dane logowania.");
					alert.showAndWait();
				}
			}
			else {
				alert.setTitle("b³êdny login lub has³o, u¿yto niedozwolonych znaków!");
				alert.setContentText("login\n dozwolone znaki: a-z,A-Z,0-9 (bez polskich znaków)."
						+ "\n\nhas³o\n zabronione znaki: ' \\ /\n");
				alert.showAndWait();
			}
		}
	}
	
	private boolean testCorrectnessOfLogin(String login) {
		
		LinkedList<Character> correctCharsForLogin = new LinkedList<Character>();
		for(char i = 'a'; i<='z'; i++)
			correctCharsForLogin.add(i);
		for(char i = 'A'; i<='Z'; i++)
			correctCharsForLogin.add(i);
		for(char i = '0'; i<='9'; i++)
			correctCharsForLogin.add(i);
		
		LinkedList<Character> charsFromLogin = new LinkedList<Character>();
		for(int i = 0; i < login.length(); i++)
			charsFromLogin.add(login.charAt(i));
		
		charsFromLogin.removeAll(correctCharsForLogin);
		
		if(charsFromLogin.isEmpty())
			return true;
		else 
			return false;
	}
	
	private boolean testCorrectnessOfPassword(String password) {
		if(password.contains("'")||password.contains("/")||password.contains("\\"))
			return false;
		else
			return true;
	}
}
