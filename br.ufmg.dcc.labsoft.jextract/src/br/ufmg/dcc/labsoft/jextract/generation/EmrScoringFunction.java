package br.ufmg.dcc.labsoft.jextract.generation;

import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;

import br.ufmg.dcc.labsoft.jextract.model.BlockModel;
import br.ufmg.dcc.labsoft.jextract.model.EntitySet;
import br.ufmg.dcc.labsoft.jextract.model.MethodModel;
import br.ufmg.dcc.labsoft.jextract.model.StatementModel;
import br.ufmg.dcc.labsoft.jextract.ranking.Coefficient;
import br.ufmg.dcc.labsoft.jextract.ranking.DependenciesAstVisitor;
import br.ufmg.dcc.labsoft.jextract.ranking.ExtractionSlice;
import br.ufmg.dcc.labsoft.jextract.ranking.SetsSimilarity;

public class EmrScoringFunction {

	protected final Settings settings;

	public EmrScoringFunction(Settings settings) {
		this.settings = settings;
	}

	public ScoreResult computeScore(final ExtractionSlice slice, StatementSelection selected) {
		final MethodModel model = selected.getMethodModel();
		final BlockModel block = selected.getBlock();
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
		
		String safenessExplanation = "";
		if (slice.isComposed()) {
			// Penalty for unsafe recommendation
			double errorProbability = this.settings.getPenalty();
			double safeness = Math.pow(1.0 - errorProbability, selected.getReorderedStatements());
			score = score * safeness;
			safenessExplanation = String.format("safeness = %.2f", safeness);
		}
		
		return new ScoreResult(score, String.format("P = %.2f, T = %.2f, V = %.2f %s", distP, distT, distV, safenessExplanation));
	}

	private double dist(EntitySet entitiesT1, EntitySet entitiesT2) {
		return new SetsSimilarity(entitiesT1, entitiesT2).end().dist(Coefficient.KUL);
	}

	private double sim(EntitySet entitiesT1, EntitySet entitiesT2) {
		return new SetsSimilarity(entitiesT1, entitiesT2).end().sim(Coefficient.KUL);
	}
}
