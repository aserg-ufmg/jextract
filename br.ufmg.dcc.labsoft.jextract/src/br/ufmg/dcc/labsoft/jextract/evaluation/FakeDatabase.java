package br.ufmg.dcc.labsoft.jextract.evaluation;


public class FakeDatabase extends Database {

	@Override
	public void insertEmi(String confid, String project, String file, String method, String slice, int rank, double score, boolean match) {
	}

	@Override
	public void insertKnownEmi(String project, String file, String method, String slice, int size, Boolean sameClass, int extractedSize) {
	}

	@Override
	public void close() {
	}

}
