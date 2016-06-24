package br.ufmg.dcc.labsoft.jextract.ui;

import org.eclipse.core.resources.IProject;

public class ProjectMenuAction extends AbstractProjectMenuAction<IProject> {

	public ProjectMenuAction() {
		super(IProject.class);
	}

	@Override
	protected IProject convertoToIProject(IProject object) {
		return object;
	}

}
