package br.ufmg.dcc.labsoft.jextractutils.evaluation;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;

public final class MethodInvocationCandidate {

	private final ICompilationUnit src;
	private final String invoker;
	private final int invocation;
	private final ICompilationUnit srcInvoked;
	private final String invoked;
	private final int size;
	private final boolean sameClass;

	public MethodInvocationCandidate(ICompilationUnit src, String invoker, int invocation, ICompilationUnit srcInvoked, String invoked, int size, boolean sameClass) {
		this.src = src;
		this.invoker = invoker;
		this.invocation = invocation;
		this.srcInvoked = srcInvoked;
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

	public ICompilationUnit getSrcInvoked() {
		return srcInvoked;
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

	@Override
	public String toString() {
        try {
        	String srcFile = this.src.getUnderlyingResource().getProjectRelativePath().toString();
        	String srcFileInvoked = this.srcInvoked.getUnderlyingResource().getProjectRelativePath().toString();
            return String.format("%s\t%s\t%s\t%s\t%d", srcFile, this.getInvoker(), srcFileInvoked, this.getInvoked(), this.sameClass ? 1 : 0);
        } catch (JavaModelException e) {
            throw new RuntimeException(e);
        }
	}

}
