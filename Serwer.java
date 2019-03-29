package Serwer;

import java.net.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.io.*;

public class Serwer {
	
	private static Connection connection;
	private ArrayList streamsToClients;
	
	public static void main(String [] args) {
		new Serwer().run();
		
	}
	
	private void run() {
		try {
			ServerSocket serverSocket = new ServerSocket(5437);
			streamsToClients = new ArrayList();
			System.out.println("serwer dzia³a.");
			
			Class.forName("org.hsqldb.jdbcDriver");
			connection = DriverManager.getConnection("jdbc:hsqldb:file:data/komunikator.db","sa","abc123");
			
			while(true) {
				System.out.println("czekam na klienta...");
				Socket clientSocket = serverSocket.accept();
				System.out.println("mamy klienta ! na adresie: "+clientSocket.getInetAddress());
				PrintWriter printer = new PrintWriter(clientSocket.getOutputStream());
				streamsToClients.add(printer);
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
		private BufferedReader input;
		private Socket clientSocket;
		
		public Client(Socket socket) {
			ID = null;
			try {
				clientSocket = socket;
				InputStreamReader isr = new InputStreamReader(clientSocket.getInputStream());
				input = new BufferedReader(isr);
			} catch (IOException ex) {
				ex.printStackTrace();
			}
			
		}
		
		public void run() {
			boolean ifLoggedIn = login();
			if(ifLoggedIn) {
				String message;
				try {
					while(((message = input.readLine())!=null)&&(!clientSocket.isInputShutdown())) {
						sendToEveryone(message);
					}
					streamsToClients.remove(this.clientSocket.getOutputStream());
				}
				catch(IOException ex) {
					ex.printStackTrace();
				}
			}
		}
		
		private boolean login() {
			String loginData;
			String login = null;
			try {
				while(((loginData = input.readLine())!=null)&&(!clientSocket.isInputShutdown())) {
					sendToEveryone(loginData);
					StringTokenizer str = new StringTokenizer(loginData);
					String marker = str.nextToken("/");
					login = str.nextToken("/").trim();
					String passwd = " ";
					if(str.hasMoreTokens())
						passwd = str.nextToken().trim();
					System.out.println(login +" "+passwd);
					Integer id=null;
					if(marker.equals("r"))
						id = SignIn(login, passwd);
					else if(marker.equals("l")) {
						id = SignUp(login, passwd);
						if(id!=null) {
							sendToEveryone("zalogowano jako id = "+id);
							ID=id;
							break;
						}
					}
					else sendToEveryone("nie udalo sie zalogowac");
				}
				if(clientSocket.isInputShutdown()) {
					streamsToClients.remove(this.clientSocket.getOutputStream());
					return false;
					}
			}
			catch(IOException ex) {
				ex.printStackTrace();
			}
			
			this.nick = login;
			return true;
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
