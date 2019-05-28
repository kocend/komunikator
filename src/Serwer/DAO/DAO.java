package Serwer.DAO;

import java.sql.ResultSet;

public interface DAO {

	ResultSet queryToBAse(String query);
	Integer SignIn(String login, String password);
	Integer SignUp(String login, String password);
	void saveMessage(String sender, String message);
	String loadMessages();
}
