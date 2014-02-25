package br.ufmg.dcc.labsoft.jextract.model.impl;

import br.ufmg.dcc.labsoft.jextract.model.DependencyRelationship;
import br.ufmg.dcc.labsoft.jextract.model.StatementModel;

public class Pdg {

	public DependencyRelationship getRelationship(StatementModel s1, StatementModel s2) {
		// TODO
		if (s1.getParentBlock() != s2.getParentBlock()) {
			return DependencyRelationship.UNKNOWN;
		}
		if (s1.getIndexInBlock() > s2.getIndexInBlock()) {
			return DependencyRelationship.UNKNOWN;
		}
		return DependencyRelationship.NO_DEPENDENCY;
	}

}
