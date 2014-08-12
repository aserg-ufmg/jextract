package br.ufmg.dcc.labsoft.jextractutils.evaluation;

import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import br.ufmg.dcc.labsoft.jextract.generation.EmrGenerator;
import br.ufmg.dcc.labsoft.jextract.generation.Settings;
import br.ufmg.dcc.labsoft.jextract.model.MethodModel;
import br.ufmg.dcc.labsoft.jextract.ranking.ExtractMethodRecomendation;

public class UtilsEmrGenerator extends EmrGenerator {

	private UtilsEmrRecommender recommender;
	private ProjectRelevantSet oracle;
	
	public UtilsEmrGenerator(List<ExtractMethodRecomendation> recomendations, Settings settings) {
		super(recomendations, settings);
		this.recommender = new UtilsEmrRecommender(settings);
	}

	@Override
	public UtilsEmrRecommender getRecommender() {
		return recommender;
	}

	public void setOracle(IProject project, ProjectRelevantSet goldset, Database db) {
		this.oracle = goldset;
		this.getRecommender().setOracle(project, goldset, db);
	}

	public ExecutionReport generateRecomendations(IProject project, ProjectRelevantSet goldset, Database db) throws Exception {
		this.setOracle(project, goldset, db);
		IJavaProject jp = (IJavaProject) JavaCore.create(project);
		Set<String> classes = goldset.getCoveredClasses();
		for (String className : classes) {
			IType type = jp.findType(className);
			analyseMethods(type.getCompilationUnit(), null);
		}
		return this.getRecommender().getReport();
	}

	public ExecutionReport evaluateRecomendations(IProject project) throws Exception {
		this.generateRecomendations(project);
		return this.recommender.getReport();
	}

	@Override
	protected void fillRecommendationInfo(ExtractMethodRecomendation recomendation, MethodModel model) {
		if (this.oracle != null) {
			recomendation.setDiffSize(this.oracle.getDiff(recomendation, model.getAstNode()));
		}
	}
	
	@Override
	protected boolean ignoreMethod(ICompilationUnit src, MethodDeclaration methodDeclaration) {
		IMethodBinding methodBinding = methodDeclaration.resolveBinding();
		final String methodSignature = methodBinding.toString();
		final String declaringType = methodBinding.getDeclaringClass().getQualifiedName();
		if (this.oracle != null && !this.oracle.isMethodAvailable(declaringType, methodSignature)) {
			return true;
		}
		return false;
	}
	
}
