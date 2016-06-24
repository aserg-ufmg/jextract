package br.ufmg.dcc.labsoft.jextract.ui;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IJavaProject;

public class JavaProjectMenuAction extends AbstractProjectMenuAction<IJavaProject> {

	public JavaProjectMenuAction() {
		super(IJavaProject.class);
	}

	@Override
	protected IProject convertoToIProject(IJavaProject object) {
		return object.getProject();
	}

}
