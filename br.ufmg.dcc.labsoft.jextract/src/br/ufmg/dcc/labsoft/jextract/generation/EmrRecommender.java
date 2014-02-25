package br.ufmg.dcc.labsoft.jextract.generation;

import java.util.ArrayList;
import java.util.List;

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
import br.ufmg.dcc.labsoft.jextract.ranking.SetsSimilarity;
import br.ufmg.dcc.labsoft.jextract.ranking.StatementsSliceCountVisitor;
import br.ufmg.dcc.labsoft.jextract.ranking.Utils;

public class EmrRecommender {

	private ProjectRelevantSet goldset = null;
	private int maxPerMethod = 3;

	public void setGoldset(ProjectRelevantSet goldset) {
		this.goldset = goldset;
	}

	public List<ExtractMethodRecomendation> rankAndFilterForMethod(ICompilationUnit src, MethodDeclaration methodDeclaration, List<ExtractMethodRecomendation> recomendations) {
		List<ExtractMethodRecomendation> result = new ArrayList<ExtractMethodRecomendation>();
		this.analyseMethod(src, methodDeclaration, recomendations);
		Utils.sort(recomendations, EmrScoringFn.KUL_TVM, false);
		int i = 0;
		String id = methodDeclaration.resolveBinding().getDeclaringClass().getName() + " " + methodDeclaration.getName();
		System.out.print(id + ": ");
		
		boolean found = false;
		for (ExtractMethodRecomendation recommendation : recomendations) {
			result.add(recommendation);
			i++;
			if (this.goldset != null) {
				if (this.goldset.contains(recommendation)) {
					System.out.println(i);
					found = true;
					break;
				}
			} else if (result.size() >= this.maxPerMethod) {
				break;
			}
		}
		
		if (!found) {
			System.out.println("not found");
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

			SetsSimilarity<String> ssimT = this.computeSetsSimilarity(methodDeclaration, slice, true, false, false);
			SetsSimilarity<String> ssimV = this.computeSetsSimilarity(methodDeclaration, slice, false, true, false);
			SetsSimilarity<String> ssimM = this.computeSetsSimilarity(methodDeclaration, slice, false, false, true);
			alternative.setSsimT(ssimT);
			alternative.setSsimV(ssimV);
			alternative.setSsimM(ssimM);

			alternative.setPt(computeProb(methodDeclaration, slice, true, false, false, alternative));
			alternative.setPv(computeProb(methodDeclaration, slice, false, true, false, alternative));
			alternative.setPm(computeProb(methodDeclaration, slice, false, false, true, alternative));
		}
	}

	private SetsSimilarity<String> computeSetsSimilarity(MethodDeclaration methodDeclaration,
	        final ExtractionSlice slice, final boolean typeAccess, final boolean variableAccess,
	        final boolean packageAccess) {
		final SetsSimilarity<String> ssim = new SetsSimilarity<String>();
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

}
