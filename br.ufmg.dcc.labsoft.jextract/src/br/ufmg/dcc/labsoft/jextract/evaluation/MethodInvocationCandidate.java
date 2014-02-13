package br.ufmg.dcc.labsoft.jextract.evaluation;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;

public final class MethodInvocationCandidate {

	private final ICompilationUnit src;
	private final IMethodBinding invoker;
	private final MethodInvocation invocation;
	private final IMethodBinding invoked;
	private final int size;
	private final boolean sameClass;

	public MethodInvocationCandidate(ICompilationUnit src, IMethodBinding invoker, MethodInvocation invocation, IMethodBinding invoked, int size, boolean sameClass) {
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

	public IMethodBinding getInvoker() {
		return invoker;
	}

	public MethodInvocation getInvocation() {
		return invocation;
	}

	public IMethodBinding getInvoked() {
		return invoked;
	}

	public int getSize() {
		return size;
	}

	public boolean isSameClass() {
		return sameClass;
	}
	
}
