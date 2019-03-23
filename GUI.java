package klientU¿ytkownika;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.*;


public class GUI {
	private JFrame ramka;
	private JTextField poleWpisywania;
	private JTextArea poleRozmowy;
	private Socket gniazdo;
	private PrintWriter printer;
	private BufferedReader reader;
	private String nick;

	public static void main(String[] args) {
		GUI gui = new GUI();
		gui.run();
	}
	
	
	void run() {
		ramka =  new JFrame();
		ramka.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		JPanel panelCentralny = new JPanel();
		JPanel panelPoludniowy = new JPanel();
		
		
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
		
		Thread watekOdbierajacy = new Thread(new Odbiorca());
		watekOdbierajacy.start();
		
		nick = JOptionPane.showInputDialog("Podaj nick:");
		
		ramka.getContentPane().add(BorderLayout.CENTER,panelCentralny);
		ramka.getContentPane().add(BorderLayout.SOUTH,panelPoludniowy);
		ramka.setSize(400, 400);
		ramka.setLocation(400, 200);
		ramka.setResizable(false);
		ramka.setVisible(true);
		
	}
	
	public void wyslijTekst() {
		String tekst = poleWpisywania.getText();
		printer.println(nick+": "+tekst);
		printer.flush();
		poleWpisywania.setText("");
		poleWpisywania.requestFocus();
	}
	
	private void konfigurujPolaczenie() {
		try {
			gniazdo = new Socket("127.0.0.1", 5000);
			printer = new PrintWriter(gniazdo.getOutputStream());
			reader = new BufferedReader(new InputStreamReader(gniazdo.getInputStream()));
		}
		catch(IOException ex) {
			ex.printStackTrace();
			JOptionPane.showMessageDialog(ramka, "Nie uda³o siê po³¹czyæ z serwerem.");
			System.exit(1);
		}
		
	}
	
	public class Odbiorca implements Runnable{
		public void run() {
			String wiadomosc;
			try {
				while((wiadomosc = reader.readLine())!=null) {
					poleRozmowy.append("\n"+wiadomosc);
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

}
