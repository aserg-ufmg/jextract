package br.ufmg.dcc.labsoft.jextract.generation;

import java.util.Arrays;

import br.ufmg.dcc.labsoft.jextract.model.BlockModel;

public class StatementSelection {
	Placement[] selected;
	int totalSize;
	private BlockModel block;

	StatementSelection(BlockModel block) {
		this.block = block;
		this.selected = new Placement[block.getChildren().size()];
		Arrays.fill(this.selected, Placement.UNASSIGNED);
		this.totalSize = 0;
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
		sb.append("B" + this.block.getIndex() + ": [");
		for (Placement p : this.selected) {
			switch (p) {
			case BEFORE:
			case UNASSIGNED:
				sb.append("-");
				break;
			case MOVED_BEFORE:
				sb.append("\u2191");
				break;
			case INSIDE:
				sb.append("E");
				break;
			case MOVED_AFTER:
				sb.append("\u2193");
				break;
			}
		}
		sb.append("]");
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

}