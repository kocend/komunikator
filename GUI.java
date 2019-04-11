package GUI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.*;
import java.util.LinkedList;
import java.util.StringTokenizer;


public class GUI {
	private JFrame frame;
	private JTextField typingField;
	private JTextArea conversationArea;
	private JList activeUsersList;
	private DefaultListModel<String> activeUsers;
	private JPanel centralPanel;
	private JPanel southPanel;
	private JPanel contactsPanel;
	private JTextField loginField;
	private JPasswordField passwordField;
	private JPanel loginPanel;
	private static String serverAddress;
	private Socket socket;
	private PrintWriter socketOut;
	private BufferedReader socketIn;
	private String userName;
	

	public static void main(String[] args) {
		/*
		 * if(args.length!=1) { System.out.println("adres ip serwera wymagany.");
		 * System.exit(3); } serverAddress = args[0];
		 */
		serverAddress = "localhost";
		new GUI().run();
	}
	
	
	void run() {
		
		configureConnection();
		
		
		frame =  new JFrame("komunikator 2.0");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.addWindowListener( new exitListener());
		
		centralPanel = new JPanel();
		southPanel = new JPanel();
		contactsPanel = new JPanel();
		
		JButton sendButton = new JButton("wy�lij");
		sendButton.addActionListener(new sendButtonListener());
		
		typingField = new JTextField(28);
		typingField.addKeyListener(new typingFieldListener());
		typingField.requestFocusInWindow();
		
		JLabel conversationLabel = new JLabel("pole konwersacji:");
		
		conversationArea = new JTextArea(20, 26);
		conversationArea.setEditable(false);
		conversationArea.setLineWrap(true);
		
		
		JLabel onlineUserLabel = new JLabel("online:");
		
		activeUsers = new DefaultListModel<String>();
		activeUsersList = new JList<String>(activeUsers);
		activeUsersList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		
		JScrollPane conversationAreaScrolling = new JScrollPane(conversationArea);
		conversationAreaScrolling.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		conversationAreaScrolling.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		
		JScrollPane contactsListScrolling = new JScrollPane(activeUsersList);
		contactsListScrolling.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		contactsListScrolling.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		
		centralPanel.setLayout(new BoxLayout(centralPanel, BoxLayout.Y_AXIS));
		centralPanel.add(conversationLabel);
		centralPanel.add(conversationAreaScrolling);
		
		southPanel.add(typingField);
		southPanel.add(sendButton);
		
		contactsPanel.setLayout(new BoxLayout(contactsPanel, BoxLayout.Y_AXIS));
		contactsPanel.add(onlineUserLabel);
		contactsPanel.add(contactsListScrolling);
		
		
		frame.setSize(400, 100);
		frame.setLocation(400, 200);
		frame.setResizable(false);
		
		
		//logging in
		loginPanel = new JPanel();
		loginPanel.setLayout(new GridLayout(3, 2));
		
		loginField = new JTextField(15);
    	passwordField = new JPasswordField(15);
    	JLabel loginLabel = new JLabel("login: ");
    	JLabel passwordLabel = new JLabel("has�o: ");
    	JButton loginButton = new JButton("login");
    	JButton registerButton = new JButton("zarejestruj");
    	loginButton.addActionListener(new loginListener());
    	registerButton.addActionListener(new registerListener());
    	
    	loginPanel.add(loginLabel);
    	loginPanel.add(loginField);
    	loginPanel.add(passwordLabel);
    	loginPanel.add(passwordField);
    	loginPanel.add(loginButton);
    	loginPanel.add(registerButton);
    	
    	frame.getContentPane().add(BorderLayout.CENTER,loginPanel);
    	
    	frame.setVisible(true);
	}
	
	public void sendText() {
		String message = typingField.getText().trim();
		socketOut.println(message);
		socketOut.flush();
		typingField.setText("");
		typingField.requestFocus();
	}
	
	public void loggedIn() {
		frame.setVisible(false);
		
		frame.setSize(400, 410);
		frame.getContentPane().remove(loginPanel);
		frame.getContentPane().add(BorderLayout.WEST,centralPanel);
		frame.getContentPane().add(BorderLayout.SOUTH,southPanel);
		frame.getContentPane().add(BorderLayout.CENTER,contactsPanel);
		
		frame.setVisible(true);
		
		Thread receiverThread = new Thread(new Receiver());
		receiverThread.start();
	}
	
