package br.ufmg.dcc.labsoft.jextract.ui;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;

import br.ufmg.dcc.labsoft.jextract.evaluation.ProjectInliner;
import br.ufmg.dcc.labsoft.jextract.evaluation.ProjectRelevantSet;
import br.ufmg.dcc.labsoft.jextract.generation.EmrGenerator;
import br.ufmg.dcc.labsoft.jextract.generation.Settings;
import br.ufmg.dcc.labsoft.jextract.ranking.EmrFileReader;
import br.ufmg.dcc.labsoft.jextract.ranking.ExtractMethodRecomendation;

public class ProjectMenuAction extends ObjectMenuAction<IProject> {

	public ProjectMenuAction() {
		super(IProject.class);
	}

	@Override
	void handleAction(IAction action, List<IProject> projects) throws Exception {
		String actionId = action.getId();
		if (actionId.equals("br.ufmg.dcc.labsoft.jextract.inlineMethods")) {
			for (IProject project : projects) {
				new ProjectInliner().run(project);
			}
			MessageDialog.openInformation(this.getShell(), "JExtract", "Inline methods complete.");
			return;
		}

		if (actionId.equals("br.ufmg.dcc.labsoft.jextract.extractGoldSet")) {
			for (IProject project : projects) {
				new ProjectInliner().extractGoldSet(project);
			}
			MessageDialog.openInformation(this.getShell(), "JExtract", "Gold set extracted.");
			return;
		}

		if (actionId.equals("br.ufmg.dcc.labsoft.jextract.evaluate")) {
			evaluateEmr(projects);
			return;
		}
		
		IProject project = projects.get(0);
		if (actionId.equals("br.ufmg.dcc.labsoft.jextract.showGoldset")) {
			EmrFileReader reader = new EmrFileReader();
			List<ExtractMethodRecomendation> recomendations = reader.read(project.getLocation().toString() + "/goldset.txt");
			//fillEmrData(recomendations, project);
			showResultView(recomendations, project, new Settings());
			return;
		}

		if (actionId.equals("br.ufmg.dcc.labsoft.jextract.findEmr")) {
			findEmr(project, null);
			return;
		}
	}


	private void findEmr(IProject project, ProjectRelevantSet goldset) throws Exception {
		EmrSettingsDialog dialog = new EmrSettingsDialog(this.getShell());
		if (dialog.open() == Window.OK) {
			Settings settings = dialog.getSettings();
			List<ExtractMethodRecomendation> recomendations = new ArrayList<ExtractMethodRecomendation>();
			EmrGenerator generator = new EmrGenerator(recomendations, settings);
			generator.setGoldset(goldset);
			generator.generateRecomendations(project);
			showResultView(recomendations, project, settings);
		}
	}

	private void evaluateEmr(List<IProject> projects) throws Exception {
		List<ExtractMethodRecomendation> recomendations = new ArrayList<ExtractMethodRecomendation>();
		EmrSettingsDialog dialog = new EmrSettingsDialog(this.getShell());
		if (dialog.open() == Window.OK) {
			Settings settings = dialog.getSettings();
			for (IProject project : projects) {
				ProjectRelevantSet goldset = new ProjectRelevantSet(project.getLocation().toString() + "/goldset.txt");
				recomendations = new ArrayList<ExtractMethodRecomendation>();
				EmrGenerator generator = new EmrGenerator(recomendations, settings);
				generator.setGoldset(goldset);
				generator.generateRecomendations(project);
			}
			
			if (projects.size() == 1) {
				showResultView(recomendations, projects.get(0), settings);
			} else {
				MessageDialog.openInformation(this.getShell(), "JExtract", "Evaluation complete.");
			}
			
		}
	}

}
