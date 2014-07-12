package br.ufmg.dcc.labsoft.jextract.ranking;

import org.eclipse.jdt.core.dom.ASTNode;

public class StatementsSliceDiffCountVisitor extends StatementsCountVisitor {

	private int diffCount = 0;
	private final ExtractionSlice slice1;
	private final ExtractionSlice slice2;

	public StatementsSliceDiffCountVisitor(ExtractionSlice slice1, ExtractionSlice slice2) {
		super();
		this.slice1 = slice1;
		this.slice2 = slice2;
	}

	@Override
	protected void onStatementVisit(ASTNode node) {
		int sp = node.getStartPosition();
		boolean isDiff = 
				(slice1.belongsToExtracted(sp) && !slice2.belongsToExtracted(sp)) ||
				(!slice1.belongsToExtracted(sp) && slice2.belongsToExtracted(sp));
		if (isDiff) {
			this.diffCount++;
		}
	}

	public int getDiffCount() {
		return diffCount;
	}

}
