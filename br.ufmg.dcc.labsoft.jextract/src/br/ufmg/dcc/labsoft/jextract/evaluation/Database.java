package br.ufmg.dcc.labsoft.jextract.evaluation;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class Database {

	private Connection connection = null;
	private Statement statement = null;
	private PreparedStatement preparedStatement = null;
	private ResultSet resultSet = null;
	
	public Database() throws Exception {
		Class.forName("com.mysql.jdbc.Driver");
	    connection = DriverManager.getConnection("jdbc:mysql://localhost/jextract?user=danilofes&password=oefudfs2#");
	}
	
	public void read() throws Exception {
	      // statements allow to issue SQL queries to the database
	      statement = connection.createStatement();
	      // resultSet gets the result of the SQL query
	      resultSet = statement
	          .executeQuery("select * from FEEDBACK.COMMENTS");
	      //writeResultSet(resultSet);

	      // preparedStatements can use variables and are more efficient
	      preparedStatement = connection
	          .prepareStatement("insert into  FEEDBACK.COMMENTS values (default, ?, ?, ?, ? , ?, ?)");
	      // "myuser, webpage, datum, summary, COMMENTS from FEEDBACK.COMMENTS");
	      // parameters start with 1
	      preparedStatement.setString(1, "Test");
	      preparedStatement.setString(2, "TestEmail");
	      preparedStatement.setString(3, "TestWebpage");
	      preparedStatement.setDate(4, new java.sql.Date(2009, 12, 11));
	      preparedStatement.setString(5, "TestSummary");
	      preparedStatement.setString(6, "TestComment");
	      preparedStatement.executeUpdate();
	      //writeMetaData(resultSet);
	}
	
}
