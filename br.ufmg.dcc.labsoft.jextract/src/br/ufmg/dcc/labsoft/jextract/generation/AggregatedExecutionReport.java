package br.ufmg.dcc.labsoft.jextract.generation;

import java.util.ArrayList;
import java.util.List;


public class AggregatedExecutionReport {

	private final Settings settings;
	private int totalOracleSize = 0;
	private int[] foundAt;
	private int[] totalAt;
	private List<ExecutionReport> reports;
	
	public AggregatedExecutionReport(Settings settings) {
		this.settings = settings;
		this.reports = new ArrayList<ExecutionReport>();
		this.foundAt = new int[this.settings.getMaxPerMethod()];
		this.totalAt = new int[this.settings.getMaxPerMethod()];
		for (int i = 0; i < this.settings.getMaxPerMethod(); i++) {
			this.foundAt[i] = 0;
			this.totalAt[i] = 0;
		}
	}
	
	public void merge(ExecutionReport rep) {
		this.reports.add(rep);
		this.totalOracleSize += rep.getOracleSize();
		for (int i = 0; i < this.settings.getMaxPerMethod(); i++) {
			this.foundAt[i] += rep.getFoundAt(i);
			this.totalAt[i] += rep.getTotalAt(i);
		}
	}
	
	public void printReport() {
		for (ExecutionReport rep : this.reports) {
			rep.printReport();
		}
		if (this.reports.size() <= 1) {
			return;
		}
		
    	System.out.println("----------------------------");
    	System.out.println("Summary");
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
    		double recall = ((double) this.foundAt[i]) / this.totalOracleSize;
    		System.out.printf("%.3f ", recall);
    	}
    	System.out.println();
    	System.out.println("----------------------------");
    }
	
	public void printSummary() {
		System.out.print(this.settings.getId());
		System.out.print('\t');
		System.out.printf("%5d ", this.totalOracleSize);
		System.out.print('\t');
		System.out.printf("%.3f ", ((double) this.foundAt[0]) / this.totalOracleSize);
		System.out.print('\t');
		System.out.printf("%.3f ", ((double) this.foundAt[1]) / this.totalOracleSize);
		System.out.printf("%.3f ", ((double) this.foundAt[2]) / this.totalOracleSize);
		System.out.println();
	}
}
