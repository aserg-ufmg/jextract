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

	private final int maxFragments = 1;

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
			ExtractMethodRecomendation rec = this.addRecomendation(model, totalSize, fragmentsArray);
			this.computeAndSetScore(rec, model, block, selected);
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

	protected void computeAndSetScore(ExtractMethodRecomendation rec, MethodModel model, BlockModel block, StatementSelection selected) {
		final ExtractionSlice slice = rec.getSlice();
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
		
		double distP = this.dist(entitiesP1, entitiesP2);
		double distT = this.dist(entitiesT1, entitiesT2);
		double distV = this.dist(entitiesV1, entitiesV2);
		//double distance = (distP + distT + distV) / 3.0;

		List<? extends StatementModel> children = block.getChildren();
		double meanP = 0.0;
		double meanT = 0.0;
		double meanV = 0.0;
		int count = 0;
		for (int i = 0; i < children.size(); i++) {
			if (selected.isSelected(i)) {
				StatementModel s = children.get(i);
				meanP += this.sim(s.getEntitiesP(), entitiesP2);
				meanT += this.sim(s.getEntitiesT(), entitiesT2);
				meanV += this.sim(s.getEntitiesV(), entitiesV2);
				count++;
				for (StatementModel ds : s.getDescendents()) {
					meanP += this.sim(ds.getEntitiesP(), entitiesP2);
					meanT += this.sim(ds.getEntitiesT(), entitiesT2);
					meanV += this.sim(ds.getEntitiesV(), entitiesV2);
					count++;
				}
			}
		}
		if (count > 0) {
			meanP = meanP / count;
			meanT = meanT / count;
			meanV = meanV / count;
		}
		//double mean = (meanP + meanT + meanV) / 3.0;
		double score = (distP * meanP + distT * meanT + distV * meanV) / 3.0;
		
		rec.setScore(score);
		rec.setExplanation(String.format("P = %.2f * %.2f, T = %.2f * %.2f, V = %.2f * %.2f", distP, meanP, distT, meanT, distV, meanV));
	}

	private double dist(EntitySet entitiesT1, EntitySet entitiesT2) {
		return new SetsSimilarity(entitiesT1, entitiesT2).end().dist(Coefficient.KUL);
	}

	private double sim(EntitySet entitiesT1, EntitySet entitiesT2) {
		return new SetsSimilarity(entitiesT1, entitiesT2).end().sim(Coefficient.KUL);
	}
}
