package br.ufmg.dcc.labsoft.jextract.ranking;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Statement;

public class StatementsCountVisitor extends ASTVisitor {
	
	private int count = 0;

	@Override
	public final void preVisit(ASTNode node) {
		if (node instanceof Statement && !(node instanceof Block)) {
			this.count++;
			onStatementVisit(node);
		}
	}

	protected void onStatementVisit(ASTNode node) {
		//
	}

	public int getCount() {
		return this.count;
	}
}
