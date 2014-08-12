package br.ufmg.dcc.labsoft.jextract.ui;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.window.Window;

import br.ufmg.dcc.labsoft.jextract.generation.EmrGenerator;
import br.ufmg.dcc.labsoft.jextract.generation.Settings;
import br.ufmg.dcc.labsoft.jextract.ranking.ExtractMethodRecomendation;

public class MethodMenuAction extends ObjectMenuAction<IMethod> {

	/**
	 * Constructor for Action1.
	 */
	public MethodMenuAction() {
		super(IMethod.class);
	}

	@Override
	public void handleAction(IAction action, List<IMethod> methods) throws Exception {
		IMethod method = methods.get(0);
		IProject project = method.getJavaProject().getProject();

		String actionId = action.getId();
		if (actionId.equals("br.ufmg.dcc.labsoft.jextract.methodmenu.findEmr")) {
			findEmr(project, method);
		}
	}

	private void findEmr(IProject project, IMethod method) throws Exception {
		EmrSettingsDialog dialog = new EmrSettingsDialog(this.getShell());
		if (dialog.open() == Window.OK) {
			Settings settings = dialog.getSettings();
			List<ExtractMethodRecomendation> recomendations = new ArrayList<ExtractMethodRecomendation>();
			EmrGenerator analyser = new EmrGenerator(recomendations, settings);
			analyser.generateRecomendations(method);
			this.showResultView(recomendations, project, settings);
		}
	}

}
