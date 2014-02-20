package br.ufmg.dcc.labsoft.jextract.model.impl;

import java.util.ArrayList;
import java.util.List;

import br.ufmg.dcc.labsoft.jextract.model.BlockModel;

class EmrBlock implements BlockModel {

	private EmrStatement blockStatement;
	private List<EmrStatement> children;

	public EmrBlock(EmrStatement blockStatement) {
		this.blockStatement = blockStatement;
		this.children = new ArrayList<EmrStatement>();
	}

	public List<EmrStatement> getChildren() {
		return this.children;
	}

	public EmrStatement get(int index) {
		return this.children.get(index);
	}

	public void appendStatement(EmrStatement statement) {
		statement.setIndexInBlock(this.children.size());
		this.children.add(statement);
	}

	public int getSize() {
		return this.blockStatement.getSize();
	}

}
