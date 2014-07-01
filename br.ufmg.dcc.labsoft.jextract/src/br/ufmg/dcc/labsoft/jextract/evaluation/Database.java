package br.ufmg.dcc.labsoft.jextract.evaluation;


public abstract class Database {

	public abstract void insertEmi(String confid, String project, String file, String method, String slice, int rank, double score, boolean match);

	public abstract void insertKnownEmi(String project, String file, String method, String slice, int size, Boolean sameClass, int extractedSize);

	public abstract void close();

}
