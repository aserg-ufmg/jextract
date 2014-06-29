package br.ufmg.dcc.labsoft.jextract.generation;

import br.ufmg.dcc.labsoft.jextract.ranking.Coefficient;

public class Settings {

	private String id;
	private Integer minMethodSize = 3;
	private Integer minExtractedSize = 3;
	private Integer maxPerMethod = 3;
	private Integer maxFragments = 1;
	private Double penalty = 0.0;
	private Double minScore = 0.0;

	public boolean includeMethodCalls = false;
	public boolean includeExternalFields = true;
	public boolean splitParentPackages = true;
	public boolean includeTypeArguments = true;

	public boolean useProbabilityFactor = false;
	public boolean zeroScoreOnEmptyExtractionSet = true;
	public boolean zeroScoreOnEmptyRemainingSet = true;
	public double wP = 1.0;
	public double wT = 1.0;
	public double wV = 1.0;
	private Coefficient coefficient = Coefficient.KUL;

	//private String typesToIgnore = "java.lang.*,java.util.*";
	//private String packagesToIgnore = "java,javax,com,org,br";
	private String typesToIgnore = "";
	private String packagesToIgnore = "java,sun,com,org,br";

	public Settings() {
		this.id = "default";
	}
	
	public Settings(String id) {
		this.id = id;
	}
	
	public String getId() {
		return this.id;
	}

	public Integer getMinMethodSize() {
		return this.minMethodSize;
	}

	public void setMinMethodSize(Integer minMethodSize) {
		this.minMethodSize = minMethodSize;
	}

	public Integer getMinExtractedSize() {
		return minExtractedSize;
	}

	public void setMinExtractedSize(int minExtractedSize) {
		this.minExtractedSize = minExtractedSize;
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

	public Coefficient getCoefficient() {
		return coefficient;
	}

	public void setCoefficient(Coefficient coefficient) {
		this.coefficient = coefficient;
	}

	public String getTypesToIgnore() {
		return typesToIgnore;
	}

	public void setTypesToIgnore(String typesToIgnore) {
		this.typesToIgnore = typesToIgnore;
	}

	public String getPackagesToIgnore() {
		return packagesToIgnore;
	}

	public void setPackagesToIgnore(String packagesToIgnore) {
		this.packagesToIgnore = packagesToIgnore;
	}

}
