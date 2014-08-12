package br.ufmg.dcc.labsoft.jextract.ui;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;

import br.ufmg.dcc.labsoft.jextract.generation.EmrGenerator;
import br.ufmg.dcc.labsoft.jextract.generation.Settings;
import br.ufmg.dcc.labsoft.jextract.ranking.ExtractMethodRecomendation;

public class ProjectMenuAction extends ObjectMenuAction<IProject> {

	public ProjectMenuAction() {
		super(IProject.class);
	}

	@Override
	public void handleAction(IAction action, List<IProject> projects) throws Exception {
		String actionId = action.getId();
		final IProject project = projects.get(0);
		if (actionId.equals("br.ufmg.dcc.labsoft.jextract.findEmr")) {
			findEmr(project);
		}
	}

	private void findEmr(final IProject project) throws Exception {
		EmrSettingsDialog dialog = new EmrSettingsDialog(this.getShell());
		if (dialog.open() == Window.OK) {
			final Settings settings = dialog.getSettings();
			AbstractJob job = new AbstractJob("Generating Recommendations") {
				@Override
				protected void doWorkIteration(int i, IProgressMonitor monitor) throws Exception {
					final List<ExtractMethodRecomendation> recomendations = new ArrayList<ExtractMethodRecomendation>();
					final EmrGenerator generator = new EmrGenerator(recomendations, settings);
					generator.generateRecomendations(project);
					
					Display.getDefault().asyncExec(new Runnable() {
						@Override
                        public void run() {
							try {
								showResultView(recomendations, project, settings);
							} catch (Exception e) {
								throw new RuntimeException(e);
							}
						}
					});
				}
			};
			job.schedule();
		}
	}

}
