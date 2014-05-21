package br.ufmg.dcc.labsoft.jextract.generation;

import java.util.Arrays;

import org.eclipse.jdt.core.dom.ASTVisitor;

import br.ufmg.dcc.labsoft.jextract.model.BlockModel;
import br.ufmg.dcc.labsoft.jextract.model.MethodModel;
import br.ufmg.dcc.labsoft.jextract.model.Placement;

public class StatementSelection {
	private final MethodModel methodModel;
	private final BlockModel block;
	final Placement[] selected;
	int totalSize;

	StatementSelection(MethodModel methodModel, BlockModel block) {
		this.methodModel = methodModel;
		this.block = block;
		this.selected = new Placement[block.getChildren().size()];
		Arrays.fill(this.selected, Placement.UNASSIGNED);
		this.totalSize = 0;
	}

	public MethodModel getMethodModel() {
		return this.methodModel;
	}

	public BlockModel getBlock() {
		return this.block;
	}

	public void set(int index, Placement placement) {
		if (Placement.INSIDE.equals(placement)) {
			this.selected[index] = placement;
			this.totalSize += this.block.get(index).getTotalSize();
		} else if (this.isSelected(index)) {
			this.selected[index] = placement;
			this.totalSize -= this.block.get(index).getTotalSize();
		} else {
			this.selected[index] = placement;
		}
	}

	public Placement get(int index) {
		return this.selected[index];
	}

	public boolean isSelected(int index) {
		return this.get(index).equals(Placement.INSIDE);
	}

	public int getTotalSize() {
		return this.totalSize;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("B" + this.block.getIndex() + "{");
		for (Placement p : this.selected) {
			sb.append(p.getCharacter());
		}
		sb.append("}");
		return sb.toString();
	}

	public int getReorderedStatements() {
		int count = 0;
		for (int i = 0; i < this.selected.length; i++) {
			Placement p = this.selected[i];
			if (p.requiresReordering()) {
				count += this.block.getChildren().get(i).getTotalSize();
			}
		}
		return count;
	}

	public void acceptFilterBySelected(ASTVisitor visitor) {
		
	}

	public void acceptFilterBySelected(boolean selected, ASTVisitor visitor) {
		this.methodModel.getAstNode().accept(new ASTVisitor() {
			//pre
		});
	}

}