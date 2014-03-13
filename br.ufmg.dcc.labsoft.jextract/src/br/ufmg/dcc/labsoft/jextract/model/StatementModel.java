package br.ufmg.dcc.labsoft.jextract.model;

import java.util.List;

import org.eclipse.jdt.core.dom.Statement;


public interface StatementModel extends HasEntityDependencies {

	BlockModel getParentBlock();

	int getIndexInBlock();

	int getTotalSize();

	Statement getAstNode();

	List<? extends StatementModel> getDescendents();
}
