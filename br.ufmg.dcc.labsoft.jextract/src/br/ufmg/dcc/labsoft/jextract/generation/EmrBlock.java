package br.ufmg.dcc.labsoft.jextract.generation;

import java.util.ArrayList;
import java.util.List;

public class EmrBlock {

	private EmrStatement blockStatement;
	private List<EmrStatement> children;
	
	public EmrBlock(EmrStatement blockStatement) {
		this.blockStatement = blockStatement;
		this.children = new ArrayList<EmrStatement>();
	}

	List<EmrStatement> getChildren() {
		return this.children;
	}

	public void appendStatement(EmrStatement statement) {
		this.children.add(statement);
	}

	public int getSize() {
		return this.blockStatement.getSize();
	}

}
