package br.ufmg.dcc.labsoft.jextract.generation;

import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import br.ufmg.dcc.labsoft.jextract.model.BlockModel;
import br.ufmg.dcc.labsoft.jextract.model.EntitySet;
import br.ufmg.dcc.labsoft.jextract.model.MethodModel;
import br.ufmg.dcc.labsoft.jextract.model.StatementModel;
import br.ufmg.dcc.labsoft.jextract.ranking.Coefficient;
import br.ufmg.dcc.labsoft.jextract.ranking.DependenciesAstVisitor;
import br.ufmg.dcc.labsoft.jextract.ranking.ExtractMethodRecomendation;
import br.ufmg.dcc.labsoft.jextract.ranking.ExtractionSlice;
import br.ufmg.dcc.labsoft.jextract.ranking.Utils;

public class EmrScoringFunction {

	protected final Settings settings;
	
	private Coefficient coefficient = Coefficient.KUL;

	private boolean useProbabilityFactor = false;
	private boolean includeMethodCalls = false;
	private boolean zeroScoreOnEmptySets = false;

	public EmrScoringFunction() {
		this.settings = new Settings();
	}

	public static EmrScoringFunction getInstance(Settings settings) {
		return new EmrScoringFunction(settings);
	}
	
	private EmrScoringFunction(Settings settings) {
		this.settings = settings;
	}

	public ScoreResult computeScore(ExtractMethodRecomendation rec, StatementSelection selected) {
		final ExtractionSlice slice = rec.getExtractionSlice();
		final MethodModel model = selected.getMethodModel();
		final BlockModel block = selected.getBlock();
		
		MethodDeclaration methodDeclaration = model.getAstNode();
		final EntitySet entitiesP1 = new EntitySet();
		final EntitySet entitiesP2 = new EntitySet();
		final EntitySet entitiesT1 = new EntitySet();
		final EntitySet entitiesT2 = new EntitySet();
		final EntitySet entitiesV1 = new EntitySet();
		final EntitySet entitiesV2 = new EntitySet();
		fillEntitySets(slice, entitiesP1, entitiesP2, entitiesT1, entitiesT2, entitiesV1, entitiesV2, methodDeclaration);
		
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
		
		double score;
		String explain;
		if (this.useProbabilityFactor) {
			double probP = 1.0 - this.probability(rec, entitiesP1, entitiesP2);
			double probT = 1.0 - this.probability(rec, entitiesT1, entitiesT2);
			double probV = 1.0 - this.probability(rec, entitiesV1, entitiesV2);
			score = (distP * probP + distT * probT + distV * probV) / 3.0;
			explain = String.format("P = %.3f x %.3f, T = %.3f x %.3f, V = %.3f x %.3f", distP, probP, distT, probT, distV, probV);
		} else {
			score = (distP + distT + distV) / 3.0;
			//double score = (distP * meanP + distT * meanT + distV * meanV) / 3.0;
			explain = String.format("P = %.3f, T = %.3f, V = %.3f", distP, distT, distV);
		}
		
		String safenessExplanation = "";
		if (slice.isComposed()) {
			// Penalty for unsafe recommendation
			double errorProbability = this.settings.getPenalty();
			double safeness = Math.pow(1.0 - errorProbability, selected.getReorderedStatements());
			score = score * safeness;
			safenessExplanation = String.format(" safeness = %.3f", safeness);
		}
		
		if (this.zeroScoreOnEmptySets) {
			boolean emptyP = this.isExtractionEmpty(entitiesP1, entitiesP2);
			boolean emptyT = this.isExtractionEmpty(entitiesT1, entitiesT2);
			boolean emptyV = this.isExtractionEmpty(entitiesV1, entitiesV2);
			if (emptyP && emptyT && emptyV) {
				return new ScoreResult(0.0, "empty extracted entities set");
			}
		}
		return new ScoreResult(score, explain + safenessExplanation);
	}

