package br.ufmg.dcc.labsoft.jextract.model.impl;

import java.util.ArrayList;
import java.util.List;

import br.ufmg.dcc.labsoft.jextract.model.BlockModel;
import br.ufmg.dcc.labsoft.jextract.model.StatementModel;

class BlockImpl implements BlockModel {

	private int index;
	private StatementImpl blockStatement;
	private List<StatementImpl> children;

	public BlockImpl(int index, StatementImpl blockStatement) {
		this.index = index;
		this.blockStatement = blockStatement;
		this.children = new ArrayList<StatementImpl>();
	}

	@Override
	public int getIndex() {
		return this.index;
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

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("B" + this.index + ":");
		for (StatementModel s : this.getChildren()) {
			sb.append("\n" + s.toString());
		}
	    return sb.toString();
	}

}
