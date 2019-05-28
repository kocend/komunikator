package chatClient;

import java.util.LinkedList;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

public class LoginScene {

    private Gui gui;
    private Scene loginScene;
    private Text errorMessage;

    public LoginScene(Gui obj) {
        gui=obj;

        Text loginLabel = new Text("Login");
        Text passwordLabel = new Text("Password");
        errorMessage = new Text("");
        errorMessage.setFill(Color.RED);

        TextField loginField = new TextField();
        loginField.setTooltip(new Tooltip("dozwolone znaki: a-z,A-Z,0-9 (bez polskich znaków)"));
        PasswordField passwordField = new PasswordField();
        passwordField.setTooltip(new Tooltip("zabronione znaki: ' \\ /"));

        Button signInButton = new Button("Sign in");
        signInButton.setTooltip(new Tooltip("kliknij aby się zalogować"));
        Button registerButton = new Button("Register");
        registerButton.setTooltip(new Tooltip("kliknij aby się zarejestrować"));

        GridPane typingPane = new GridPane();
        typingPane.setPadding(new Insets(10, 10, 10, 10));
        typingPane.setVgap(10);
        typingPane.setHgap(10);
        typingPane.add(loginLabel, 0, 0);
        typingPane.add(loginField, 1, 0);
        typingPane.add(passwordLabel, 0, 1);
        typingPane.add(passwordField, 1, 1);

        GridPane buttonPane = new GridPane();
        buttonPane.setPadding(new Insets(10, 10, 10, 10));
        buttonPane.setVgap(10);
        buttonPane.setHgap(10);
        buttonPane.add(signInButton, 0, 0);
        buttonPane.add(registerButton, 1, 0);

        GridPane gridPane = new GridPane();
        gridPane.setMinSize(300, 200);
        gridPane.setPadding(new Insets(10, 10, 10, 10));
        gridPane.setVgap(10);
        gridPane.setHgap(10);
        gridPane.setAlignment(Pos.CENTER);

        gridPane.add(typingPane, 0, 0);
        gridPane.add(errorMessage, 0, 1);
        gridPane.add(buttonPane, 0, 2);

        loginField.addEventHandler(KeyEvent.KEY_PRESSED, (KeyEvent event)->{
            errorMessage.setText("");
            if(event.getCode()==KeyCode.ENTER) {
                String login = loginField.getText().trim();
                String password = passwordField.getText().trim();
                logIn("l", login, password);
            }
        });

        passwordField.addEventHandler(KeyEvent.KEY_PRESSED, (KeyEvent event)->{
            errorMessage.setText("");
            if(event.getCode()==KeyCode.ENTER) {
                String login = loginField.getText().trim();
                String password = passwordField.getText().trim();
                logIn("l", login, password);
            }
        });

        signInButton.addEventHandler(MouseEvent.MOUSE_CLICKED, (MouseEvent event)->{
            String login = loginField.getText().trim();
            String password = passwordField.getText().trim();
            logIn("l", login, password);
        });

        registerButton.addEventHandler(MouseEvent.MOUSE_CLICKED, (MouseEvent event)->{
            String login = loginField.getText().trim();
            String password = passwordField.getText().trim();
            logIn("r", login, password);
        });

        gridPane.setStyle("-fx-background-color: rgba(100, 149, 237,0.4);");

        loginScene = new Scene(gridPane);
    }

    public Scene getInstance() {
        return loginScene;
    }

    private void logIn(String marker, String login, String password) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setHeaderText(null);
        if(!("".equals(login)&&"".equals(password))){
            if(testCorrectnessOfLogin(login)&&testCorrectnessOfPassword(password)) {
                if(gui.authentication(marker+"/"+login+"/"+password)) {

                    if(marker.equals("r")) alert.setTitle("pomyślna rejestracja!");
                    else if(marker.equals("l")) alert.setTitle("pomyślne logowanie!");

                    if(marker.equals("r")) alert.setContentText("Witaj "+login+" zostałeś pomyślnie zarejestrowany!");
                    else if(marker.equals("l")) alert.setContentText("Witaj "+login+" zostałeś pomyślnie zalogowany!");

                    alert.showAndWait();

                    gui.setUserName(login);
                    gui.loggedIn();
                }
                else {
                    if(marker.equals("r")) errorMessage.setText("użytkownik już istnieje.");
                    else if(marker.equals("l")) errorMessage.setText("niepoprawne dane logowania.");
                }
            }
            else {
                errorMessage.setText("użyto niedozwolonych znaków");
            }
        }
    }

    private boolean testCorrectnessOfLogin(String login) {

        LinkedList<Character> correctCharsForLogin = new LinkedList<>();
        for(char i = 'a'; i<='z'; i++)
            correctCharsForLogin.add(i);
        for(char i = 'A'; i<='Z'; i++)
            correctCharsForLogin.add(i);
        for(char i = '0'; i<='9'; i++)
            correctCharsForLogin.add(i);

        LinkedList<Character> charsFromLogin = new LinkedList<>();
        for(int i = 0; i < login.length(); i++)
            charsFromLogin.add(login.charAt(i));

        charsFromLogin.removeAll(correctCharsForLogin);

        return charsFromLogin.isEmpty();
    }

    private boolean testCorrectnessOfPassword(String password) {
        return !(password.contains("'")||password.contains("/")||password.contains("\\"));
    }
}
