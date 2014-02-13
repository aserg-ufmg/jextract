package br.ufmg.dcc.labsoft.jextract.ranking;

import org.eclipse.jdt.core.ICompilationUnit;

public class ExtractMethodRecomendation {

	public int rank = 0;
	public final int id;
	public final String className;
	public final String method;
	private int duplicatedSize;
	private int extractedSize;
	private int originalSize;
	final ExtractionSlice slice;
	private SetsSimilarity<String> ssimT;
	private SetsSimilarity<String> ssimV;
	private SetsSimilarity<String> ssimM;
	private double pt = 0.0;
	private double pv = 0.0;
	private double pm = 0.0;
//	private double ptv = 0.0;
	private ICompilationUnit sourceFile;
	private boolean ok = false;
	private boolean relevant = false;
	private boolean similar = false;
	private boolean availableInGoldSet = false;

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

	public void setSourceFile(ICompilationUnit sourceFile) {
		this.sourceFile = sourceFile;
	}

	public ExtractionSlice getSlice() {
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

	public void setSsimT(SetsSimilarity<String> ssimT) {
		this.ssimT = ssimT;
	}

	public void setSsimV(SetsSimilarity<String> ssimV) {
		this.ssimV = ssimV;
	}

	public void setSsimM(SetsSimilarity<String> ssimM) {
		this.ssimM = ssimM;
	}

	public String getExplanation() {
		StringBuilder sb = new StringBuilder();
		sb.append(String.format("Typ(a=%d, b=%d, c=%d)", this.ssimT.getA(), this.ssimT.getB(), this.ssimT.getC()));
		sb.append(" | ");
		sb.append(String.format("Var(a=%d, b=%d, c=%d)", this.ssimV.getA(), this.ssimV.getB(), this.ssimV.getC()));
		sb.append(" | ");
		sb.append(String.format("Mod(a=%d, b=%d, c=%d)", this.ssimM.getA(), this.ssimM.getB(), this.ssimM.getC()));
		return sb.toString();
	}

	public String getExplanationDetails() {
		return Utils.explain(this.ssimT, this.ssimV, this.ssimM);
	}

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

	public double getPt() {
		return pt;
	}

	public void setPt(double pt) {
		this.pt = pt;
	}

	public double getPv() {
		return pv;
	}

	public void setPv(double pv) {
		this.pv = pv;
	}

	public double getPm() {
		return pm;
	}
	
	public void setPm(double pm) {
		this.pm = pm;
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

	public SetsSimilarity<String> getSsimT() {
		return ssimT;
	}

	public SetsSimilarity<String> getSsimV() {
		return ssimV;
	}

	public SetsSimilarity<String> getSsimM() {
		return ssimM;
	}

}
