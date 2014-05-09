package br.ufmg.dcc.labsoft.jextract.evaluation;

import org.eclipse.jdt.core.IProblemRequestor;
import org.eclipse.jdt.core.compiler.IProblem;

public class ProblemDetector implements IProblemRequestor {

	private boolean problems = false;

	@Override
	public void acceptProblem(IProblem problem) {
		this.problems = true;
	}

	@Override
	public void beginReporting() {
	}

	@Override
	public void endReporting() {
	}

	@Override
	public boolean isActive() {
		return true;
	} // will detect problems if active

	public boolean hasProblems() {
		return this.problems;
	}
}
