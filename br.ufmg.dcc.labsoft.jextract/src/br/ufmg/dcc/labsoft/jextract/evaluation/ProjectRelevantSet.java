package br.ufmg.dcc.labsoft.jextract.evaluation;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import br.ufmg.dcc.labsoft.jextract.ranking.EmrFileReader;
import br.ufmg.dcc.labsoft.jextract.ranking.ExtractMethodRecomendation;
import br.ufmg.dcc.labsoft.jextract.ranking.ExtractionSlice;

public class ProjectRelevantSet {

	Set<String> coveredMethods = new HashSet<String>();
	Set<ExtractMethodRecomendation> set = new HashSet<ExtractMethodRecomendation>();
	Set<ExtractMethodRecomendation> reducedSet = new HashSet<ExtractMethodRecomendation>();

	public ProjectRelevantSet() {
		//
	}

	public ProjectRelevantSet(String path) {
		try {
			List<ExtractMethodRecomendation> emrList = new EmrFileReader().read(path);
			for (ExtractMethodRecomendation emr : emrList) {
				this.put(emr.id, emr.className, emr.method, emr.getSlice().toString());
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	protected void put(int id, String className, String method, String slice) {
		ExtractionSlice extractionSlice = ExtractionSlice.fromString(slice);
		set.add(new ExtractMethodRecomendation(set.size() + 1, className, method, extractionSlice));
		reducedSet.add(new ExtractMethodRecomendation(reducedSet.size() + 1, className, method, extractionSlice.reduce()));
		coveredMethods.add(getClassAndMethod(className, method));
	}

	private String getClassAndMethod(String className, String method) {
		return className + "\t" + method;
	}

	public boolean contains(ExtractMethodRecomendation rec) {
		return set.contains(rec);
	}

	public boolean containsReduced(ExtractMethodRecomendation rec) {
		return reducedSet.contains(rec);
	}

	public boolean isMethodAvailable(ExtractMethodRecomendation rec) {
		return coveredMethods.contains(getClassAndMethod(rec.className, rec.method));
	}

	public boolean isMethodAvailable(String className, String method) {
		return coveredMethods.contains(getClassAndMethod(className, method));
	}

	public int size() {
		return this.set.size();
	}
}
