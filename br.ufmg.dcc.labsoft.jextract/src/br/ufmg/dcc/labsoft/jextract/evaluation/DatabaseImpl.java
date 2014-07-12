package br.ufmg.dcc.labsoft.jextract.evaluation;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

class DatabaseImpl extends Database {

	private Connection connection = null;
	private final String schema;

	public DatabaseImpl(String schema) {
		this.schema = schema;
		try {
			Class.forName("com.mysql.jdbc.Driver");
			this.connection = DriverManager.getConnection("jdbc:mysql://localhost/" + schema + "?user=danilofes&password=oefudfs2#");
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void insertEmi(String confid, String project, String file, String method, String slice, int rank, double score, boolean match, int diff) {
		try {
			PreparedStatement preparedStatement = this.connection.prepareStatement(
					"INSERT INTO " + schema + ".emi (emi_confid, emi_project, emi_file, emi_method, emi_rank, emi_slice, emi_score, emi_match, emi_diff) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)"
					);
			preparedStatement.setString(1, confid);
			preparedStatement.setString(2, project);
			preparedStatement.setString(3, file);
			preparedStatement.setString(4, method);
			preparedStatement.setInt(5, rank);
			preparedStatement.setString(6, slice);
			preparedStatement.setDouble(7, score);
			preparedStatement.setInt(8, match ? 1 : 0);
			preparedStatement.setInt(9, diff);
			preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
	}

	public void insertKnownEmi(String project, String file, String method, String slice, int size, Boolean sameClass, int extractedSize) {
		try {
			PreparedStatement preparedStatement = this.connection.prepareStatement(
					"INSERT INTO " + schema + ".knownemi (kem_project, kem_file, kem_method, kem_slice, kem_methodsize, kem_sameclass, kem_extractedsize) VALUES (?, ?, ?, ?, ?, ?, ?)"
					);
			preparedStatement.setString(1, project);
			preparedStatement.setString(2, file);
			preparedStatement.setString(3, method);
			preparedStatement.setString(4, slice);
			preparedStatement.setInt(5, size);
			preparedStatement.setInt(6, Boolean.TRUE.equals(sameClass) ? 1 : 0);
			preparedStatement.setInt(7, extractedSize);
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
