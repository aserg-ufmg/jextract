package br.ufmg.dcc.labsoft.jextract.model;

import org.eclipse.jdt.core.dom.Statement;


public interface StatementModel extends HasEntityDependencies {

	BlockModel getParentBlock();

	int getIndexInBlock();

	int getTotalSize();

	Statement getAstNode();

	DependencyRelationship getRelationship(StatementModel other);
}
