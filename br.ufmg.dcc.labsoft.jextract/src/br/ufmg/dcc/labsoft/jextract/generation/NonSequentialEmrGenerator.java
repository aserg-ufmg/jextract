package br.ufmg.dcc.labsoft.jextract.generation;

import java.util.BitSet;
import java.util.List;

import br.ufmg.dcc.labsoft.jextract.model.BlockModel;
import br.ufmg.dcc.labsoft.jextract.model.MethodModel;
import br.ufmg.dcc.labsoft.jextract.ranking.ExtractMethodRecomendation;

public class NonSequentialEmrGenerator extends SimpleEmrGenerator {

	public NonSequentialEmrGenerator(List<ExtractMethodRecomendation> recomendations, int minSize) {
		super(recomendations, minSize);
	}

	@Override
	protected void forEachSlice(MethodModel model, int minSize) {
		//int methodSize = model.getTotalSize();
		for (BlockModel block: model.getBlocks()) {
			StatementSelection selected = new StatementSelection(block);
			this.select(block, selected, 0);
		}
	}

	private void select(BlockModel block, StatementSelection selected, int i) {
	    if (i >= block.getChildren().size()) {
	    	this.handleSlice(block, selected);
	    } else {
	    	if (this.canSelect(block, selected, i, true)) {
	    		selected.select(i);
	    		this.select(block, selected, i + 1);
	    		selected.unselect(i);
	    	}
	    	if (this.canSelect(block, selected, i, false)) {
	    		this.select(block, selected, i + 1);
	    	}
	    }
    }

	private boolean canSelect(BlockModel block, StatementSelection selected, int i, boolean b) {
	    // TODO Auto-generated method stub
	    return false;
    }

	private void handleSlice(BlockModel block, StatementSelection selected) {
	    // TODO Auto-generated method stub
    }

	private static class StatementSelection {
		BitSet selected;
		BlockModel block;
		StatementSelection(BlockModel block) {
			this.block = block;
			this.selected = new BitSet(block.getChildren().size());
		}
		public void select(int index) {
			this.selected.set(index);
		}
		public void unselect(int index) {
			this.selected.clear(index);
		}
	}

}
