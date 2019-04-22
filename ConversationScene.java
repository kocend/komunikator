package GUI;

import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;


public class ConversationScene {
	
	private Gui gui;
	private Scene conversationScene;
	private TextField typingField;
	private TextArea conversationArea;
	private ListView<String> onlineUsers;
	

	public ConversationScene(Gui obj) {
		gui=obj;
		Text areaLabel = new Text("rozmowa:");
	    Text onlineLabel = new Text("online:"); 
	       
	    conversationArea = new TextArea();
	    conversationArea.setPrefSize(300, 300);
	    conversationArea.setWrapText(true);
	    conversationArea.setEditable(false);
	    
	    onlineUsers = new ListView<String>();
	    onlineUsers.setPrefSize(20, 300);
	    onlineUsers.setEditable(false);
	    
	    
	    typingField = new TextField();
	    typingField.addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
	    	
			@Override
			public void handle(KeyEvent event) {
				if(event.getCode()==KeyCode.ENTER)
					gui.sendText();
			}
			
		});
	     
	    Button sendButton = new Button("wyœlij"); 
	    sendButton.addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
	    	
			@Override
			public void handle(KeyEvent event) {
				gui.sendText();
			}
			
		});
	    
	    GridPane gridPane = new GridPane();    
	    
	    gridPane.setPadding(new Insets(10, 10, 10, 10)); 
	    
	    gridPane.setVgap(10); 
        gridPane.setHgap(10);       
        
	    gridPane.setAlignment(Pos.CENTER); 
	     
	    gridPane.add(areaLabel, 0, 0); 
	    gridPane.add(onlineLabel, 1, 0); 
	    gridPane.add(conversationArea, 0, 1);       
	    gridPane.add(onlineUsers, 1, 1); 
	    gridPane.add(typingField, 0, 2); 
	    gridPane.add(sendButton, 1, 2);
	    
	    gridPane.setStyle("-fx-background-color: rgba(100, 149, 237,0.4);");
	    
	    conversationScene = new Scene(gridPane);
	    typingField.requestFocus();
	}
	
	public Scene getInstance() {
		return conversationScene;
	}

	public TextField getTypingField() {
		return typingField;
	}

	public TextArea getConversationArea() {
		return conversationArea;
	}

	public ListView<String> getOnlineUsers() {
		return onlineUsers;
	}
}