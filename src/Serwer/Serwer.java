package Serwer;

import java.net.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.StringTokenizer;

import Serwer.DAO.DAO;
import Serwer.DAO.hsqldbDAO;

import java.io.*;

public class Serwer {
	
	private DAO database;
	private LinkedList<PrintWriter> streamsToClients;
	private LinkedList<String> activeUsersNicks;
	private LinkedList<Socket> usersSockets;
	
	public static void main(String [] args) {
		new Serwer().run();
	}
	
	private void run() {
		
		try {
			ServerSocket serverSocket = new ServerSocket(5437);
			streamsToClients = new LinkedList<>();
			activeUsersNicks = new LinkedList<>();
			usersSockets = new LinkedList<>();
			System.out.println("serwer dzia³a.");
			
			database = new hsqldbDAO();
			
			Runtime.getRuntime().addShutdownHook(new OnExit());
			
			while(true) {
				System.out.println("czekam na klienta...");
				Socket clientSocket = serverSocket.accept();
				System.out.println("mamy klienta ! na adresie: "+clientSocket.getInetAddress());
				Thread clientThread = new Thread(new Client(clientSocket));
				clientThread.start();
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	
	private class OnExit extends Thread{
		@Override
		public void run() {
			Iterator<Socket> it = usersSockets.iterator();
			while(it.hasNext()) {
				try {
					it.next().shutdownOutput();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
	}
	
	private void sendToEveryone(String message) {
		
		Iterator it = streamsToClients.iterator();
		while(it.hasNext()) {
			try {
				PrintWriter printer = (PrintWriter)it.next();
				printer.println(message);
				printer.flush();
			}
			catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}
	
	
	
	class Client implements Runnable{
		private Integer ID;
		private String nick;
		private BufferedReader socketIn;
		private Socket clientSocket;
		
		public Client(Socket socket) {
			ID = null;
			try {
				clientSocket = socket;
				usersSockets.add(clientSocket);
				socketIn = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		
		public void run() {
			if(login()) {
				try {
					activeUsersNicks.add(nick);
					sendToEveryone("online/"+nick);
					streamsToClients.add(new PrintWriter(clientSocket.getOutputStream()));
				
					String message;
					while(((message = socketIn.readLine())!=null)&&(!clientSocket.isInputShutdown())) {
						sendToEveryone("message/"+nick+": " + message);
						if(message.length()<=500)database.saveMessage(nick, message);
					}
					
					if(clientSocket.isInputShutdown()) {
						clientSocket.shutdownOutput();
						System.out.println("klient "+clientSocket.getInetAddress()+" siê od³¹czy³");
					}
					streamsToClients.remove(clientSocket.getOutputStream());
					sendToEveryone("offline/"+nick);
					activeUsersNicks.remove(nick);
					usersSockets.remove(clientSocket);
				} catch (IOException e) {
					
					try {
						streamsToClients.remove(clientSocket.getOutputStream());
					} catch (IOException e1) {
						e1.printStackTrace();
					}
					sendToEveryone("offline/"+nick);
					activeUsersNicks.remove(nick);
					usersSockets.remove(clientSocket);
					
					e.printStackTrace();
				}
			}
		}
		
		private boolean login() {
			String loginData;
			String login = null;
			try {
				while(((loginData = socketIn.readLine())!=null)&&(!clientSocket.isInputShutdown())) {
					
					PrintWriter socketOut = new PrintWriter(clientSocket.getOutputStream());
					
					System.out.println(loginData);
					
					if (loginData.contains("'")) {
							socketOut.println("false");
							socketOut.flush();
							continue;
					}
					
					String [] ifCorrectNumberofSlash = loginData.split("/");
					if(ifCorrectNumberofSlash.length!=3) {
						socketOut.println("false");
						socketOut.flush();
						continue;
					}
					
					StringTokenizer str = new StringTokenizer(loginData);
					String marker = str.nextToken("/");
					login = str.nextToken("/");
					
					String passwd = "";
					if(str.hasMoreTokens())
						passwd = str.nextToken();
					
					if(activeUsersNicks.contains(login)) {
						socketOut.println("false");
						socketOut.flush();
						continue;
					}
					
					
					Integer id=null;
					
					if(marker.equals("r")) {
						id = database.SignUp(login, passwd);
						if(id!=null) {
							socketOut.println("true");
							socketOut.flush();
							ID=id;
							break;
						}
					}
					
					if(marker.equals("l")) {
						id = database.SignIn(login, passwd);
						if(id!=null) {
							socketOut.println("true");
							socketOut.flush();
							ID=id;
							break;
						}
					}
					
					socketOut.println("false");
					socketOut.flush();
				}
				if(clientSocket.isInputShutdown()) {
					clientSocket.shutdownOutput();
					System.out.println("klient "+clientSocket.getInetAddress()+" siê od³¹czy³");
					return false;
				}
			}
			catch(IOException ex) {
				ex.printStackTrace();
			}
			sendActiveUsers();
			loadMessages();
			this.nick = login;
			return true;
		}
		
		private void loadMessages() {
			PrintWriter socketOut = null;
			try {
				socketOut = new PrintWriter(clientSocket.getOutputStream());
			} catch (IOException e) {
				e.printStackTrace();
			}
			String messages = database.loadMessages();
			if(messages.length()!=0) {
				socketOut.println(messages);
				socketOut.flush();
			}
		}

		private void sendActiveUsers() {
			try {
				PrintWriter socketOut = new PrintWriter(clientSocket.getOutputStream());
				Iterator it = activeUsersNicks.iterator();
				while(it.hasNext()) {
					socketOut.println("online/"+(String)it.next());
				}
				socketOut.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
	}
}