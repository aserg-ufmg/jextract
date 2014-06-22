package br.ufmg.dcc.labsoft.jextract.ui;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;

import br.ufmg.dcc.labsoft.jextract.evaluation.ProjectInliner;
import br.ufmg.dcc.labsoft.jextract.evaluation.ProjectRelevantSet;
import br.ufmg.dcc.labsoft.jextract.generation.AggregatedExecutionReport;
import br.ufmg.dcc.labsoft.jextract.generation.EmrGenerator;
import br.ufmg.dcc.labsoft.jextract.generation.ExecutionReport;
import br.ufmg.dcc.labsoft.jextract.generation.Settings;
import br.ufmg.dcc.labsoft.jextract.ranking.Coefficient;
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
			//List<Settings> settingsList = this.getSettingsList();
			List<Settings> settingsList = this.getDefaultSettingsList();
			evaluateEmr(projects, settingsList);
			return;
		}
		
		final IProject project = projects.get(0);
		if (actionId.equals("br.ufmg.dcc.labsoft.jextract.showGoldset")) {
			EmrFileReader reader = new EmrFileReader();
			List<ExtractMethodRecomendation> recomendations = reader.read(project.getLocation().toString() + "/goldset.txt");
			//fillEmrData(recomendations, project);
			showResultView(recomendations, project, new Settings());
			return;
		}

		if (actionId.equals("br.ufmg.dcc.labsoft.jextract.findEmr")) {
			findEmr(project);
			return;
		}
	}


	private List<Settings> getDefaultSettingsList() {
		List<Settings> list = new ArrayList<Settings>();
		
		Settings kul = new Settings("kul");
		kul.setCoefficient(Coefficient.KUL);
		list.add(kul);
		
	    return list;
    }

	private List<Settings> getSettingsList() {
		List<Settings> list = new ArrayList<Settings>();
		
		Settings jac = new Settings("JAC");
		jac.setCoefficient(Coefficient.JAC);
		list.add(jac);
		Settings sor = new Settings("SOR");
		sor.setCoefficient(Coefficient.SOR);
		list.add(sor);
		Settings ss2 = new Settings("SS2");
		ss2.setCoefficient(Coefficient.SS2);
		list.add(ss2);
		Settings psc = new Settings("PSC");
		psc.setCoefficient(Coefficient.PSC);
		list.add(psc);
		Settings kul = new Settings("KUL");
		kul.setCoefficient(Coefficient.KUL);
		list.add(kul);
		Settings och = new Settings("OCH");
		och.setCoefficient(Coefficient.OCH);
		list.add(och);

	    return list;
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

	private void evaluateEmr(List<IProject> projects, List<Settings> settingsList) throws Exception {
		for (Settings settings : settingsList) {
			List<ExtractMethodRecomendation> recomendations = new ArrayList<ExtractMethodRecomendation>();
			//Settings settings = dialog.getSettings();
			AggregatedExecutionReport arep = new AggregatedExecutionReport(settings);
			for (IProject project : projects) {
				ProjectRelevantSet goldset = new ProjectRelevantSet(project.getLocation().toString() + "/goldset.txt");
				recomendations = new ArrayList<ExtractMethodRecomendation>();
				EmrGenerator generator = new EmrGenerator(recomendations, settings);
				generator.setGoldset(goldset);
				ExecutionReport rep = generator.generateRecomendations(project);
				arep.merge(rep);
			}
			arep.printReport();
			//arep.printSummary();
			if (projects.size() == 1 && settingsList.size() == 1) {
				showResultView(recomendations, projects.get(0), settings);
			}
		}
		MessageDialog.openInformation(this.getShell(), "JExtract", "Evaluation complete.");
	}

}
