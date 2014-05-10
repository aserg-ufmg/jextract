package br.ufmg.dcc.labsoft.jextract.evaluation;

import org.eclipse.jdt.core.ICompilationUnit;

public final class MethodInvocationCandidate {

	private final ICompilationUnit src;
	private final String invoker;
	private final int invocation;
	private final String invoked;
	private final int size;
	private final boolean sameClass;

	public MethodInvocationCandidate(ICompilationUnit src, String invoker, int invocation, String invoked, int size, boolean sameClass) {
		this.src = src;
		this.invoker = invoker;
		this.invocation = invocation;
		this.invoked = invoked;
		this.size = size;
		this.sameClass = sameClass;
	}

	public ICompilationUnit getSrc() {
		return src;
	}

	public String getInvoker() {
		return invoker;
	}

	public int getInvocation() {
		return invocation;
	}

	public String getInvoked() {
		return invoked;
	}

	public int getSize() {
		return size;
	}

	public boolean isSameClass() {
		return sameClass;
	}
	
}
