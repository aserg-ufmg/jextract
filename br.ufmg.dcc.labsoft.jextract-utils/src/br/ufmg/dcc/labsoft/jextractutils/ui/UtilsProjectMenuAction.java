package br.ufmg.dcc.labsoft.jextractutils.ui;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.PartInitException;

import br.ufmg.dcc.labsoft.jextract.codeanalysis.Utils;
import br.ufmg.dcc.labsoft.jextract.generation.EmrGenerator;
import br.ufmg.dcc.labsoft.jextract.generation.Settings;
import br.ufmg.dcc.labsoft.jextract.ranking.Coefficient;
import br.ufmg.dcc.labsoft.jextract.ranking.EmrFileReader;
import br.ufmg.dcc.labsoft.jextract.ranking.EmrRankFileExporter;
import br.ufmg.dcc.labsoft.jextract.ranking.ExtractMethodRecomendation;
import br.ufmg.dcc.labsoft.jextract.ui.EmrSettingsDialog;
import br.ufmg.dcc.labsoft.jextract.ui.ItemProcessingJob;
import br.ufmg.dcc.labsoft.jextract.ui.JobRunner;
import br.ufmg.dcc.labsoft.jextract.ui.ObjectMenuAction;
import br.ufmg.dcc.labsoft.jextractutils.evaluation.AggregatedExecutionReport;
import br.ufmg.dcc.labsoft.jextractutils.evaluation.Database;
import br.ufmg.dcc.labsoft.jextractutils.evaluation.ExecutionReport;
import br.ufmg.dcc.labsoft.jextractutils.evaluation.FileEmrEvaluator;
import br.ufmg.dcc.labsoft.jextractutils.evaluation.ProjectInliner;
import br.ufmg.dcc.labsoft.jextractutils.evaluation.ProjectRelevantSet;
import br.ufmg.dcc.labsoft.jextractutils.evaluation.UtilsEmrGenerator;

public class UtilsProjectMenuAction extends ObjectMenuAction<IProject> {

	public UtilsProjectMenuAction() {
		super(IProject.class);
	}

	@Override
	protected void showResultView(List<ExtractMethodRecomendation> recomendations, IProject project, Settings settings) throws PartInitException {
		ProjectRelevantSet set = new ProjectRelevantSet(project.getLocation().toString() + "/goldset.txt");
		for (ExtractMethodRecomendation rec : recomendations) {
			rec.setRelevant(set.contains(rec));
			rec.setSimilar(set.containsReduced(rec));
			rec.setAvailableInGoldSet(set.isMethodAvailable(rec));
		}
		super.showResultView(recomendations, project, settings);
	}

	@Override
	public void handleAction(IAction action, List<IProject> projects) throws Exception {
		String actionId = action.getId();
		if (actionId.equals("br.ufmg.dcc.labsoft.jextractutils.inlineMethods")) {
			for (IProject project : projects) {
				new ProjectInliner(project).inlineMethods();
			}
			MessageDialog.openInformation(this.getShell(), "JExtract", "Inline methods complete.");
			return;
		}
		if (actionId.equals("br.ufmg.dcc.labsoft.jextractutils.rewriteVisibility")) {
			for (IProject project : projects) {
				new ProjectInliner(project).rewriteVisibility();
			}
			MessageDialog.openInformation(this.getShell(), "JExtract", "Rewrite Visibility Complete.");
			return;
		}

		if (actionId.equals("br.ufmg.dcc.labsoft.jextractutils.extractGoldSet")) {
			for (IProject project : projects) {
				new ProjectInliner(project).extractGoldSet();
			}
			MessageDialog.openInformation(this.getShell(), "JExtract", "Gold set extracted.");
			return;
		}

		if (actionId.equals("br.ufmg.dcc.labsoft.jextractutils.evaluate")) {
			List<Settings> settingsList = this.getDefaultSettings();
			//List<Settings> settingsList = this.getFullSettings();
			//List<Settings> settingsList = this.getSettingsCoefficients();
			//List<Settings> settingsList = this.getSettingsWeights();
			evaluateEmr(projects, settingsList);
			return;
		}
		if (actionId.equals("br.ufmg.dcc.labsoft.jextractutils.evaluateFromFile")) {
			evaluateFromFile(projects);
			return;
		}
		if (actionId.equals("br.ufmg.dcc.labsoft.jextractutils.exportData")) {
			//List<Settings> settingsList = this.getFullSettings();
			//List<Settings> settingsList = this.getSettingsCoefficients();
			//List<Settings> settingsList = this.getSettingsWeights();
			exportData(projects);
			return;
		}
		
		final IProject project = projects.get(0);
		if (actionId.equals("br.ufmg.dcc.labsoft.jextractutils.showGoldset")) {
			EmrFileReader reader = new EmrFileReader();
			List<ExtractMethodRecomendation> recomendations = reader.read(project.getLocation().toString() + "/goldset.txt");
			fillEmrData(recomendations, project);
			showResultView(recomendations, project, new Settings());
			return;
		}
	}

