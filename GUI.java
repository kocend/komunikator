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
	private JFrame ramka;
	private JTextField poleWpisywania;
	private JTextArea poleRozmowy;
	private JPanel panelCentralny;
	private JPanel panelPoludniowy;
	private JTextField loginField;
	private JTextField passwordField;
	private JPanel loginPanel;
	private Socket gniazdo;
	private PrintWriter printer;
	private BufferedReader reader;
	private String userName;
	

	public static void main(String[] args) {
		new GUI().run();
	}
	
	
	void run() {
		ramka =  new JFrame("komunikator 2.0");
		ramka.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		ramka.addWindowListener( new exitListener());
		
		panelCentralny = new JPanel();
		panelPoludniowy = new JPanel();
		
		
		JButton wyslij = new JButton("wyœlij");
		wyslij.addActionListener(new wyslijListener());
		
		poleWpisywania = new JTextField(25);
		poleWpisywania.addKeyListener(new poleWpisywaniaListener());
		poleWpisywania.requestFocusInWindow();
		
		poleRozmowy = new JTextArea(20, 30);
		poleRozmowy.setEditable(false);
		poleRozmowy.setLineWrap(true);
		
		JScrollPane przewijanie = new JScrollPane(poleRozmowy);
		przewijanie.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		przewijanie.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		
		panelCentralny.add(przewijanie);
		
		panelPoludniowy.add(poleWpisywania);
		panelPoludniowy.add(wyslij);
		
		
		konfigurujPolaczenie();
		
		
		ramka.setSize(400, 100);
		ramka.setLocation(400, 200);
		ramka.setResizable(false);
		
		
		//logowanie
		loginPanel = new JPanel();
		loginPanel.setLayout(new GridLayout(3, 2));
		
		loginField = new JTextField(15);
    	passwordField = new JTextField(15);
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
    	
    	ramka.getContentPane().add(BorderLayout.CENTER,loginPanel);
    	
    	ramka.setVisible(true);
	}
	
	public void wyslijTekst() {
		String message = poleWpisywania.getText().trim();
		printer.println(message);
		printer.flush();
		poleWpisywania.setText("");
		poleWpisywania.requestFocus();
	}
	
	public void loggedIn() {
		ramka.setVisible(false);
		
		ramka.setSize(400, 400);
		ramka.getContentPane().remove(loginPanel);
		ramka.getContentPane().add(BorderLayout.CENTER,panelCentralny);
		ramka.getContentPane().add(BorderLayout.SOUTH,panelPoludniowy);
		
		ramka.setVisible(true);
		
		Thread watekOdbierajacy = new Thread(new Odbiorca());
		watekOdbierajacy.start();
	}
	
	private void konfigurujPolaczenie() {
		try {
			gniazdo = new Socket("localhost", 5437);
			printer = new PrintWriter(gniazdo.getOutputStream());
			reader = new BufferedReader(new InputStreamReader(gniazdo.getInputStream()));
		}
		catch(IOException ex) {
			ex.printStackTrace();
			JOptionPane.showMessageDialog(ramka, "Nie uda³o siê po³¹czyæ z serwerem, spróbuj ponownie póŸniej.");
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
	
	
	public boolean authentication(String loginData) {
		loginData = "l/"+loginData.trim();
		  printer.println(loginData); 
		  printer.flush();

		String serverResponse = null;
		try {
			serverResponse = reader.readLine();
			System.out.println(serverResponse);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		if(("true").equals(serverResponse.trim()))
			return true;
		return false;
	}
	
	public boolean register(String registerData) {
		registerData = "r/"+registerData.trim();
		printer.println(registerData);
		printer.flush();
		
		String serverResponse = null;
		try {
			serverResponse = reader.readLine();
			System.out.println(serverResponse);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		if(("true").equals(serverResponse.trim()))
			return true;
		return false;
	}
	
	
	public class Odbiorca implements Runnable{
		public void run() {
			String wiadomosc;
			try {
				while((wiadomosc = reader.readLine())!=null) {
					poleRozmowy.append(wiadomosc+"\n");
				}
			}
			catch(IOException ex) {
				ex.printStackTrace();
			}
		}
	}
	
	
	public class wyslijListener implements ActionListener{
		public void actionPerformed(ActionEvent event) {
			wyslijTekst();
		}
	}
	
	private class loginListener implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent e) {
			String login = loginField.getText().trim();
			if(testCorrectnessOfLogin(login)) {
				if(!passwordField.getText().equals("")) {
					String loginData = login+"/"+passwordField.getText().trim();
					if(authentication(loginData)) {
						JOptionPane.showMessageDialog(ramka, "Witaj "+login+"! zosta³eœ zalogowany.");
						userName = login;
						loggedIn();
					}
					else {
						JOptionPane.showMessageDialog(ramka, "niepoprawny login lub has³o.");
					}
				}
				else JOptionPane.showMessageDialog(ramka, "has³o nie mo¿e byæ puste!");
			}
			else JOptionPane.showMessageDialog(ramka, "niepoprawny login, u¿yto niedozwolonych znaków.");
			loginField.setText("");
			passwordField.setText("");
		}
	}
	
	private class registerListener implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent e) {
			String login = loginField.getText().trim();
			if(testCorrectnessOfLogin(login)) {
				if(!passwordField.getText().equals("")) {
					String loginData = login+"/"+passwordField.getText().trim();
					if(register(loginData)) {
						JOptionPane.showMessageDialog(ramka, "Witaj "+login+"! zosta³eœ pomyœlnie zarejestrowany.");
						userName = login;
					}
					else {
						JOptionPane.showMessageDialog(ramka, "taki u¿ytkownik ju¿ istnieje!");
					}
				}
				else JOptionPane.showMessageDialog(ramka, "has³o nie mo¿e byæ puste!");
			}
			else JOptionPane.showMessageDialog(ramka, "niepoprawny login, u¿yto niedozwolonych znaków.");
			loginField.setText("");
			passwordField.setText("");
		}
	}
	
	
	public class poleWpisywaniaListener implements KeyListener{

		@Override
		public void keyPressed(KeyEvent arg0) {
			// TODO Auto-generated method stub
			if(arg0.getKeyChar()=='\n') {
				wyslijTekst();
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
				  gniazdo.shutdownOutput();
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
