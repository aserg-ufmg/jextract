package br.ufmg.dcc.labsoft.jextract.evaluation;

public class MethodData {

	final int size;
	final boolean hasPrivate;
	final boolean hasPackagePrivate;
	public MethodData(int size, boolean hasPrivate, boolean hasPackagePrivate) {
		super();
		this.size = size;
		this.hasPrivate = hasPrivate;
		this.hasPackagePrivate = hasPackagePrivate;
	}
}
