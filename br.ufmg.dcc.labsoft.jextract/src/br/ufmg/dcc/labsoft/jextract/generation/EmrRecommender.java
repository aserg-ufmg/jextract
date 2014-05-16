package br.ufmg.dcc.labsoft.jextract.generation;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import br.ufmg.dcc.labsoft.jextract.evaluation.ProjectRelevantSet;
import br.ufmg.dcc.labsoft.jextract.ranking.DependenciesAstVisitor;
import br.ufmg.dcc.labsoft.jextract.ranking.EmrScoringFn;
import br.ufmg.dcc.labsoft.jextract.ranking.ExtractMethodRecomendation;
import br.ufmg.dcc.labsoft.jextract.ranking.ExtractionSlice;
import br.ufmg.dcc.labsoft.jextract.ranking.ExtractionSlice.Fragment;
import br.ufmg.dcc.labsoft.jextract.ranking.SetsSimilarity;
import br.ufmg.dcc.labsoft.jextract.ranking.StatementsSliceCountVisitor;
import br.ufmg.dcc.labsoft.jextract.ranking.Utils;

public class EmrRecommender {

	private ProjectRelevantSet goldset = null;
	private final Settings settings;
	private int[] foundAt;
	private int[] totalAt;
	
    public EmrRecommender(Settings settings) {
	    this.settings = settings;
    }

	public void setGoldset(ProjectRelevantSet goldset) {
		this.goldset = goldset;
		this.foundAt = new int[this.settings.getMaxPerMethod()];
		this.totalAt = new int[this.settings.getMaxPerMethod()];
		for (int i = 0; i < this.settings.getMaxPerMethod(); i++) {
			this.foundAt[i] = 0;
			this.totalAt[i] = 0;
		}
	}

	public List<ExtractMethodRecomendation> rankAndFilterForMethod(ICompilationUnit src, MethodDeclaration methodDeclaration, List<ExtractMethodRecomendation> recomendations) {
		LinkedList<ExtractMethodRecomendation> result = new LinkedList<ExtractMethodRecomendation>();
		this.analyseMethod(src, methodDeclaration, recomendations);
		Utils.sort(recomendations, EmrScoringFn.SCORE, false);
		
		final int maxPerMethod = this.settings.getMaxPerMethod();
		final Double minScore = this.settings.getMinScore();
		for (ExtractMethodRecomendation recommendation : recomendations) {
			ExtractionSlice slice = recommendation.getSlice();
			boolean greaterEqualMinScore = EmrScoringFn.SCORE.score(recommendation) >= minScore;
			if (!greaterEqualMinScore) {
				continue;
			}
			Fragment frag = slice.getEnclosingFragment();
			boolean valid = slice.isComposed() || Utils.canExtract(src, frag.start, frag.length());
			if (!valid) {
				continue;
			}
			result.add(recommendation);
			if (result.size() >= maxPerMethod) {
				break;
			}
		}
		
		if (this.goldset != null) {
			String id = methodDeclaration.resolveBinding().getDeclaringClass().getQualifiedName() + " " + methodDeclaration.getName();
			System.out.print(id + ": ");
			boolean found = false;
			int i = 0;
			for (ExtractMethodRecomendation rec : result) {
				for (int j = i; j < this.settings.getMaxPerMethod(); j++) {
					this.totalAt[j]++;
				}
				if (this.goldset.contains(rec)) {
					System.out.println(i + 1);
					found = true;
					for (int j = i; j < this.settings.getMaxPerMethod(); j++) {
						this.foundAt[j]++;
					}
				}
				i++;
			}
			if (!found) {
				System.out.println("not found");
			}
		}
		
		return result;
	}

