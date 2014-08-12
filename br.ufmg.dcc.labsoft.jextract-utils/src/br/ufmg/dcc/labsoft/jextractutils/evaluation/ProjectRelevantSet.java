package br.ufmg.dcc.labsoft.jextractutils.evaluation;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.dom.MethodDeclaration;

import br.ufmg.dcc.labsoft.jextract.ranking.EmrFileReader;
import br.ufmg.dcc.labsoft.jextract.ranking.ExtractMethodRecomendation;
import br.ufmg.dcc.labsoft.jextract.ranking.ExtractionSlice;
import br.ufmg.dcc.labsoft.jextract.ranking.StatementsSliceDiffCountVisitor;

public class ProjectRelevantSet {

	Map<String, ExtractMethodRecomendation> coveredMethods = new HashMap<String, ExtractMethodRecomendation>();
	Set<ExtractMethodRecomendation> set = new HashSet<ExtractMethodRecomendation>();
	Set<ExtractMethodRecomendation> reducedSet = new HashSet<ExtractMethodRecomendation>();

	public ProjectRelevantSet() {
		//
	}

	public ProjectRelevantSet(String path) {
		try {
			List<ExtractMethodRecomendation> emrList = new EmrFileReader().read(path);
			for (ExtractMethodRecomendation emr : emrList) {
				this.put(emr.id, emr.className, emr.method, emr.getExtractionSlice().toString());
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	protected void put(int id, String className, String method, String slice) {
		ExtractionSlice extractionSlice = ExtractionSlice.fromString(slice);
		ExtractMethodRecomendation rec = new ExtractMethodRecomendation(set.size() + 1, className, method, extractionSlice);
		set.add(rec);
		reducedSet.add(new ExtractMethodRecomendation(reducedSet.size() + 1, className, method, extractionSlice.reduce()));
		coveredMethods.put(getClassAndMethod(className, method), rec);
	}

	private String getClassAndMethod(String className, String method) {
		return className + "\t" + method;
	}

	public boolean contains(ExtractMethodRecomendation rec) {
		return rec.getDiffSize() == 0 || set.contains(rec);
	}

	public boolean containsReduced(ExtractMethodRecomendation rec) {
		return reducedSet.contains(rec);
	}

	public boolean isMethodAvailable(ExtractMethodRecomendation rec) {
		return coveredMethods.containsKey(getClassAndMethod(rec.className, rec.method));
	}

	public ExtractMethodRecomendation getAnswerFor(ExtractMethodRecomendation rec) {
		return this.coveredMethods.get(getClassAndMethod(rec.className, rec.method));
	}

	public int getDiff(ExtractMethodRecomendation rec, MethodDeclaration astNode) {
		ExtractionSlice idealAnswer = this.getAnswerFor(rec).getExtractionSlice();
		if (idealAnswer == null) {
			return -1;
		}
		StatementsSliceDiffCountVisitor c2 = new StatementsSliceDiffCountVisitor(rec.getExtractionSlice(), idealAnswer);
		astNode.accept(c2);
		return c2.getDiffCount();
	}

	public boolean isMethodAvailable(String className, String method) {
		return coveredMethods.containsKey(getClassAndMethod(className, method));
	}

	public int size() {
		return this.set.size();
	}
	
	public Set<String> getCoveredClasses() {
		HashSet<String> covered = new HashSet<String>();
		for (ExtractMethodRecomendation emr : set) {
			covered.add(emr.className);
		}
		return covered;
	}

}
