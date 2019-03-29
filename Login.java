package GUI;

import java.awt.AWTEvent;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

        public class Login {
        	
        	private JDialog dialog;
        	private JTextField loginField;
        	private JTextField passwordField;
        	private JLabel loginLabel;
        	private JLabel passwordLabel;
        	private JButton loginButton;
        	private JButton registryButton;
        	private String username;
        	private boolean loggedIn;
        	
            public Login(JFrame frame){
            	
            	dialog = new JDialog();
            	
            	dialog.setTitle("Login");
            	
            	loginField = new JTextField(15);
            	passwordField = new JTextField(15);
            	loginLabel = new JLabel("login:  ");
            	passwordLabel = new JLabel("has³o: ");
            	loginButton = new JButton("login");
            	registryButton = new JButton("zarejestruj");
            	username = null;
            	loggedIn = false;
            	
            	loginButton.addActionListener(new ActionListener() {
            		
					@Override
					public void actionPerformed(ActionEvent e) {
						boolean ifSuccessfulLogin = GUI.authentication(getData());
						if(ifSuccessfulLogin) {
							JOptionPane.showMessageDialog(dialog,
									"Witaj " + username + "!",
									"Login",
									JOptionPane.INFORMATION_MESSAGE);
							loggedIn = true;
							dialog.dispose();
						}
						else {
							JOptionPane.showMessageDialog(dialog,
		                            "nie ma takiego u¿ytkownika, mo¿e spróbuj za³o¿yæ konto.",
		                            "Login",
		                            JOptionPane.INFORMATION_MESSAGE);
						}
					}
				});
            	
            	registryButton.addActionListener(new ActionListener() {
					
					@Override
					public void actionPerformed(ActionEvent e) {
						boolean ifSuccessfulRegister = GUI.registry(getData());
						if(ifSuccessfulRegister) {
							JOptionPane.showMessageDialog(dialog,
									"Pomyœlnie zarejestrowano!",
									"Login",
									JOptionPane.INFORMATION_MESSAGE);
						}
						else {
							JOptionPane.showMessageDialog(dialog,
		                            "u¿ytkownik o takiej nazwie ju¿ istnieje.",
		                            "Login",
		                            JOptionPane.INFORMATION_MESSAGE);
						}
						
					}
				});
            	
            	dialog.setLayout(new FlowLayout());
            	
        		  dialog.add(loginLabel);
        		  dialog.add(loginField);
        		  dialog.add(passwordLabel);
        		  dialog.add(passwordField);
        		  dialog.add(loginButton);
        		  dialog.add(registryButton);
        		  
        		dialog.addWindowListener(new WindowListener() {
					
					@Override
					public void windowOpened(WindowEvent e) {
						// TODO Auto-generated method stub
						
					}
					
					@Override
					public void windowIconified(WindowEvent e) {
						// TODO Auto-generated method stub
						
					}
					
					@Override
					public void windowDeiconified(WindowEvent e) {
						// TODO Auto-generated method stub
						
					}
					
					@Override
					public void windowDeactivated(WindowEvent e) {
						// TODO Auto-generated method stub
						
					}
					
					@Override
					public void windowClosing(WindowEvent e) {
						dialog.dispose();
						frame.dispose();
						System.exit(2);
					}
					
					@Override
					public void windowClosed(WindowEvent e) {
						// TODO Auto-generated method stub
						
					}
					
					@Override
					public void windowActivated(WindowEvent e) {
						// TODO Auto-generated method stub
						
					}
				});
            	dialog.setLocation(500,250);
            	dialog.setSize(240, 130);
            	dialog.setResizable(false);
            	dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            	
            	dialog.setVisible(true);
            	dialog.toFront();
            }
            
            private String getData() {
            	String nick = loginField.getText().trim();
            	String password = passwordField.getText().trim();
            	loginField.setText("");
            	passwordField.setText("");
            	username = nick;
            	return nick+"/"+password;
            }
            
            public boolean isSuccesfullyLoggedIn() {
            	return loggedIn;
            }
            

        }