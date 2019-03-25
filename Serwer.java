package klientSerwera;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.io.*;

public class Serwer {
	
	private ArrayList strumienieDoOdbiorców;
	
	public static void main(String [] args) {
		new Serwer().run();
		
	}
	
	public void run() {
		try {
			ServerSocket gniazdoSerwera = new ServerSocket(5437);
			strumienieDoOdbiorców = new ArrayList();
			System.out.println("serwer dzia³a.");
			
			while(true) {
				System.out.println("czekam na klienta...");
				Socket gniazdoKlienta = gniazdoSerwera.accept();
				System.out.println("mamy klienta ! na adresie: "+gniazdoKlienta.getInetAddress());
				PrintWriter printer = new PrintWriter(gniazdoKlienta.getOutputStream());
				strumienieDoOdbiorców.add(printer);
				Thread watekKlienta = new Thread(new Odbiorca(gniazdoKlienta));
				watekKlienta.start();
			}
		}
		catch(IOException ex) {
			ex.printStackTrace();
		}
		
	}

	
	public void rozeslijWszystkim(String wiadomosc) {
		
		Iterator it = strumienieDoOdbiorców.iterator();
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
				while(((wiadomosc = input.readLine())!=null)&&(!gniazdoKlienta.isInputShutdown()))
					rozeslijWszystkim(wiadomosc);
			}
			catch(IOException ex) {
				ex.printStackTrace();
			}
			
			try {
				strumienieDoOdbiorców.remove(this.gniazdoKlienta.getOutputStream());
			}
			catch(IOException ex) {
				ex.printStackTrace();
			}
		}
		
	}
}
