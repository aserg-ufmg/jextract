package br.ufmg.dcc.labsoft.jextract.generation;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import br.ufmg.dcc.labsoft.jextract.evaluation.Database;
import br.ufmg.dcc.labsoft.jextract.evaluation.ProjectRelevantSet;
import br.ufmg.dcc.labsoft.jextract.ranking.ExtractMethodRecomendation;
import br.ufmg.dcc.labsoft.jextract.ranking.ExtractionSlice;
import br.ufmg.dcc.labsoft.jextract.ranking.ExtractionSlice.Fragment;
import br.ufmg.dcc.labsoft.jextract.ranking.StatementsSliceCountVisitor;
import br.ufmg.dcc.labsoft.jextract.ranking.Utils;

public class EmrRecommender {

	private ProjectRelevantSet goldset = null;
	private final Settings settings;
	private ExecutionReport rep;
	//private int[] foundAt;
	//private int[] totalAt;
	
    public EmrRecommender(Settings settings) {
	    this.settings = settings;
    }

	public void setGoldset(IProject project, ProjectRelevantSet goldset, Database db) {
		this.goldset = goldset;
		this.rep = new ExecutionReport(this.settings, project, goldset, db);
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
			//String id = methodDeclaration.resolveBinding().getDeclaringClass().getQualifiedName() + " " + methodDeclaration.getName();
			//System.out.print(id + ": ");
			boolean found = false;
			int i = 0;
			for (ExtractMethodRecomendation rec : result) {
				if (this.rep.reportEmrAtRank(rec, i)) {
					found = true;
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

	public ExecutionReport getReport() {
		return this.rep;
	}

}
