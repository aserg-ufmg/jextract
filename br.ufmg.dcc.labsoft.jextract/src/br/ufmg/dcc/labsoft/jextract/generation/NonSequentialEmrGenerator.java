package br.ufmg.dcc.labsoft.jextract.generation;

import java.util.ArrayList;
import java.util.Arrays;
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

	private int recursiveCalls = 0;
	private MethodModel model;
	
	private BlockModel block;
	private StatementSelection selected;
	
	public NonSequentialEmrGenerator(List<ExtractMethodRecomendation> recomendations, Settings settings) {
		super(recomendations, settings);
	}

	@Override
	protected void forEachSlice(MethodModel m) {
		this.recursiveCalls = 0;
		this.model = m;
		for (BlockModel b: m.getBlocks()) {
			this.block = b;
			this.selected = new StatementSelection(b);
			this.init(0);
		}
		//System.out.println("cost: " + this.recursiveCalls);
	}

	private void init(int i) {
		if (!this.checkBounds(i)) {
			return;
		}
		
		init(i + 1);
		extract(i, 1);
    }
	
	private void extract(int i, int fragments) {
		if (!this.checkBounds(i)) {
			return;
		}
		int newSize = this.selected.getTotalSize() + this.block.get(i).getTotalSize();
		if ((this.model.getTotalSize() - newSize) < this.settings.getMinSize()) {
			return;
		}
		if (!this.canBePlaced(i, Placement.INSIDE)) {
			return;
		}
		this.selected.set(i, Placement.INSIDE);
		extract(i + 1, fragments);
		end();
		skip(i + 1, fragments + 1);
		this.selected.set(i, Placement.UNASSIGNED);
	}
	
	private void skip(int i, int fragments) {
		if (!this.checkBounds(i) || fragments > this.settings.getMaxFragments()) {
			return;
		}
		if (this.canBePlaced(i, Placement.BEFORE)) {
			this.selected.set(i, Placement.BEFORE);
		} else if (this.canBePlaced(i, Placement.AFTER)) {
			this.selected.set(i, Placement.AFTER);
		} else {
			return;
		}
		
		skip(i + 1, fragments);
		extract(i + 1, fragments);
		this.selected.set(i, Placement.UNASSIGNED);
	}
	
	private void end() {
		if (selected.getTotalSize() < this.settings.getMinSize()) {
			return;
    	}
		this.handleSlice(model, block, selected);
	}

	private boolean checkBounds(int i) {
		this.recursiveCalls++;
		if (i >= block.getChildren().size()) {
			return false;
		}
		return true;
	}

	private boolean canBePlaced(int i, Placement placement) {
		for (int j = i - 1; j >= 0; j--) {
			// If an statement will be placed before some of its depencies, fail
			boolean iIsBeforeJ = this.selected.get(j).compareTo(placement) > 0;
			boolean iDependsOnJ = this.block.depends(i, j);
			if (iIsBeforeJ && iDependsOnJ) {
				return false;
			}
		}
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

	static enum Placement {
		UNASSIGNED,
		BEFORE,
		INSIDE,
		AFTER
	}

	private static class StatementSelection {
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
		double score = (distP + distT + distV) / 3.0;
		//double score = (distP * meanP + distT * meanT + distV * meanV) / 3.0;
		
		if (slice.isComposed()) {
			// Penalty for unsafe recommendation
			score = score - (score * this.settings.getPenalty());
		}
		
		rec.setScore(score);
		rec.setExplanation(String.format("P = %.2f, T = %.2f, V = %.2f", distP, distT, distV));
		//rec.setExplanation(String.format("P = %.2f * %.2f, T = %.2f * %.2f, V = %.2f * %.2f", distP, meanP, distT, meanT, distV, meanV));
	}

	private double dist(EntitySet entitiesT1, EntitySet entitiesT2) {
		return new SetsSimilarity(entitiesT1, entitiesT2).end().dist(Coefficient.KUL);
	}

	private double sim(EntitySet entitiesT1, EntitySet entitiesT2) {
		return new SetsSimilarity(entitiesT1, entitiesT2).end().sim(Coefficient.KUL);
	}
}
