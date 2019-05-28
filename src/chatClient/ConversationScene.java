package chatClient;

import java.util.LinkedList;
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
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;


public class ConversationScene {
	
	private Gui gui;
	private Scene conversationScene;
	private TextField typingField;
	private TextArea conversationArea;
	private LinkedList<String> activeUsers;
	
	public ConversationScene(Gui obj) {
		
		gui=obj;
		
		Text areaLabel = new Text("rozmowa:");
	    Text onlineLabel = new Text("online:"); 
	       
	    conversationArea = new TextArea();
	    conversationArea.setPrefSize(300, 300);
	    conversationArea.setWrapText(true);
	    conversationArea.setEditable(false);
	    
	    activeUsers = new LinkedList<>();
	    ListView<String> onlineUsers = new ListView<>();
	    onlineUsers.setPrefSize(20, 300);
	    onlineUsers.getItems().addAll(activeUsers);
	    
	    
	    typingField = new TextField();
	    typingField.addEventHandler(KeyEvent.KEY_PRESSED, (KeyEvent event)->{if(event.getCode()==KeyCode.ENTER)gui.sendText();});
	     
	    Button sendButton = new Button("wyœlij"); 
	    sendButton.addEventHandler(MouseEvent.MOUSE_CLICKED, (MouseEvent event)->{gui.sendText();});
	    
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

	public LinkedList<String> getActiveUsers() {
		return activeUsers;
	}
}
