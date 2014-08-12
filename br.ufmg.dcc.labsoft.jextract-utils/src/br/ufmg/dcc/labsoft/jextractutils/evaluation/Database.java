package br.ufmg.dcc.labsoft.jextractutils.evaluation;

import java.sql.SQLException;


public abstract class Database {

	public static Database getInstance() {
		//return new FakeDatabase();
		try {
			//return new DatabaseImpl("qualitas2");
			//return new DatabaseImpl("jdeo");
			return new DatabaseImpl("qualitas3");
		} catch (SQLException e) {
			System.out.println("Could not connect to mysql database: " + e.getMessage());
			return new FakeDatabase();
		}
	}

	public abstract void insertEmi(String confid, String project, String file, String method, String slice, int rank, double score, boolean match, int diff);

	public abstract void insertKnownEmi(String project, String file, String method, String slice, int size, Boolean sameClass, int extractedSize);

	public abstract void close();

}
