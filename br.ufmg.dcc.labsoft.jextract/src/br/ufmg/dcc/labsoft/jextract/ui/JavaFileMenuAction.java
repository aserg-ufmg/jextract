package br.ufmg.dcc.labsoft.jextract.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.window.Window;

import br.ufmg.dcc.labsoft.jextract.generation.EmrGenerator;
import br.ufmg.dcc.labsoft.jextract.generation.Settings;
import br.ufmg.dcc.labsoft.jextract.ranking.ExtractMethodRecomendation;

public class JavaFileMenuAction extends ObjectMenuAction<ICompilationUnit> {

	public JavaFileMenuAction() {
		super(ICompilationUnit.class);
	}

	@Override
	public void handleAction(IAction action, List<ICompilationUnit> icus) throws Exception {
		String actionId = action.getId();
		final ICompilationUnit icu = icus.get(0);
		if (actionId.equals("br.ufmg.dcc.labsoft.jextract.findEmr")) {
			findEmr(icu);
		}
	}

	private void findEmr(final ICompilationUnit icu) throws Exception {
		EmrSettingsDialog dialog = new EmrSettingsDialog(this.getShell());
		if (dialog.open() == Window.OK) {
			final Settings settings = dialog.getSettings();
			final List<ExtractMethodRecomendation> recomendations = new ArrayList<ExtractMethodRecomendation>();
			final EmrGenerator generator = new EmrGenerator(recomendations, settings);
			
			JobRunner.run("Finding Recommendations", new ItemProcessingJob<ICompilationUnit>(){
				public List<ICompilationUnit> getItems(){
					return Collections.singletonList(icu);
				}
				public void processItem(ICompilationUnit icu) throws Exception {
					generator.generateRecomendations(icu);
				}
				public void updateUI() throws Exception {
					showResultView(recomendations, icu.getJavaProject().getProject(), settings);
				}
			});
		}
	}

}
