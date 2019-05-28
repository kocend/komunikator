package Serwer.DAO;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class hsqldbDAO implements DAO {
	
	private Connection conn;
	String driverClassName = "org.hsqldb.jdbcDriver";
	String connectionUrl = "jdbc:hsqldb:file:data/komunikator.db";
	String dbUser = "sa";
	String dbPwd = "abc123";
	
	public hsqldbDAO() {
		try {
			Class.forName(driverClassName);
			conn = DriverManager.getConnection(connectionUrl, dbUser, dbPwd);
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	@Override
	public synchronized Integer SignIn(String login, String password) {
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

	@Override
	public synchronized Integer SignUp(String login, String password) {
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

	@Override
	public synchronized void saveMessage(String sender, String message) {
		System.out.println(message);
		message = message.replaceAll("'", "''");
		//message = message.replaceAll("\", "a");
		String query = "INSERT INTO messages values('"+sender+"','all','"+message+"',now());";
		queryToBAse(query);
		System.out.println(query);
	}

	@Override
	public synchronized String loadMessages() {
		StringBuilder messages = new StringBuilder();
		ResultSet result = queryToBAse("SELECT sender, message FROM MESSAGES;");
		try {
			while(result.next()) {
				messages.append("message/"+result.getString(1)+": ");
				messages.append(result.getString(2));
				messages.append('\n');
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		if(messages.length()!=0) {
			messages.deleteCharAt(messages.length()-1);
			//TODO replace all '' to '
		}
		return messages.toString();
	}

	@Override
	public synchronized ResultSet queryToBAse(String query) {

		  ResultSet result = null;
	  
		  try { 
			  Statement statement = conn.createStatement();
			  result = statement.executeQuery(query);
		  } catch (SQLException e) {
			  e.printStackTrace(); 
		  } 
		  return result;
	}
	

}
