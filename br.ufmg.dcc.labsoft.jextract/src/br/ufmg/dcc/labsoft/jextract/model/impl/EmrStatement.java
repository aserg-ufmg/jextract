package br.ufmg.dcc.labsoft.jextract.model.impl;

import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Statement;

import br.ufmg.dcc.labsoft.jextract.model.StatementModel;


class EmrStatement implements StatementModel {

	private int index = 0;
	private int indexInBlock = 0;
	private int selfSize = 0;
	private int childrenSize = 0;
	private boolean block = false;
	EmrStatement parent;
	Statement astNode = null;

	private static final EmrStatement NIL = new EmrStatement(){
		@Override
		void registerAsChild(EmrStatement child) {};
	};

	private EmrStatement() {
		this.index = -1;
		this.selfSize = 0;
		this.childrenSize = 0;
		this.parent = this;
	}

	EmrStatement(int index, Statement astNode, EmrStatement parent, boolean block) {
		this.index = index;
		this.selfSize = astNode instanceof Block ? 0 : 1;
		this.childrenSize = 0;
		this.parent = parent != null ? parent : NIL;
		this.parent.registerAsChild(this);
		this.block = block;
		this.astNode = astNode;
	}

	void registerAsChild(EmrStatement child) {
		this.childrenSize += child.getSize();
		this.parent.registerAsChild(child);
	}

	public int getIndexInBlock() {
		return this.indexInBlock;
	}

	void setIndexInBlock(int indexInBlock) {
		this.indexInBlock = indexInBlock;
	}

	public int getIndex() {
		return this.index;
	}

	public int getSize() {
		return this.selfSize + this.childrenSize;
	}

	public boolean isBlock() {
		return this.block;
	}

	public Statement getAstNode() {
		return this.astNode;
	}

}