	private void fillEmrData(List<ExtractMethodRecomendation> recomendations, IProject project) throws CoreException {
		IJavaProject javaProject = JavaCore.create(project);
		for (ExtractMethodRecomendation rec : recomendations) {
			IType findType = javaProject.findType(rec.className);
			ICompilationUnit icu = findType.getCompilationUnit();
			rec.setSourceFile(icu);
		}
    }

	private List<Settings> getDefaultSettings() {
		List<Settings> list = new ArrayList<Settings>();
		Settings kul = new Settings("default");
		kul.setMaxPerMethod(3);
		kul.setCoefficient(Coefficient.KUL);
		list.add(kul);
	    return list;
    }

	private List<Settings> getFullSettings() {
		List<Settings> list = new ArrayList<Settings>();
		Settings kul = new Settings("full");
		kul.setMaxPerMethod(2048 * 4);
		kul.setCoefficient(Coefficient.KUL);
		list.add(kul);
	    return list;
    }
	
	private List<Settings> getSettingsCoefficients() {
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
	
//	private void eachCoef(List<Settings> settings, int wv, int wt, int wp) {
//		String weights = String.format("%d-%d-%d", wv, wt, wp);
//		for (Coefficient c : Coefficient.values()) {
//			Settings s = new Settings(c.toString().toLowerCase() + " " + weights);
//			s.wV = wv;
//			s.wT = wt;
//			s.wP = wp;
//			s.setCoefficient(c);
//			settings.add(s);
//		}
//	}

	private List<Settings> getSettingsWeights() {
		List<Settings> list = new ArrayList<Settings>();
		oneCoef(list, 1, 0, 0);
		oneCoef(list, 0, 1, 0);
		oneCoef(list, 0, 0, 1);
		oneCoef(list, 1, 1, 0);
		oneCoef(list, 1, 0, 1);
		oneCoef(list, 1, 1, 1);
		oneCoef(list, 9, 6, 4);
		oneCoef(list, 100, 10, 1);
		oneCoef(list, 4, 6, 9);
		oneCoef(list, 1, 10, 100);
		oneCoef(list, 2, 1, 1);
		oneCoef(list, 1, 2, 1);
		oneCoef(list, 1, 1, 2);
	    return list;
    }

	private void oneCoef(List<Settings> settings, int wv, int wt, int wp) {
		String weights = String.format("%d-%d-%d", wv, wt, wp);
		Coefficient c = Coefficient.KUL;
		Settings s = new Settings(c.toString().toLowerCase() + " " + weights);
		s.wV = wv;
		s.wT = wt;
		s.wP = wp;
		s.setCoefficient(c);
		settings.add(s);
	}
	
	private void findEmr(final IProject project) throws Exception {
		EmrSettingsDialog dialog = new EmrSettingsDialog(this.getShell());
		if (dialog.open() == Window.OK) {
			final Settings settings = dialog.getSettings();
			final List<ExtractMethodRecomendation> recomendations = new ArrayList<ExtractMethodRecomendation>();
			final EmrGenerator generator = new EmrGenerator(recomendations, settings);
			
			JobRunner.run("Generating Recommendations", new ItemProcessingJob<ICompilationUnit>() {
				@Override
				public List<ICompilationUnit> getItems() {
					return Utils.findJavaResources(project);
				}
				@Override
				public void processItem(ICompilationUnit item) throws Exception {
					generator.generateRecomendations(item);
				}
				@Override
				public void updateUI() throws Exception {
					showResultView(recomendations, project, settings);
				}
			});
		}
	}

	private void evaluateEmr(List<IProject> projects, List<Settings> settingsList) throws Exception {
		//Database db = new DatabaseImpl();
		Database db = Database.getInstance();
		try {
			for (Settings settings : settingsList) {
				List<ExtractMethodRecomendation> recomendations = new ArrayList<ExtractMethodRecomendation>();
				//Settings settings = dialog.getSettings();
				AggregatedExecutionReport arep = new AggregatedExecutionReport(settings);
				for (IProject project : projects) {
					ProjectRelevantSet goldset = new ProjectRelevantSet(project.getLocation().toString() + "/goldset.txt");
					recomendations = new ArrayList<ExtractMethodRecomendation>();
					UtilsEmrGenerator generator = new UtilsEmrGenerator(recomendations, settings);
					generator.setOracle(project, goldset, db);
					//ExecutionReport rep = generator.generateRecomendations(project);
					ExecutionReport rep = generator.evaluateRecomendations(project);
					arep.merge(rep);
				}
				arep.printReport();
				//arep.printSummary();
				if (projects.size() == 1 && settingsList.size() == 1) {
					showResultView(recomendations, projects.get(0), settings);
				}
			}
		} finally {
			db.close();
		}
		
		MessageDialog.openInformation(this.getShell(), "JExtract", "Evaluation complete.");
	}

	private void exportData(List<IProject> projects) throws Exception {
		//Database db = new FakeDatabase();
		//Database db = Database.getInstance();
		List<Settings> settingsList = new ArrayList<Settings>();
		Settings s = new Settings("default");
		s.setMaxPerMethod(1);
		settingsList.add(s);
		
		try {
			for (Settings settings : settingsList) {
				List<ExtractMethodRecomendation> allRecomendations = new ArrayList<ExtractMethodRecomendation>();
				//Settings settings = dialog.getSettings();
				//AggregatedExecutionReport arep = new AggregatedExecutionReport(settings);
				for (IProject project : projects) {
					//ProjectRelevantSet goldset = new ProjectRelevantSet(project.getLocation().toString() + "/goldset.txt");
					List<ExtractMethodRecomendation> recomendations = new ArrayList<ExtractMethodRecomendation>();
					UtilsEmrGenerator generator = new UtilsEmrGenerator(recomendations, settings);
					//generator.setOracle(project, goldset, db);
					//ExecutionReport rep = generator.generateRecomendations(project);
					generator.generateRecomendations(project);
					//arep.merge(rep);
					allRecomendations.addAll(recomendations);
					String projectName = project.getName();
					for (ExtractMethodRecomendation rec : recomendations) {
						rec.setProject(projectName);
					}
				}
				br.ufmg.dcc.labsoft.jextract.ranking.Utils.sort(allRecomendations, false);
				
				String outputFile = projects.get(0).getLocation().toString() + "/../out.txt";
				EmrRankFileExporter exporter = new EmrRankFileExporter(allRecomendations, outputFile);
				exporter.export();
				
				//arep.printReport();
				//arep.printSummary();
//				if (projects.size() == 1 && settingsList.size() == 1) {
//					showResultView(allRecomendations, projects.get(0), settings);
//				}
			}
		} finally {
			//db.close();
		}
		
		MessageDialog.openInformation(this.getShell(), "JExtract", "Export data complete.");
	}

	private void evaluateFromFile(List<IProject> projects) throws Exception {
		Database db = Database.getInstance();
		try {
			List<ExtractMethodRecomendation> recomendations = new ArrayList<ExtractMethodRecomendation>();
			//Settings settings = dialog.getSettings();
			Settings settings = new Settings("JDeodorant");
			AggregatedExecutionReport arep = new AggregatedExecutionReport(settings);
			for (IProject project : projects) {
				ProjectRelevantSet goldset = new ProjectRelevantSet(project.getLocation().toString() + "/goldset.txt");
				FileEmrEvaluator evaluator = new FileEmrEvaluator(settings, project, goldset, db);
				ExecutionReport rep = evaluator.evaluateResults(recomendations);
				arep.merge(rep);
			}
			arep.printReport();
			//arep.printSummary();
			if (projects.size() == 1) {
				showResultView(recomendations, projects.get(0), settings);
			}
		} finally {
			db.close();
		}
		
		MessageDialog.openInformation(this.getShell(), "JExtract", "Evaluation complete.");
	}

}
