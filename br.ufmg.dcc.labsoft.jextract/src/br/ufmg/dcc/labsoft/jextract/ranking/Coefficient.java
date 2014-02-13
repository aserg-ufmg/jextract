package br.ufmg.dcc.labsoft.jextract.ranking;

public enum Coefficient {

	// Jaccard
	JAC {
		@Override
		public double formula(double a, double b, double c) {
			return a / (a + b + c);
		}
	},
	// Sorenson
	SOR {
		@Override
		public double formula(double a, double b, double c) {
			return 2 * a / (2 * a + b + c);
		}
	},
	// Ochiai
	OCH {
		@Override
		public double formula(double a, double b, double c) {
			return a / Math.sqrt(((a + b) * (a + c)));
		}
	},
	// PSC
	PSC {
		@Override
		public double formula(double a, double b, double c) {
			return (a * a) / ((a + b) * (a + c));
		}
	},
	// Kulczynski
	KUL {
		@Override
		public double formula(double a, double b, double c) {
			return 0.5 * ((a / (a + b)) + (a / (a + c)));
		}
	},
	// Sokal and Sneath 2
	SS2 {
		@Override
		public double formula(double a, double b, double c) {
			return a / (a + 2 * (b + c));
		}
	};

	public abstract double formula(double a, double b, double c);

}
