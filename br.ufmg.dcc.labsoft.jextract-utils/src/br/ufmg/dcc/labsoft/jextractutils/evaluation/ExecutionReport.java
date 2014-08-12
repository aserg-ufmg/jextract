package br.ufmg.dcc.labsoft.jextractutils.evaluation;

import org.eclipse.core.resources.IProject;

import br.ufmg.dcc.labsoft.jextract.generation.Settings;
import br.ufmg.dcc.labsoft.jextract.ranking.ExtractMethodRecomendation;

public class ExecutionReport {

	private Database db = null;
	private final IProject project;
	private final Settings settings;
	private final ProjectRelevantSet goldset;
	private int[] foundAt;
	private int[] totalAt;
	
	public ExecutionReport(Settings settings, IProject project, ProjectRelevantSet goldset, Database db) {
		this.settings = settings;
		this.project = project;
		this.goldset = goldset;
		this.foundAt = new int[this.settings.getMaxPerMethod()];
		this.totalAt = new int[this.settings.getMaxPerMethod()];
		for (int i = 0; i < this.settings.getMaxPerMethod(); i++) {
			this.foundAt[i] = 0;
			this.totalAt[i] = 0;
		}
		this.db = db;
	}
	
	public IProject getProject() {
		return this.project;
	}

//	public void setProject(IProject project) {
//		this.project = project;
//	}

	public int getOracleSize() {
		return this.goldset.size();
	}
	
	public int getTotalAt(int i) {
		return this.totalAt[i];
	}
	
	public int getFoundAt(int i) {
		return this.foundAt[i];
	}
	
	public boolean reportEmrAtRank(ExtractMethodRecomendation rec, int i) {
		boolean inOracle = false;
		for (int j = i; j < this.settings.getMaxPerMethod(); j++) {
			this.totalAt[j]++;
		}
		if (this.goldset.contains(rec)) {
			//System.out.println(i + 1);
			inOracle = true;
			for (int j = i; j < this.settings.getMaxPerMethod(); j++) {
				this.foundAt[j]++;
			}
		}
		
		if (this.db != null) {
			this.db.insertEmi(this.settings.getId(), this.project.getName(), rec.getFilePath(), rec.getMethodBindingKey(), rec.getExtractionSlice().toString(), i, rec.getScore(), inOracle, rec.getDiffSize());
		}
		return inOracle;
	}
	
	public void printReport() {
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