	private void fillEntitySets(final ExtractionSlice slice, final EntitySet entitiesP1, final EntitySet entitiesP2,
            final EntitySet entitiesT1, final EntitySet entitiesT2, final EntitySet entitiesV1,
            final EntitySet entitiesV2, MethodDeclaration methodDeclaration) {
	    ITypeBinding declaringClass = methodDeclaration.resolveBinding().getDeclaringClass();
		methodDeclaration.accept(new DependenciesAstVisitor(declaringClass) {
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
			@Override
			public void onMethodAccess(ASTNode node, IMethodBinding binding) {
				if (includeMethodCalls) {
					if (slice.belongsToMethod(node.getStartPosition())) {
						entitiesV1.add(binding.getKey());
					}
					if (slice.belongsToExtracted(node.getStartPosition())) {
						entitiesV2.add(binding.getKey());
					}
				}
			}
			
		});
    }

	private double dist(EntitySet entitiesT1, EntitySet entitiesT2) {
		return 1.0 - this.sim(entitiesT1, entitiesT2);
	}

	private double sim(EntitySet entitiesT1, EntitySet entitiesT2) {
		EntitySet intersection = entitiesT1.intersection(entitiesT2);
		double a = intersection.size();
		double b = entitiesT1.size() - a;
		double c = entitiesT2.size() - a;
		if (a == 0) {
			return 0.0;
		}
		return this.coefficient.formula(a, b, c);
	}

	private boolean isExtractionEmpty(EntitySet entitiesT1, EntitySet entitiesT2) {
		EntitySet intersection = entitiesT1.intersection(entitiesT2);
		double a = intersection.size();
		double c = entitiesT2.size() - a;
		if (c == 0) {
			return true;
		}
		return false;
	}

	private double probability(ExtractMethodRecomendation rec, EntitySet methodSet, EntitySet extractedSet) {
		int methodSize = (rec.getOriginalSize() - rec.getExtractedSize());
		double methodP = ((double) methodSize) / rec.getOriginalSize();
		double extractedP = 1.0 - methodP;
		double p = 1.0;
		for (String entityOfMethod : methodSet) {
			if (!extractedSet.contains(entityOfMethod)) {
				p *= Math.pow(methodP, methodSet.getCount(entityOfMethod));
			}
		}
		for (String entityOfExtracted : extractedSet) {
			if (!methodSet.contains(entityOfExtracted)) {
				p *= Math.pow(extractedP, extractedSet.getCount(entityOfExtracted));
			}
		}
		return p;
	}

	public String getScoreDetails(ExtractMethodRecomendation emr) {
		CompilationUnit cu = Utils.compile(emr.getSourceFile(), true);
		MethodDeclaration methodDeclaration = (MethodDeclaration) cu.findDeclaringNode(emr.getMethodBindingKey());
		ExtractionSlice slice = emr.getExtractionSlice();
		
		final EntitySet entitiesP1 = new EntitySet();
		final EntitySet entitiesP2 = new EntitySet();
		final EntitySet entitiesT1 = new EntitySet();
		final EntitySet entitiesT2 = new EntitySet();
		final EntitySet entitiesV1 = new EntitySet();
		final EntitySet entitiesV2 = new EntitySet();
		fillEntitySets(slice, entitiesP1, entitiesP2, entitiesT1, entitiesT2, entitiesV1, entitiesV2, methodDeclaration);
		
		StringBuilder sb = new StringBuilder();
		sb.append("# Modules:\n");
		this.printSets(sb, entitiesP1, entitiesP2);
		sb.append("# Types:\n");
		this.printSets(sb, entitiesT1, entitiesT2);
		sb.append("# Variables:\n");
		this.printSets(sb, entitiesV1, entitiesV2);
		return sb.toString();
    }

	private void printSets(StringBuilder sb, EntitySet mSet, EntitySet eSet) {
		EntitySet setA = mSet.intersection(eSet);
		EntitySet setB = mSet.minus(eSet);
		EntitySet setC = eSet.minus(mSet);
		sb.append(String.format("a=%d ", setA.size()));
		this.entitySetAsString(sb, setA);
		sb.append(String.format("b=%d ", setB.size()));
		this.entitySetAsString(sb, setB);
		sb.append(String.format("c=%d ", setC.size()));
		this.entitySetAsString(sb, setC);
	}
	
	public void entitySetAsString(StringBuilder sb, Iterable<String> methodSet) {
		sb.append("{\n");
		for (String item : methodSet) {
			sb.append("  ");
			sb.append(item);
			sb.append("\n");
		}
		sb.append("}\n");
	}
}
