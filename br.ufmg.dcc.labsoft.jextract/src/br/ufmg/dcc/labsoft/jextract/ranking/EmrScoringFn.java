package br.ufmg.dcc.labsoft.jextract.ranking;


public enum EmrScoringFn {
	JAC_T,
	JAC_V,
	JAC_M,
	JAC_TV,
	JAC_VM,
	JAC_TM,
	JAC_TVM,

	SOR_T,
	SOR_V,
	SOR_M,
	SOR_TV,
	SOR_VM,
	SOR_TM,
	SOR_TVM,

	OCH_T,
	OCH_V,
	OCH_M,
	OCH_TV,
	OCH_VM,
	OCH_TM,
	OCH_TVM,

	PSC_T,
	PSC_V,
	PSC_M,
	PSC_TV,
	PSC_VM,
	PSC_TM,
	PSC_TVM,

	KUL_T,
	KUL_V,
	KUL_M,
	KUL_TV,
	KUL_VM,
	KUL_TM,
	KUL_TVM,

	SCORE {
		@Override
		public double score(ExtractMethodRecomendation rec) {
			return rec.getScore();
		}
	},
	
	SS2_T,
	SS2_V,
	SS2_M,
	SS2_TV,
	SS2_VM,
	SS2_TM,
	SS2_TVM,
	
	P_KUL_TV,
	P_KUL_TVM,
	
	/*
	BAL_JAC_T,
	BAL_JAC_V,
	BAL_JAC_TV,
	BAL_JAC_TVM,
	BAL_KUL_T,
	BAL_KUL_TV,
	BAL_KUL_TVM,
	BAL_PSC_T,
	BAL_PSC_TV,
	BAL_PSC_TVM,
	P_KUL_T,
	P_KUL_V,
	P_KUL_M,
	P_KUL_TV,
	P_KUL_TVM,
	P_PSC_T,
	P_PSC_V,
	P_PSC_M,
	P_PSC_TV,
	P_PSC_TVM,
	P_JAC_T,
	P_JAC_V,
	P_JAC_M,
	P_JAC_TV,
	P_JAC_TVM,*/
	ID {
		@Override
		public double score(ExtractMethodRecomendation rec) {
			return -rec.id;
		}
	};

	public double score(ExtractMethodRecomendation rec) {
		String name = this.name();
		int splitIndex = name.lastIndexOf('_');
		String coef = name.substring(splitIndex - 3, splitIndex);
		String tvm = name.substring(splitIndex + 1);
		boolean p = name.startsWith("P_");
		boolean bal = name.startsWith("BAL_");
		boolean useVar = tvm.indexOf('V') != -1;
		boolean useType = tvm.indexOf('T') != -1;
		boolean useMod = tvm.indexOf('M') != -1;
		
		Coefficient coefficient = Coefficient.valueOf(coef);
		double r = 0.0;
		double denominator = 0.0;
		if (useVar) {
			double dist = rec.getSsimV().dist(coefficient);
			if (p) {
				dist *= (1.0 - rec.getPv());
			}
			r += dist;
			denominator += 1.0;
		}
		if (useType) {
			double dist = rec.getSsimT().dist(coefficient);
			if (p) {
				dist *= (1.0 - rec.getPt());
			}
			r += dist;
			denominator += 1.0;
		}
		if (useMod) {
			double dist = rec.getSsimM().dist(coefficient);
			if (p) {
				dist *= (1.0 - rec.getPm());
			}
			r += dist;
			denominator += 1.0;
		}
		r = r / denominator;
		if (bal) {
			double extracted = rec.getExtractedSize();
			double remaining = rec.getOriginalSize() - extracted;
			double balFactor = Math.min(extracted, remaining) / Math.max(extracted, remaining);
			r *= balFactor;
		}
		return r;
	}

}
