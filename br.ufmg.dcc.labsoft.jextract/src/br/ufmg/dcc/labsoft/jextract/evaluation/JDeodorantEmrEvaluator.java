package br.ufmg.dcc.labsoft.jextract.evaluation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import br.ufmg.dcc.labsoft.jextract.codeanalysis.AstParser;
import br.ufmg.dcc.labsoft.jextract.generation.ExecutionReport;
import br.ufmg.dcc.labsoft.jextract.generation.Settings;
import br.ufmg.dcc.labsoft.jextract.ranking.ExtractMethodRecomendation;
import br.ufmg.dcc.labsoft.jextract.ranking.ExtractionSlice;
import br.ufmg.dcc.labsoft.jextract.ranking.StatementsSliceCountVisitor;

public class JDeodorantEmrEvaluator {

	private static final int COL_FILEPATH = 0;
	private static final int COL_METHOD = 1;
	private static final int COL_FRAGMENTS = 2;

	private final AstParser parser = new AstParser();
	private final IProject project;
	
	private final Settings settings;
	private final ExecutionReport rep;
	private ProjectRelevantSet goldset;
	
    public JDeodorantEmrEvaluator(Settings settings, IProject project, ProjectRelevantSet goldset, Database db) {
	    this.settings = settings;
	    this.project = project;
	    this.goldset = goldset;
	    this.rep = new ExecutionReport(this.settings, project, goldset, db);
    }

	public ExecutionReport evaluateResults(List<ExtractMethodRecomendation> recomendations) throws IOException {
		File file = new File(project.getLocation().toString() + "/jdeodorant.txt");
		BufferedReader br = new BufferedReader(new FileReader(file));
		try {
			String line;
			int id = 1;
			while ((line = br.readLine()) != null) {
				String[] cols = line.split("\t");
				String path = cols[COL_FILEPATH];
				String method = cols[COL_METHOD];
				String fragments = cols[COL_FRAGMENTS];
				
				ICompilationUnit icu = this.parser.getICompilationUnit(project, path);
				MethodDeclaration methodDeclaration = this.parser.getMethodDeclaration(icu, method);
				IMethodBinding methodBinding = methodDeclaration.resolveBinding();
				final String methodSignature = methodBinding.toString();
				final String declaringType = methodBinding.getDeclaringClass().getQualifiedName();
				
				ExtractionSlice slice = ExtractionSlice.fromString(fragments);
				ExtractMethodRecomendation emr = new ExtractMethodRecomendation(id++, declaringType, methodSignature, slice);
				emr.setSourceFile(icu);
				emr.setMethodBindingKey(method);
				
				StatementsSliceCountVisitor statementCounter = new StatementsSliceCountVisitor(slice);
				methodDeclaration.accept(statementCounter);
				emr.setOriginalSize(statementCounter.getCount());
				emr.setDuplicatedSize(statementCounter.getDuplicatedCount());
				emr.setExtractedSize(statementCounter.getExtractedCount());
				emr.setScore(0.0);
				
				if (this.goldset.isMethodAvailable(emr)) {
					recomendations.add(emr);
					this.rep.reportEmrAtRank(emr, 0);
				}
			}
		} finally {
			br.close();
		}
		return this.rep;
	}

}
