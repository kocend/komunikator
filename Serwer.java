package klientSerwera;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.io.*;

public class Serwer {
	
	private ArrayList strumienieDoOdbiorc�w;
	
	public static void main(String [] args) {
		new Serwer().run();
		
	}
	
	public void run() {
		try {
			ServerSocket gniazdoSerwera = new ServerSocket(5000);
			strumienieDoOdbiorc�w = new ArrayList();
			
			while(true) {
				Socket gniazdoKlienta = gniazdoSerwera.accept();
				PrintWriter printer = new PrintWriter(gniazdoKlienta.getOutputStream());
				strumienieDoOdbiorc�w.add(printer);
				Thread watekKlienta = new Thread(new Odbiorca(gniazdoKlienta));
				watekKlienta.start();
			}
		}
		catch(IOException ex) {
			ex.printStackTrace();
		}
		
	}

	
	public void rozeslijWszystkim(String wiadomosc) {
		
		Iterator it = strumienieDoOdbiorc�w.iterator();
		while(it.hasNext()) {
			try {
			PrintWriter printer = (PrintWriter)it.next();
			printer.println(wiadomosc);
			printer.flush();
			}
			catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		
	}
	
	class Odbiorca implements Runnable{
		private BufferedReader input;
		private Socket gniazdoKlienta;
		
		public Odbiorca(Socket gniazdo) {
			try {
				gniazdoKlienta = gniazdo;
				InputStreamReader isr = new InputStreamReader(gniazdo.getInputStream());
				input = new BufferedReader(isr);
			}
			catch(IOException ex) {
				ex.printStackTrace();
			}
			
		}
		
		public void run() {
			String wiadomosc;
			try {
				while((wiadomosc = input.readLine())!=null)
					rozeslijWszystkim(wiadomosc);
			}
			catch(IOException ex) {
				ex.printStackTrace();
			}
		}
		
	}
}
