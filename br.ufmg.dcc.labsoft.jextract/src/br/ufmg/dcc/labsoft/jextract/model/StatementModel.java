package br.ufmg.dcc.labsoft.jextract.model;

import org.eclipse.jdt.core.dom.Statement;


public interface StatementModel {

	int getIndexInBlock();

	int getSize();

	Statement getAstNode();

}