	private void analyseMethod(ICompilationUnit src, MethodDeclaration methodDeclaration, List<ExtractMethodRecomendation> alternatives) {
		IMethodBinding methodBinding = methodDeclaration.resolveBinding();
		String methodSignature = methodBinding.toString();
		String declaringType = methodBinding.getDeclaringClass().getQualifiedName();

		//String key = declaringType + "\t" + methodSignature;
		//System.out.println("Analysing recomendations for " + key);

		for (ExtractMethodRecomendation alternative : alternatives) {
			final ExtractionSlice slice = alternative.getSlice();
			StatementsSliceCountVisitor statementCounter = new StatementsSliceCountVisitor(slice);
			methodDeclaration.accept(statementCounter);
			alternative.setOriginalSize(statementCounter.getCount());
			alternative.setDuplicatedSize(statementCounter.getDuplicatedCount());
			alternative.setExtractedSize(statementCounter.getExtractedCount());

			if (alternative.getSourceFile() == null) {
				alternative.setSourceFile(src);
			}

			SetsSimilarity ssimT = this.computeSetsSimilarity(methodDeclaration, slice, true, false, false);
			SetsSimilarity ssimV = this.computeSetsSimilarity(methodDeclaration, slice, false, true, false);
			SetsSimilarity ssimM = this.computeSetsSimilarity(methodDeclaration, slice, false, false, true);
			alternative.setSsimT(ssimT);
			alternative.setSsimV(ssimV);
			alternative.setSsimM(ssimM);

			alternative.setPt(computeProb(methodDeclaration, slice, true, false, false, alternative));
			alternative.setPv(computeProb(methodDeclaration, slice, false, true, false, alternative));
			alternative.setPm(computeProb(methodDeclaration, slice, false, false, true, alternative));
		}
	}

	private SetsSimilarity computeSetsSimilarity(MethodDeclaration methodDeclaration,
	        final ExtractionSlice slice, final boolean typeAccess, final boolean variableAccess,
	        final boolean packageAccess) {
		final SetsSimilarity ssim = new SetsSimilarity();
		methodDeclaration.accept(new DependenciesAstVisitor(methodDeclaration.resolveBinding().getDeclaringClass()) {
			@Override
			public void onTypeAccess(ASTNode node, ITypeBinding binding) {
				if (!typeAccess) {
					return;
				}
				if (slice.belongsToMethod(node.getStartPosition())) {
					ssim.addToSet1(binding.getKey());
				}
				if (slice.belongsToExtracted(node.getStartPosition())) {
					ssim.addToSet2(binding.getKey());
				}
			}

			@Override
			public void onVariableAccess(ASTNode node, IVariableBinding binding) {
				if (!variableAccess) {
					return;
				}
				if (slice.belongsToMethod(node.getStartPosition())) {
					ssim.addToSet1(binding.getKey());
				}
				if (slice.belongsToExtracted(node.getStartPosition())) {
					ssim.addToSet2(binding.getKey());
				}
			}

			@Override
			public void onModuleAccess(ASTNode node, String packageName) {
				if (!packageAccess) {
					return;
				}
				if (slice.belongsToMethod(node.getStartPosition())) {
					ssim.addToSet1(packageName);
				}
				if (slice.belongsToExtracted(node.getStartPosition())) {
					ssim.addToSet2(packageName);
				}
			}
		});
		ssim.end();
		return ssim;
	}

	private double computeProb(MethodDeclaration methodDeclaration, final ExtractionSlice slice,
	        final boolean typeAccess, final boolean variableAccess, final boolean packageAccess,
	        ExtractMethodRecomendation alternative) {
		return 1.0;
	}

	public void printReport(IProject project) {
	    if (this.goldset != null) {
	    	System.out.println("----------------------------");
	    	System.out.println(project.getName());
	    	System.out.println("----------------------------");
	    	System.out.print("total:     ");
	    	for (int i = 0; i < this.settings.getMaxPerMethod(); i++) {
	    		System.out.printf("%05d ", this.totalAt[i]);
	    	}
	    	System.out.println();
	    	System.out.print("correct:   ");
	    	for (int i = 0; i < this.settings.getMaxPerMethod(); i++) {
	    		System.out.printf("%05d ", this.foundAt[i]);
	    	}
	    	System.out.println();
	    	System.out.print("precision: ");
	    	for (int i = 0; i < this.settings.getMaxPerMethod(); i++) {
				double precision = ((double) this.foundAt[i]) / this.totalAt[i];
				System.out.printf("%.3f ", precision);
			}
	    	System.out.println();
	    	System.out.print("recall:    ");
	    	for (int i = 0; i < this.settings.getMaxPerMethod(); i++) {
	    		double recall = ((double) this.foundAt[i]) / this.goldset.size();
	    		System.out.printf("%.3f ", recall);
	    	}
	    	System.out.println();
	    	System.out.println("----------------------------");
	    }
    }

}
