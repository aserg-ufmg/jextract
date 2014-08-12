package br.ufmg.dcc.labsoft.jextractutils.evaluation;

import java.util.LinkedList;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import br.ufmg.dcc.labsoft.jextract.generation.EmrRecommender;
import br.ufmg.dcc.labsoft.jextract.generation.Settings;
import br.ufmg.dcc.labsoft.jextract.ranking.ExtractMethodRecomendation;

public class UtilsEmrRecommender extends EmrRecommender {

	private ProjectRelevantSet oracle = null;
	private ExecutionReport rep;
	//private int[] foundAt;
	//private int[] totalAt;
	
    public UtilsEmrRecommender(Settings settings) {
	    super(settings);
    }

	public void setOracle(IProject project, ProjectRelevantSet oracle, Database db) {
		this.oracle = oracle;
		this.rep = new ExecutionReport(this.settings, project, oracle, db);
	}

	protected void reportResults(ICompilationUnit src, MethodDeclaration methodDeclaration, LinkedList<ExtractMethodRecomendation> result) {
		if (this.oracle != null) {
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
	}
	
	public ExecutionReport getReport() {
		return this.rep;
	}

}