	private void configureConnection() {
		try {
			socket = new Socket(serverAddress, 5437);
			socketOut = new PrintWriter(socket.getOutputStream());
			socketIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		}
		catch(IOException ex) {
			ex.printStackTrace();
			JOptionPane.showMessageDialog(frame, "Nie uda�o si� po��czy� z serwerem, spr�buj ponownie p�niej.");
			System.exit(1);
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
					case "message": conversationArea.append(text+"\n");
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
				JOptionPane.showMessageDialog(frame, "po��czenie z serwerem zerwane, spr�buj uruchomi� aplikacj� jeszcze raz.");
				System.exit(1);
			}
		}
	}
	
	private void addToOnlineUsers(String user) {
		activeUsers.addElement(user);
	}
	
	private void removeFromOnlineUsers(String user) {
		activeUsers.removeElement(user);
	}
	
	
	public class sendButtonListener implements ActionListener{
		public void actionPerformed(ActionEvent event) {
			sendText();
		}
	}
	
	private class loginListener implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent e) {
			String login = loginField.getText().trim();
			if(testCorrectnessOfLogin(login)) {
				if(!passwordField.getText().equals("")) {
					String loginData = "l/"+login+"/"+passwordField.getText().trim();
					if(authentication(loginData)) {
						JOptionPane.showMessageDialog(frame, "Witaj "+login+"! zosta�e� zalogowany.");
						userName = login;
						loggedIn();
					}
					else {
						JOptionPane.showMessageDialog(frame, "niepoprawny login lub has�o.");
					}
				}
				else JOptionPane.showMessageDialog(frame, "has�o nie mo�e by� puste!");
			}
			else JOptionPane.showMessageDialog(frame, "niepoprawny login, u�yto niedozwolonych znak�w.");
			loginField.setText("");
			passwordField.setText("");
		}
	}
	
	private class registerListener implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent e) {
			String login = loginField.getText().trim();
			if(testCorrectnessOfLogin(login)) {
				if(testCorrectnessOfPassword(passwordField.getText())) {
					if(!passwordField.getText().equals("")) {
						String loginData = "r/"+login+"/"+passwordField.getText().trim();
						if(authentication(loginData)) {
							JOptionPane.showMessageDialog(frame, "Witaj "+login+"! zosta�e� pomy�lnie zarejestrowany.");
							userName = login;
						}
						else {
							JOptionPane.showMessageDialog(frame, "taki u�ytkownik ju� istnieje!");
						}
					}
				else JOptionPane.showMessageDialog(frame, "has�o nie mo�e by� puste!");
				}
				else JOptionPane.showMessageDialog(frame, "has�o zawiera niedozwolone znaki takie jak:   '   /   \\     ");
			}
			else JOptionPane.showMessageDialog(frame, "niepoprawny login, u�yto niedozwolonych znak�w.");
			loginField.setText("");
			passwordField.setText("");
		}
	}
	
	
	public class typingFieldListener implements KeyListener{

		@Override
		public void keyPressed(KeyEvent arg0) {
			// TODO Auto-generated method stub
			if(arg0.getKeyChar()=='\n') {
				sendText();
			}
		}

		@Override
		public void keyReleased(KeyEvent arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void keyTyped(KeyEvent arg0) {
			// TODO Auto-generated method stub
			
		}
	}
	
	public class exitListener implements WindowListener{

		@Override
		public void windowActivated(WindowEvent e) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void windowClosed(WindowEvent e) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void windowClosing(WindowEvent e) {
			  try {
				  socket.shutdownOutput();
				  } catch(IOException ex) {
					  ex.printStackTrace(); 
			  }
		}

		@Override
		public void windowDeactivated(WindowEvent e) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void windowDeiconified(WindowEvent e) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void windowIconified(WindowEvent e) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void windowOpened(WindowEvent e) {
			// TODO Auto-generated method stub
			
		}
		
	}

}
