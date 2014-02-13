package br.ufmg.dcc.labsoft.jextract.ranking;

import java.util.Comparator;

public class EmrComparator implements Comparator<ExtractMethodRecomendation> {

	private final EmrScoringFn scoringFn;
	private boolean groupByMethod;
	
	public EmrComparator(EmrScoringFn scoringFn, boolean groupByMethod) {
		this.scoringFn = scoringFn;
		this.groupByMethod = groupByMethod;
	}

	@Override
	public int compare(ExtractMethodRecomendation o1, ExtractMethodRecomendation o2) {
		if (this.groupByMethod) {
			if (o1.isAvailableInGoldSet() && !o2.isAvailableInGoldSet()) {
				return -1;
			}
			if (!o1.isAvailableInGoldSet() && o2.isAvailableInGoldSet()) {
				return 1;
			}
			int compC = o1.className.compareTo(o2.className);
			if (compC != 0) {
				return compC; 
			}
			int compM = o1.method.compareTo(o2.method);
			if (compM != 0) {
				return compM;
			}
		}
		int compR = - Double.compare(this.scoringFn.score(o1), this.scoringFn.score(o2));
		return compR == 0 ? o1.id - o2.id : compR; 
	}

}
