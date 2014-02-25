package br.ufmg.dcc.labsoft.jextract.model.impl;

import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Statement;

import br.ufmg.dcc.labsoft.jextract.model.BlockModel;
import br.ufmg.dcc.labsoft.jextract.model.DependencyRelationship;
import br.ufmg.dcc.labsoft.jextract.model.StatementModel;


class StatementImpl implements StatementModel {

	private int index = 0;
	private int indexInBlock = 0;
	private int selfSize = 0;
	private int childrenSize = 0;
	private boolean block = false;
	StatementImpl parent;
	MethodModelImpl methodModel;
	Statement astNode = null;
	BlockModel parentBlock = null;
	Pdg pdg = null;

	private static final StatementImpl NIL = new StatementImpl(){
		@Override
		void registerAsChild(StatementImpl child) {};
	};

	private StatementImpl() {
		this.index = -1;
		this.selfSize = 0;
		this.childrenSize = 0;
		this.parent = this;
	}

	StatementImpl(int index, Statement astNode, StatementImpl parent, boolean block, Pdg pdg) {
		this.index = index;
		this.selfSize = astNode instanceof Block ? 0 : 1;
		this.childrenSize = 0;
		this.parent = parent != null ? parent : NIL;
		this.parent.registerAsChild(this);
		this.block = block;
		this.astNode = astNode;
		this.pdg = pdg;
	}

	void registerAsChild(StatementImpl child) {
		this.childrenSize += child.getTotalSize();
		this.parent.registerAsChild(child);
	}

	@Override
	public int getIndexInBlock() {
		return this.indexInBlock;
	}

	void setIndexInBlock(int indexInBlock) {
		this.indexInBlock = indexInBlock;
	}

	public int getIndex() {
		return this.index;
	}

	@Override
	public int getTotalSize() {
		return this.selfSize + this.childrenSize;
	}

	@Override
	public BlockModel getParentBlock() {
		return this.parentBlock;
	}

	public boolean isBlock() {
		return this.block;
	}

	@Override
	public Statement getAstNode() {
		return this.astNode;
	}

	@Override
    public DependencyRelationship getRelationship(StatementModel other) {
	    return this.pdg.getRelationship(this, other);
    }

	public void setParentBlock(BlockModel parentBlock) {
		this.parentBlock = parentBlock;
	}
	
}
