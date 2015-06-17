package br.ufmg.dcc.labsoft.jextract.ranking;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;

import br.ufmg.dcc.labsoft.jextract.model.OffsetBasedEmrDescriptor;

public class ExtractMethodRecomendation implements OffsetBasedEmrDescriptor {

	private int rank = 0;
	public final int id;
	public String project;
	public final String className;
	public final String method;
	private int duplicatedSize;
	private int extractedSize;
	private int diffSize = -1;
	private int originalSize;
	private int reorderedSize = 0;
	final ExtractionSlice slice;
//	private double ptv = 0.0;
	private ICompilationUnit sourceFile;
	private String methodBindingKey;
	private boolean ok = false;
	private boolean relevant = false;
	private boolean similar = false;
	private boolean availableInGoldSet = false;
	private double score = 0.0;
	private String explanation = "";

	public ExtractMethodRecomendation(int id, String className, String method, ExtractionSlice slice) {
		this.id = id;
		this.className = className;
		this.method = method;
		this.slice = slice;
	}

	@Override
	public String toString() {
		return String.format("%s\t%s\t%s", className, method, slice.toString());
	}

	public ICompilationUnit getSourceFile() {
		return sourceFile;
	}

	@Override
	public String getFilePath() {
		try {
			return sourceFile.getUnderlyingResource().getProjectRelativePath().toString();
		} catch (JavaModelException e) {
			throw new RuntimeException(e);
		}
	}

	public void setSourceFile(ICompilationUnit sourceFile) {
		this.sourceFile = sourceFile;
	}
	
	@Override
	public String getMethodBindingKey() {
		return this.methodBindingKey;
	}

	public void setMethodBindingKey(String methodBindingKey) {
		this.methodBindingKey = methodBindingKey;
	}

	@Override
	public ExtractionSlice getExtractionSlice() {
		return slice;
	}

	public int getOriginalSize() {
		return originalSize;
	}

	public void setOriginalSize(int size) {
		this.originalSize = size;
	}

	public double getDuplicationRatio() {
		return ((double) this.duplicatedSize) / this.extractedSize;
	}

	public double getMinSize() {
		int extractedOnly = this.extractedSize - this.duplicatedSize;
		return Math.min(this.originalSize - extractedOnly, extractedOnly);
	}

	public void setOk(boolean eclipsePreconditionsOk) {
		this.ok = eclipsePreconditionsOk;
	}

	public boolean isOk() {
		return this.ok;
	}

	public void setExplanation(String explanation) {
		this.explanation = explanation;
	}

	public String getExplanation() {
		return this.explanation;
	}

//	public String getExplanationDetails() {
//		return Utils.explain(this.ssimT, this.ssimV, this.ssimM);
//	}

	public int getRank() {
		return rank;
	}

	public void setRank(int rank) {
		this.rank = rank;
	}

	public int getDuplicatedSize() {
		return duplicatedSize;
	}

	public void setDuplicatedSize(int duplicatedSize) {
		this.duplicatedSize = duplicatedSize;
	}

	public int getExtractedSize() {
		return extractedSize;
	}

	public void setExtractedSize(int extractedSize) {
		this.extractedSize = extractedSize;
	}

	public int getDiffSize() {
		return this.diffSize;
	}

	public void setDiffSize(int diffSize) {
		this.diffSize = diffSize;
	}

	public int getReorderedSize() {
		return this.reorderedSize;
	}

	public void setReorderedSize(int reorderedSize) {
		this.reorderedSize = reorderedSize;
	}

//	public double getPtv() {
//		return ptv;
//	}

//	public void setPtv(double ptv) {
//		this.ptv = ptv;
//	}

	public String getKey() {
		return String.format("%s\t%s\t%s", this.className, this.method, this.slice.toString());
	}

	@Override
	public int hashCode() {
		return this.getKey().hashCode();
	}

	@Override
	public boolean equals(Object arg0) {
		if (arg0 instanceof ExtractMethodRecomendation) {
			ExtractMethodRecomendation emr = (ExtractMethodRecomendation) arg0;
			return this.getKey().equals(emr.getKey());
		}
		return false;
	}

	public boolean isRelevant() {
		return relevant;
	}

	public void setRelevant(boolean relevant) {
		this.relevant = relevant;
	}

	public boolean isSimilar() {
		return similar;
	}

	public void setSimilar(boolean similar) {
		this.similar  = similar;
	}

	public boolean isAvailableInGoldSet() {
		return availableInGoldSet;
	}

	public void setAvailableInGoldSet(boolean availableInGoldSet) {
		this.availableInGoldSet = availableInGoldSet;
	}

	public int getSafeness() {
		return this.slice.isComposed() ? 0 : 1;
	}

	public double getScore() {
		return score;
	}

	public void setScore(double score) {
		this.score = score;
	}

	public String getProject() {
		return project;
	}

	public void setProject(String project) {
		this.project = project;
	}
	
}
