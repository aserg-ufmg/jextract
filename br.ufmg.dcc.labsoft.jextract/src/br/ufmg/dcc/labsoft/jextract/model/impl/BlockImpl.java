package br.ufmg.dcc.labsoft.jextract.model.impl;

import java.util.ArrayList;
import java.util.List;

import br.ufmg.dcc.labsoft.jextract.model.BlockModel;

class BlockImpl implements BlockModel {

	private StatementImpl blockStatement;
	private List<StatementImpl> children;

	public BlockImpl(StatementImpl blockStatement) {
		this.blockStatement = blockStatement;
		this.children = new ArrayList<StatementImpl>();
	}

	@Override
	public List<StatementImpl> getChildren() {
		return this.children;
	}

	@Override
	public StatementImpl get(int index) {
		return this.children.get(index);
	}

	public void appendStatement(StatementImpl statement) {
		statement.setIndexInBlock(this.children.size());
		statement.setParentBlock(this);
		this.children.add(statement);
	}

	@Override
	public int getTotalSize() {
		return this.blockStatement.getTotalSize();
	}

}
