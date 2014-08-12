package br.ufmg.dcc.labsoft.jextractutils.evaluation;

public class MethodData {

	final int size;
	final int numberOfArguments;
	final boolean voidMethod;
	public MethodData(int size, int numberOfArguments, boolean voidMethod) {
		super();
		this.size = size;
		this.numberOfArguments = numberOfArguments;
		this.voidMethod = voidMethod;
	}
}
