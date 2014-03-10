package br.ufmg.dcc.labsoft.jextract.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

import br.ufmg.dcc.labsoft.jextract.evaluation.ProjectInliner;
import br.ufmg.dcc.labsoft.jextract.evaluation.ProjectRelevantSet;
import br.ufmg.dcc.labsoft.jextract.generation.NonSequentialEmrGenerator;
import br.ufmg.dcc.labsoft.jextract.generation.Settings;
import br.ufmg.dcc.labsoft.jextract.generation.SimpleEmrGenerator;
import br.ufmg.dcc.labsoft.jextract.ranking.EmrFileExporter;
import br.ufmg.dcc.labsoft.jextract.ranking.EmrFileReader;
import br.ufmg.dcc.labsoft.jextract.ranking.ExtractMethodRecomendation;
import br.ufmg.dcc.labsoft.jextract.ranking.JavaProjectAnalyser;

public class ProjectMenuAction extends ObjectMenuAction<IProject> {

	public ProjectMenuAction() {
		super(IProject.class);
	}

	@Override
	void handleAction(IAction action, IProject project) throws Exception {
		// MessageDialog.openInformation(shell, "JExtract", actionId);
		String actionId = action.getId();
		if (actionId.equals("br.ufmg.dcc.labsoft.jextract.inlineMethods")) {
			new ProjectInliner().run(project);
			MessageDialog.openInformation(this.getShell(), "JExtract", "Inline methods complete.");
			return;
		}

		if (actionId.equals("br.ufmg.dcc.labsoft.jextract.extractGoldSet")) {
			List<ExtractMethodRecomendation> emrList = new ProjectInliner().extractGoldSet(project);
			EmrFileExporter exporter = new EmrFileExporter(emrList, project.getLocation().toString() + "/goldset.txt");
			exporter.export();
			MessageDialog.openInformation(this.getShell(), "JExtract", "Gold set extracted.");
			return;
		}

		List<ExtractMethodRecomendation> recomendations;
		if (actionId.equals("br.ufmg.dcc.labsoft.jextract.importEmr")) {
			recomendations = importFromFile(project);
			JavaProjectAnalyser analyser = new JavaProjectAnalyser(recomendations, false);
			analyser.analyseProject(project);
		} else if (actionId.equals("br.ufmg.dcc.labsoft.jextract.showGoldset")) {
			EmrFileReader reader = new EmrFileReader();
			recomendations = reader.read(project.getLocation().toString() + "/goldset.txt");
			// Sort the recomendations.
			JavaProjectAnalyser analyser = new JavaProjectAnalyser(recomendations, true);
			analyser.analyseProject(project);
		} else if (actionId.equals("br.ufmg.dcc.labsoft.jextract.findEmr")) {
			recomendations = findEmr(project, null);
		} else if (actionId.equals("br.ufmg.dcc.labsoft.jextract.evaluate")) {
			ProjectRelevantSet goldset = new ProjectRelevantSet(project.getLocation().toString() + "/goldset.txt");
			recomendations = findEmr(project, goldset);
		} else {
			recomendations = Collections.emptyList();
		}

		IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		ExtractMethodRecomendationsView view = (ExtractMethodRecomendationsView) activePage
		        .showView("br.ufmg.dcc.labsoft.jextract.ui.ExtractMethodRecomendationsView");
		view.setRecomendations(recomendations, project);

		if (recomendations.isEmpty()) {
			MessageDialog.openInformation(this.getShell(), "JExtract", "No recomendations found.");
		}

	}

	private List<ExtractMethodRecomendation> importFromFile(IProject project) throws Exception {
		FileDialog fileDialog = new FileDialog(this.getShell());
		fileDialog.setText("Select File");
		// Set filter on .txt files
		fileDialog.setFilterExtensions(new String[] { "*.txt" });
		// Put in a readable name for the filter
		fileDialog.setFilterNames(new String[] { "Textfiles(*.txt)" });
		// Open Dialog and save result of selection
		String selected = fileDialog.open();

		EmrFileReader reader = new EmrFileReader();
		List<ExtractMethodRecomendation> recomendations = reader.read(selected);

		return recomendations;
	}

	private List<ExtractMethodRecomendation> findEmr(IProject project, ProjectRelevantSet goldset) throws Exception {
		EmrSettingsDialog dialog = new EmrSettingsDialog(this.getShell());
		if (dialog.open() == Window.OK) {
			Settings settings = dialog.getSettings();
			// int k = dialog.getFirstK();

			List<ExtractMethodRecomendation> recomendations = new ArrayList<ExtractMethodRecomendation>();
			//SimpleEmrGenerator analyser = new SimpleEmrGenerator(recomendations, minSize);
			SimpleEmrGenerator analyser = new NonSequentialEmrGenerator(recomendations, settings);
			analyser.setGoldset(goldset);
			analyser.generateRecomendations(project);

			// List<ExtractMethodRecomendation> filtered =
			// Utils.filterSameMethod(recomendations, k);
			return recomendations;
		}

		return Collections.emptyList();
	}

}
