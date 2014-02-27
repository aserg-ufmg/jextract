package br.ufmg.dcc.labsoft.jextract.generation;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.Statement;

import br.ufmg.dcc.labsoft.jextract.model.BlockModel;
import br.ufmg.dcc.labsoft.jextract.model.EntitySet;
import br.ufmg.dcc.labsoft.jextract.model.MethodModel;
import br.ufmg.dcc.labsoft.jextract.model.StatementModel;
import br.ufmg.dcc.labsoft.jextract.ranking.Coefficient;
import br.ufmg.dcc.labsoft.jextract.ranking.DependenciesAstVisitor;
import br.ufmg.dcc.labsoft.jextract.ranking.ExtractMethodRecomendation;
import br.ufmg.dcc.labsoft.jextract.ranking.ExtractionSlice;
import br.ufmg.dcc.labsoft.jextract.ranking.ExtractionSlice.Fragment;
import br.ufmg.dcc.labsoft.jextract.ranking.SetsSimilarity;

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
			double score = this.computeScore(model, block, selected, totalSize, fragmentsArray);
			this.addRecomendation(model, totalSize, score, fragmentsArray);
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

	protected double computeScore(MethodModel model, BlockModel block, StatementSelection selected, int totalSize, Fragment[] fragmentsArray) {
		final ExtractionSlice slice = new ExtractionSlice(fragmentsArray);
		final SetsSimilarity ssim = new SetsSimilarity();
		
		final EntitySet entitiesP1 = new EntitySet();
		final EntitySet entitiesP2 = new EntitySet();
		final EntitySet entitiesT1 = new EntitySet();
		final EntitySet entitiesT2 = new EntitySet();
		final EntitySet entitiesV1 = new EntitySet();
		final EntitySet entitiesV2 = new EntitySet();
		
		model.getAstNode().accept(new DependenciesAstVisitor(model.getAstNode().resolveBinding().getDeclaringClass()) {
			@Override
			public void onModuleAccess(ASTNode node, String packageName) {
				if (slice.belongsToMethod(node.getStartPosition())) {
					entitiesP1.add(packageName);
				}
				if (slice.belongsToExtracted(node.getStartPosition())) {
					entitiesP2.add(packageName);
				}
			}
			@Override
			public void onTypeAccess(ASTNode node, ITypeBinding binding) {
				if (slice.belongsToMethod(node.getStartPosition())) {
					entitiesT1.add(binding.getKey());
				}
				if (slice.belongsToExtracted(node.getStartPosition())) {
					entitiesT2.add(binding.getKey());
				}
			}
			@Override
			public void onVariableAccess(ASTNode node, IVariableBinding binding) {
				if (slice.belongsToMethod(node.getStartPosition())) {
					entitiesV1.add(binding.getKey());
				}
				if (slice.belongsToExtracted(node.getStartPosition())) {
					entitiesV2.add(binding.getKey());
				}
			}
		});
		ssim.end();
		
		double distance = (this.dist(entitiesP1, entitiesP2) + this.dist(entitiesT1, entitiesT2) + this.dist(entitiesV1, entitiesV2)) / 3.0;

		List<? extends StatementModel> children = block.getChildren();
		double meanSim = 0.0;
		int count = 0;
		for (int i = 0; i < children.size(); i++) {
			if (selected.isSelected(i)) {
				StatementModel s = children.get(i);
				meanSim += this.sim(s.getEntitiesP(), entitiesP2) + this.sim(s.getEntitiesT(), entitiesT2) + this.sim(s.getEntitiesV(), entitiesV2);
				count++;
			}
		}
		if (meanSim > 0.0) {
			meanSim = meanSim / count;
		}
		
		return distance * meanSim;
	}

	private double dist(EntitySet entitiesT1, EntitySet entitiesT2) {
		return new SetsSimilarity(entitiesT1, entitiesT2).end().dist(Coefficient.KUL);
	}

	private double sim(EntitySet entitiesT1, EntitySet entitiesT2) {
		return new SetsSimilarity(entitiesT1, entitiesT2).end().sim(Coefficient.KUL);
	}
}
