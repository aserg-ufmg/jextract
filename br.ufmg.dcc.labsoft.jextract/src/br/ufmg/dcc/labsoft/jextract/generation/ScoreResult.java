package br.ufmg.dcc.labsoft.jextract.generation;

public class ScoreResult {

	private final double score;
	private final String explanation;
	
	public ScoreResult(double score, String explanation) {
		this.score = score;
		this.explanation = explanation;
	}
	public double getScore() {
		return score;
	}
	public String getExplanation() {
		return explanation;
	}
	
}
