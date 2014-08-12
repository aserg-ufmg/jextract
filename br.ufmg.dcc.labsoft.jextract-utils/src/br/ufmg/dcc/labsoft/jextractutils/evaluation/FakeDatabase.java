package br.ufmg.dcc.labsoft.jextractutils.evaluation;


class FakeDatabase extends Database {

	@Override
	public void insertEmi(String confid, String project, String file, String method, String slice, int rank, double score, boolean match, int diff) {
	}

	@Override
	public void insertKnownEmi(String project, String file, String method, String slice, int size, Boolean sameClass, int extractedSize) {
	}

	@Override
	public void close() {
	}

}
