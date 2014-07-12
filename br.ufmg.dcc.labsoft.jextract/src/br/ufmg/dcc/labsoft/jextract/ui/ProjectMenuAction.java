package br.ufmg.dcc.labsoft.jextract.ui;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;

import br.ufmg.dcc.labsoft.jextract.evaluation.Database;
import br.ufmg.dcc.labsoft.jextract.evaluation.JDeodorantEmrEvaluator;
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
				new ProjectInliner(project).inlineMethods();
			}
			MessageDialog.openInformation(this.getShell(), "JExtract", "Inline methods complete.");
			return;
		}
		if (actionId.equals("br.ufmg.dcc.labsoft.jextract.rewriteVisibility")) {
			for (IProject project : projects) {
				new ProjectInliner(project).rewriteVisibility();
			}
			MessageDialog.openInformation(this.getShell(), "JExtract", "Rewrite Visibility Complete.");
			return;
		}

		if (actionId.equals("br.ufmg.dcc.labsoft.jextract.extractGoldSet")) {
			for (IProject project : projects) {
				new ProjectInliner(project).extractGoldSet();
			}
			MessageDialog.openInformation(this.getShell(), "JExtract", "Gold set extracted.");
			return;
		}

		if (actionId.equals("br.ufmg.dcc.labsoft.jextract.evaluate")) {
			List<Settings> settingsList = this.getSettingsListCoef();
			//List<Settings> settingsList = this.getSettingsList2();
			//List<Settings> settingsList = this.getCanonicalSettings();
			//List<Settings> settingsList = this.getFullSettings();
			//List<Settings> settingsList = this.getSettingsWeights();
			//settingsList.addAll(c);
			settingsList.addAll(this.getSettingsWeights());
			evaluateEmr(projects, settingsList);
			return;
		}
		if (actionId.equals("br.ufmg.dcc.labsoft.jextract.evaluateJdeodorant")) {
			evaluateJdeodorant(projects);
			return;
		}
		
		final IProject project = projects.get(0);
		if (actionId.equals("br.ufmg.dcc.labsoft.jextract.showGoldset")) {
			EmrFileReader reader = new EmrFileReader();
			List<ExtractMethodRecomendation> recomendations = reader.read(project.getLocation().toString() + "/goldset.txt");
			
			fillEmrData(recomendations, project);
			
//			Database db = Database.getInstance();
//			try {
//				for (ExtractMethodRecomendation emr : recomendations) {
//					Boolean sameClass = sameClassMap.get(emr.getMethodBindingKey());
//					emr.getExtractedSize();
//					db.insertKnownEmi(this.project.getName(), emr.getFilePath(), emr.getMethodBindingKey(), emr.getExtractionSlice().toString(), emr.getOriginalSize(), sameClass, emr.getExtractedSize());
//				}
//			} finally {
//				db.close();
//			}
			
			showResultView(recomendations, project, new Settings());
			return;
		}

		if (actionId.equals("br.ufmg.dcc.labsoft.jextract.findEmr")) {
			findEmr(project);
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

	private List<Settings> getCanonicalSettings() {
		List<Settings> list = new ArrayList<Settings>();
		
		Settings kul = new Settings("main2");
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
	
	private List<Settings> getSettingsListCoef() {
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
	
	private List<Settings> getSettingsList2() {
		List<Settings> list = new ArrayList<Settings>();
		eachCoef(list, 1, 0, 0);
		eachCoef(list, 0, 1, 0);
		eachCoef(list, 0, 0, 1);
		eachCoef(list, 1, 1, 0);
		eachCoef(list, 1, 0, 1);
		eachCoef(list, 1, 1, 1);
		eachCoef(list, 4, 3, 2);
		eachCoef(list, 1, 2, 3);
		eachCoef(list, 2, 1, 1);
	    return list;
    }
	
	private void eachCoef(List<Settings> settings, int wv, int wt, int wp) {
		String weights = String.format("%d-%d-%d", wv, wt, wp);
		for (Coefficient c : Coefficient.values()) {
			Settings s = new Settings(c.toString().toLowerCase() + " " + weights);
			s.wV = wv;
			s.wT = wt;
			s.wP = wp;
			s.setCoefficient(c);
			settings.add(s);
		}
	}

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
					EmrGenerator generator = new EmrGenerator(recomendations, settings);
					generator.setGoldset(project, goldset, db);
					ExecutionReport rep = generator.generateRecomendations(project);
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

	private void evaluateJdeodorant(List<IProject> projects) throws Exception {
		Database db = Database.getInstance();
		try {
			List<ExtractMethodRecomendation> recomendations = new ArrayList<ExtractMethodRecomendation>();
			//Settings settings = dialog.getSettings();
			Settings settings = new Settings("JDeodorant");
			AggregatedExecutionReport arep = new AggregatedExecutionReport(settings);
			for (IProject project : projects) {
				ProjectRelevantSet goldset = new ProjectRelevantSet(project.getLocation().toString() + "/goldset.txt");
				JDeodorantEmrEvaluator evaluator = new JDeodorantEmrEvaluator(settings, project, goldset, db);
				ExecutionReport rep = evaluator.evaluateResults(recomendations);
				arep.merge(rep);
			}
			//arep.printReport();
			arep.printSummary();
			if (projects.size() == 1) {
				showResultView(recomendations, projects.get(0), settings);
			}
		} finally {
			db.close();
		}
		
		MessageDialog.openInformation(this.getShell(), "JExtract", "JDeodorant evaluation complete.");
	}

}
