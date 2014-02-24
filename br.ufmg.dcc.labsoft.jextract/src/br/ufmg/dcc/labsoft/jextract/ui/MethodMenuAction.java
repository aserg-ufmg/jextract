package br.ufmg.dcc.labsoft.jextract.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

import br.ufmg.dcc.labsoft.jextract.generation.SimpleEmrGenerator;
import br.ufmg.dcc.labsoft.jextract.ranking.ExtractMethodRecomendation;
import br.ufmg.dcc.labsoft.jextract.ranking.JavaProjectAnalyser;

public class MethodMenuAction extends ObjectMenuAction<IMethod> {

	/**
	 * Constructor for Action1.
	 */
	public MethodMenuAction() {
		super(IMethod.class);
	}

	@Override
	void handleAction(IAction action, IMethod method) throws Exception {
		// MessageDialog.openInformation(shell, "JExtract", actionId);
		IProject project = method.getJavaProject().getProject();

		List<ExtractMethodRecomendation> recomendations;
		String actionId = action.getId();
		if (actionId.equals("br.ufmg.dcc.labsoft.jextract.methodmenu.findEmr")) {
			recomendations = findEmr(method);
		} else {
			recomendations = new ArrayList<ExtractMethodRecomendation>();
		}

		// Sort the recomendations.
		JavaProjectAnalyser analyser = new JavaProjectAnalyser(recomendations, false);
		analyser.analyseMethod(method);

		IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		ExtractMethodRecomendationsView view = (ExtractMethodRecomendationsView) activePage
		        .showView("br.ufmg.dcc.labsoft.jextract.ui.ExtractMethodRecomendationsView");
		view.setRecomendations(recomendations, project);

		if (recomendations.isEmpty()) {
			MessageDialog.openInformation(this.getShell(), "JExtract", "No recomendations found.");
		}
	}

	private List<ExtractMethodRecomendation> findEmr(IMethod method) throws Exception {
		EmrSettingsDialog dialog = new EmrSettingsDialog(this.getShell());
		if (dialog.open() == Window.OK) {
			Integer minSize = dialog.getMinSize();
			// int k = dialog.getFirstK();

			List<ExtractMethodRecomendation> recomendations = new ArrayList<ExtractMethodRecomendation>();
			SimpleEmrGenerator analyser = new SimpleEmrGenerator(recomendations, minSize);
			//SimpleEmrGenerator analyser = new NonSequentialEmrGenerator(recomendations, minSize);
			analyser.generateRecomendations(method);

			// List<ExtractMethodRecomendation> filtered =
			// Utils.filterSameMethod(recomendations, k);
			return recomendations;
		}
		return Collections.emptyList();
	}

}
