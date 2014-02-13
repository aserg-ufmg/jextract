package br.ufmg.dcc.labsoft.jextract.ranking;

import org.eclipse.jdt.core.dom.ASTNode;

public class StatementsSliceCountVisitor extends StatementsCountVisitor {

	private int extractedCount = 0;
	private int duplicatedCount = 0;
	private final ExtractionSlice slice;

	public StatementsSliceCountVisitor(ExtractionSlice slice) {
		super();
		this.slice = slice;
	}

	@Override
	protected void onStatementVisit(ASTNode node) {
		if (slice.belongsToExtracted(node.getStartPosition())) {
			this.extractedCount++;
			if (slice.belongsToMethod(node.getStartPosition())) {
				this.duplicatedCount++;
			}
		}
	}

	public int getExtractedCount() {
		return extractedCount;
	}

	public int getDuplicatedCount() {
		return duplicatedCount;
	}
	
}
