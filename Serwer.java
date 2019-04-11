package Serwer;

import java.net.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.*;

public class Serwer {
	
	private static Connection connection;
	private LinkedList<PrintWriter> streamsToClients;
	private LinkedList<String> activeUsersNicks;
	
	public static void main(String [] args) {
		new Serwer().run();
		
	}
	
	private void run() {
		
		try {
			ServerSocket serverSocket = new ServerSocket(5437);
			streamsToClients = new LinkedList<PrintWriter>();
			activeUsersNicks = new LinkedList<String>();
			System.out.println("serwer dzia³a.");
			
			Class.forName("org.hsqldb.jdbcDriver");
			connection = DriverManager.getConnection("jdbc:hsqldb:file:data/komunikator.db","sa","abc123");
			
			while(true) {
				System.out.println("czekam na klienta...");
				Socket clientSocket = serverSocket.accept();
				System.out.println("mamy klienta ! na adresie: "+clientSocket.getInetAddress());
				Thread clientThread = new Thread(new Client(clientSocket));
				clientThread.start();
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}
	
	
	  private synchronized ResultSet queryToBAse(String query) { 
		 
		  ResultSet result = null;
	  
		  try { 
			  Statement statement = connection.createStatement();
			  result = statement.executeQuery(query);
		  } catch (SQLException e) {
			  e.printStackTrace(); 
		  } 
		  return result;
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
				socketIn = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		
		public void run() {
			if(login()) {
				try {
					activeUsersNicks.add(nick);
					//sendToEveryone("Server: "+nick+" has just logged in.");
					sendToEveryone("online/"+nick);
					streamsToClients.add(new PrintWriter(clientSocket.getOutputStream()));
				
					String message;
					while(((message = socketIn.readLine())!=null)&&(!clientSocket.isInputShutdown()))
						sendToEveryone("message/"+nick+": " + message);
				
					streamsToClients.remove(clientSocket.getOutputStream());
					sendToEveryone("offline/"+nick);
					activeUsersNicks.remove(nick);
					//sendToEveryone("Server: "+nick+" has just logged out.");
				} catch (IOException e) {
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
						id = SignUp(login, passwd);
						if(id!=null) {
							socketOut.println("true");
							socketOut.flush();
							continue;
						}
					}
					
					if(marker.equals("l")) {
						id = SignIn(login, passwd);
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
				if(clientSocket.isInputShutdown()) return false;
			}
			catch(IOException ex) {
				ex.printStackTrace();
			}
			sendActiveUsers();
			this.nick = login;
			return true;
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
	
	private synchronized Integer SignIn(String login, String password) {
		Integer id = null;
		String query = "SELECT ID FROM USERS WHERE " 
						+"nick = '"+login
						+"' AND "
						+"passwd = '"+password
						+"';";
		ResultSet result = queryToBAse(query);
		try {
			while(result.next())
				id = result.getInt("ID");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return id;
	}
	
	private synchronized Integer SignUp(String login, String password) {
		Integer id = null;
		String query = "SELECT ID FROM USERS WHERE "
						+"nick = '"+login
						+"';";
		ResultSet result = queryToBAse(query);
		try {
			while(result.next())
				return null;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		String addQuery = "INSERT INTO USERS (nick,passwd) VALUES ('"
							+login
							+"','"
							+password
							+"');";
		
		queryToBAse(addQuery);
		
		result = queryToBAse(query);
		
		try {
			while(result.next())
				id=result.getInt("ID");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return id;
	}
	
}
