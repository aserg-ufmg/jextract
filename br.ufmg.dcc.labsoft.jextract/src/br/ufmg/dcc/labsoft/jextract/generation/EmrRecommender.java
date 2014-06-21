package br.ufmg.dcc.labsoft.jextract.generation;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import br.ufmg.dcc.labsoft.jextract.evaluation.ProjectRelevantSet;
import br.ufmg.dcc.labsoft.jextract.ranking.ExtractMethodRecomendation;
import br.ufmg.dcc.labsoft.jextract.ranking.ExtractionSlice;
import br.ufmg.dcc.labsoft.jextract.ranking.ExtractionSlice.Fragment;
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
		Utils.sort(recomendations, false);
		
		final int maxPerMethod = this.settings.getMaxPerMethod();
		final Double minScore = this.settings.getMinScore();
		for (ExtractMethodRecomendation recommendation : recomendations) {
			ExtractionSlice slice = recommendation.getExtractionSlice();
			boolean greaterEqualMinScore = recommendation.getScore() >= minScore;
			if (!greaterEqualMinScore) {
				continue;
			}
			Fragment frag = slice.getEnclosingFragment();
			boolean valid = slice.isComposed() || Utils.canExtract(src, frag.start, frag.length());
			if (!valid) {
				continue;
			}
			result.add(recommendation);
			recommendation.setRank(result.size());
			if (result.size() >= maxPerMethod) {
				break;
			}
		}
		
		if (this.goldset != null) {
			String id = methodDeclaration.resolveBinding().getDeclaringClass().getQualifiedName() + " " + methodDeclaration.getName();
			//System.out.print(id + ": ");
			boolean found = false;
			int i = 0;
			for (ExtractMethodRecomendation rec : result) {
				for (int j = i; j < this.settings.getMaxPerMethod(); j++) {
					this.totalAt[j]++;
				}
				if (this.goldset.contains(rec)) {
					//System.out.println(i + 1);
					found = true;
					for (int j = i; j < this.settings.getMaxPerMethod(); j++) {
						this.foundAt[j]++;
					}
				}
				i++;
			}
			if (!found) {
				//System.out.println("not found");
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
			final ExtractionSlice slice = alternative.getExtractionSlice();
			StatementsSliceCountVisitor statementCounter = new StatementsSliceCountVisitor(slice);
			methodDeclaration.accept(statementCounter);
			alternative.setOriginalSize(statementCounter.getCount());
			alternative.setDuplicatedSize(statementCounter.getDuplicatedCount());
			alternative.setExtractedSize(statementCounter.getExtractedCount());

			if (alternative.getSourceFile() == null) {
				alternative.setSourceFile(src);
			}
			alternative.setMethodBindingKey(methodBinding.getKey());
		}
	}

	public void printReport(IProject project) {
	    if (this.goldset != null) {
	    	System.out.println("----------------------------");
	    	System.out.println(project.getName());
	    	System.out.println("----------------------------");
	    	System.out.print("total:     ");
	    	for (int i = 0; i < this.settings.getMaxPerMethod(); i++) {
	    		System.out.printf("%5d ", this.totalAt[i]);
	    	}
	    	System.out.println();
	    	System.out.print("correct:   ");
	    	for (int i = 0; i < this.settings.getMaxPerMethod(); i++) {
	    		System.out.printf("%5d ", this.foundAt[i]);
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
