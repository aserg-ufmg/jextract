package br.ufmg.dcc.labsoft.jextract.generation;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import org.eclipse.jdt.core.dom.Statement;

import br.ufmg.dcc.labsoft.jextract.model.BlockModel;
import br.ufmg.dcc.labsoft.jextract.model.MethodModel;
import br.ufmg.dcc.labsoft.jextract.model.StatementModel;
import br.ufmg.dcc.labsoft.jextract.ranking.ExtractMethodRecomendation;
import br.ufmg.dcc.labsoft.jextract.ranking.ExtractionSlice.Fragment;

public class NonSequentialEmrGenerator extends SimpleEmrGenerator {

	private final int maxFragments = 2;

	public NonSequentialEmrGenerator(List<ExtractMethodRecomendation> recomendations, int minSize) {
		super(recomendations, minSize);
	}

	@Override
	protected void forEachSlice(MethodModel model) {
		//int methodSize = model.getTotalSize();
		for (BlockModel block: model.getBlocks()) {
			StatementSelection selected = new StatementSelection(block);
			this.select(model, block, selected, 0, false, 0);
		}
	}

	private void select(MethodModel model, BlockModel block, StatementSelection selected, int i, boolean lastSelected, int fragments) {
	    if (i >= block.getChildren().size()) {
	    	if (selected.getTotalSize() >= this.minSize) {
	    		this.handleSlice(model, block, selected);
	    	}
	    } else {
	    	if (this.canSelect(model, block, selected, i, true, true, lastSelected ? fragments : fragments + 1)) {
	    		selected.select(i);
	    		this.select(model, block, selected, i + 1, true, lastSelected ? fragments : fragments + 1);
	    		selected.unselect(i);
	    	}
	    	if (this.canSelect(model, block, selected, i, false, false, fragments)) {
	    		this.select(model, block, selected, i + 1, false, fragments);
	    	}
	    }
    }

	private boolean canSelect(MethodModel model, BlockModel block, StatementSelection selected, int i, boolean b, boolean lastSelected, int fragments) {
		if (b) {
			if (fragments > this.maxFragments) {
				return false;
			}
			int newSize = selected.getTotalSize() + block.get(i).getTotalSize();
			if ((model.getTotalSize() - newSize) < this.minSize) {
				return false;
			}
		}
		// TODO
	    return true;
    }

	private void handleSlice(MethodModel model, BlockModel block, StatementSelection selected) {
		List<? extends StatementModel> children = block.getChildren();
		List<Fragment> frags = new ArrayList<Fragment>();
		int length = children.size();
		int totalSize = 0;
		for (int i = 0, j = 0; i < length; i = j) {
			while (i < length && !selected.isSelected(i)) {
				i++;
				j++;
			}
			while (j < length && selected.isSelected(j)) {
				totalSize += children.get(j).getTotalSize();
				j++;
			}
			if (i < length) {
				Statement s1 = children.get(i).getAstNode();
				Statement s2 = children.get(j - 1).getAstNode();
				frags.add(new Fragment(s1.getStartPosition(), s2.getStartPosition() + s2.getLength(), false));
			}
		}
		
		if (!frags.isEmpty()) {
			Fragment[] fragmentsArray = frags.toArray(new Fragment[frags.size()]);
			this.addRecomendation(model, totalSize, fragmentsArray);
		}
    }
	
	private static class StatementSelection {
		BitSet selected;
		int totalSize;
		private BlockModel block;
		StatementSelection(BlockModel block) {
			this.block = block;
			this.selected = new BitSet(block.getChildren().size());
			this.totalSize = 0;
		}
		public void select(int index) {
			this.selected.set(index);
			this.totalSize += this.block.get(index).getTotalSize();
		}
		public void unselect(int index) {
			this.selected.clear(index);
			this.totalSize -= this.block.get(index).getTotalSize();
		}
		public boolean isSelected(int index) {
			return this.selected.get(index);
		}
		public int getTotalSize() {
			return this.totalSize;
		}
	}

}
