package br.ufmg.dcc.labsoft.jextract.evaluation;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class Database {

	private Connection connection = null;

	public Database() {
		try {
			Class.forName("com.mysql.jdbc.Driver");
			this.connection = DriverManager.getConnection("jdbc:mysql://localhost/jextract?user=danilofes&password=oefudfs2#");
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void insertEmi(String confid, String project, String file, String method, String slice, int rank, double score, boolean match) {
		try {
			PreparedStatement preparedStatement = this.connection.prepareStatement(
					"INSERT INTO jextract.emi (emi_confid, emi_project, emi_file, emi_method, emi_rank, emi_slice, emi_score, emi_match) VALUES (?, ?, ?, ?, ?, ?, ?, ?)"
					);
			preparedStatement.setString(1, confid);
			preparedStatement.setString(2, project);
			preparedStatement.setString(3, file);
			preparedStatement.setString(4, method);
			preparedStatement.setInt(5, rank);
			preparedStatement.setString(6, slice);
			preparedStatement.setDouble(7, score);
			preparedStatement.setInt(8, match ? 1 : 0);
			preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
	}

	public void insertKnownEmi(String project, String file, String method, String slice, int size) {
		try {
			PreparedStatement preparedStatement = this.connection.prepareStatement(
					"INSERT INTO jextract.knownemi (kem_project, kem_file, kem_method, kem_slice, kem_methodsize) VALUES (?, ?, ?, ?, ?)"
					);
			preparedStatement.setString(1, project);
			preparedStatement.setString(2, file);
			preparedStatement.setString(3, method);
			preparedStatement.setString(4, slice);
			preparedStatement.setInt(5, size);
			preparedStatement.executeUpdate();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public void close() {
		if (this.connection != null) {
			try {
	            this.connection.close();
            } catch (SQLException e) {
	            throw new RuntimeException(e);
            }
		}
	}

}
