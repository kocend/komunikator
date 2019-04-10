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


public class GUI {
	private JFrame frame;
	private JTextField typingField;
	private JTextArea conversationArea;
	//private JCheckBox contactsBox;
	private JPanel centralPanel;
	private JPanel southPanel;
	//private JPanel contactsPanel;
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
		frame =  new JFrame("komunikator 2.0");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.addWindowListener( new exitListener());
		
		centralPanel = new JPanel();
		southPanel = new JPanel();
		//contactsPanel = new JPanel();
		
		JButton sendButton = new JButton("wyœlij");
		sendButton.addActionListener(new sendButtonListener());
		
		typingField = new JTextField(24);
		typingField.addKeyListener(new typingFieldListener());
		typingField.requestFocusInWindow();
		
		conversationArea = new JTextArea(20, 30);
		conversationArea.setEditable(false);
		conversationArea.setLineWrap(true);
		
		JScrollPane scrolling = new JScrollPane(conversationArea);
		scrolling.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scrolling.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		
		//contactsBox = new JCheckBox();
		//contactsBox.setSize(10, 30);
		
		centralPanel.add(scrolling);
		
		southPanel.add(typingField);
		southPanel.add(sendButton);
		
		//contactsPanel.add(contactsBox);
		
		configureConnection();
		
		
		frame.setSize(400, 100);
		frame.setLocation(400, 200);
		frame.setResizable(false);
		
		
		//logging in
		loginPanel = new JPanel();
		loginPanel.setLayout(new GridLayout(3, 2));
		
		loginField = new JTextField(15);
    	passwordField = new JPasswordField(15);
    	JLabel loginLabel = new JLabel("login: ");
    	JLabel passwordLabel = new JLabel("has³o: ");
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
		frame.getContentPane().add(BorderLayout.CENTER,centralPanel);
		frame.getContentPane().add(BorderLayout.SOUTH,southPanel);
		//frame.getContentPane().add(BorderLayout.EAST,contactsPanel);
		
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
			JOptionPane.showMessageDialog(frame, "Nie uda³o siê po³¹czyæ z serwerem, spróbuj ponownie póŸniej.");
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
					conversationArea.append(message+"\n");
				}
			}
			catch(IOException ex) {
				ex.printStackTrace();
			}
		}
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
						JOptionPane.showMessageDialog(frame, "Witaj "+login+"! zosta³eœ zalogowany.");
						userName = login;
						loggedIn();
					}
					else {
						JOptionPane.showMessageDialog(frame, "niepoprawny login lub has³o.");
					}
				}
				else JOptionPane.showMessageDialog(frame, "has³o nie mo¿e byæ puste!");
			}
			else JOptionPane.showMessageDialog(frame, "niepoprawny login, u¿yto niedozwolonych znaków.");
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
							JOptionPane.showMessageDialog(frame, "Witaj "+login+"! zosta³eœ pomyœlnie zarejestrowany.");
							userName = login;
						}
						else {
							JOptionPane.showMessageDialog(frame, "taki u¿ytkownik ju¿ istnieje!");
						}
					}
				else JOptionPane.showMessageDialog(frame, "has³o nie mo¿e byæ puste!");
				}
				else JOptionPane.showMessageDialog(frame, "has³o zawiera niedozwolone znaki takie jak:   '   /   \\     ");
			}
			else JOptionPane.showMessageDialog(frame, "niepoprawny login, u¿yto niedozwolonych znaków.");
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
