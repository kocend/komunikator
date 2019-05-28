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
        primaryStage.setResizable(false);

        try{primaryStage.getIcons().add(new Image(this.getClass().getResource("icon.png").toString()));}
        catch(Exception ex) {System.out.println("brak icon.png w plikach bin programu");}

        stage.setOnCloseRequest((WindowEvent event)->{
            try{
                socket.shutdownOutput();
                System.out.println("close request!");
                Platform.exit();
                System.exit(0);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });


        Scene loginScene = new LoginScene(this).getInstance();
        primaryStage.setScene(loginScene);
        primaryStage.show();
    }

    protected void loggedIn() {
        primaryStage.hide();

        conversationScene = new ConversationScene(this);
        Scene convScene = conversationScene.getInstance();
        primaryStage.setScene(convScene);

        primaryStage.show();

        Thread receiverThread = new Thread(new Receiver());
        receiverThread.start();
    }


    protected void sendText() {
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
            alert.setContentText("Nie udało się połączyć z serwerem, spróbuj ponownie później.");
            alert.showAndWait();
            System.exit(1);
        }
    }



    protected boolean authentication(String loginData) {
        loginData = loginData.trim();
        socketOut.println(loginData);
        socketOut.flush();

        String serverResponse = "false";
        try {
            serverResponse = socketIn.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return ("true").equals(serverResponse.trim());
    }


    public String getUserName() {
        return userName;
    }


    protected void setUserName(String userName) {
        this.userName = userName;
    }


    public class Receiver implements Runnable{
        public void run() {
            String message;
            try {
                while(((message = socketIn.readLine())!=null)&&(!socket.isInputShutdown())) {
                    System.out.println("");
                    System.out.println(message);
                    StringTokenizer str = new StringTokenizer(message);
                    System.out.println(str.toString());
                    String command = str.nextToken("/");
                    System.out.println(command);
                    String text = str.nextToken("\n");
                    System.out.println(text);
                    text=text.substring(1);
                    System.out.println(text);
                    final String finalText = text;

                    switch(command) {
                        case "message": addMessage(finalText);
                            break;
                        case "online": addToOnlineUsers(text);
                            break;
                        case "offline": removeFromOnlineUsers(text);
                            break;
                    }
                }
            }
            catch(IOException ex) {
                if(!socket.isInputShutdown()) {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            Alert alert = new Alert(AlertType.ERROR);
                            alert.initOwner(primaryStage);
                            alert.setHeaderText(null);
                            alert.setContentText("połączenie z serwerem zerwane, spróbuj uruchomić aplikację jeszcze raz.");
                            alert.showAndWait();
                            System.exit(1);
                        }
                    });
                }
            }
        }

        private void addMessage(final String message){
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    conversationScene.getConversationArea().appendText(message+"\n");
                }
            });
        }


        private void addToOnlineUsers(String user){
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    conversationScene.addUser(user);
                }
            });
        }

        private void removeFromOnlineUsers(String user) {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    conversationScene.removeUser(user);
                }
            });
        }
    }
}
