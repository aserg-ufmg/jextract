package br.ufmg.dcc.labsoft.jextract.generation;

public class Settings {

	private Integer minSize = 3;
	private Integer maxPerMethod = 3;
	private Integer maxFragments = 1;
	private Double penalty = 0.0;
	private Double minScore = 0.0;

	public Integer getMinSize() {
		return minSize;
	}

	public void setMinSize(int minSize) {
		this.minSize = minSize;
	}

	public Integer getMaxPerMethod() {
		return maxPerMethod;
	}

	public void setMaxPerMethod(int maxPerMethod) {
		this.maxPerMethod = maxPerMethod;
	}

	public Integer getMaxFragments() {
		return maxFragments;
	}

	public void setMaxFragments(int maxFragments) {
		this.maxFragments = maxFragments;
	}

	public Double getPenalty() {
		return penalty;
	}

	public void setPenalty(double penalty) {
		this.penalty = penalty;
	}

	public Double getMinScore() {
		return this.minScore;
	}

	public void setMinScore(Double minScore) {
		this.minScore = minScore;
	}
	
}
