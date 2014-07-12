package br.ufmg.dcc.labsoft.jextract.evaluation;


public abstract class Database {

	public static Database getInstance() {
		//return new FakeDatabase();
		//return new DatabaseImpl("qualitas2");
		return new DatabaseImpl("qualitas3");
		//return new DatabaseImpl("jdeo");
	}

	public abstract void insertEmi(String confid, String project, String file, String method, String slice, int rank, double score, boolean match, int diff);

	public abstract void insertKnownEmi(String project, String file, String method, String slice, int size, Boolean sameClass, int extractedSize);

	public abstract void close();

}
