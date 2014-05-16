package br.ufmg.dcc.labsoft.jextract.ui;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

abstract class ObjectMenuAction<T> implements IObjectActionDelegate {

	private Shell shell;

	private List<T> selected;

	private final Class<T> objectClass;

	/**
	 * @param objectClass
	 */
	public ObjectMenuAction(Class<T> objectClass) {
		super();
		this.objectClass = objectClass;
	}

	@Override
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		this.shell = targetPart.getSite().getShell();
	}

	public Shell getShell() {
		return this.shell;
	}

	/**
	 * @see IActionDelegate#run(IAction)
	 */
	public void run(IAction action) {
		if (this.selected != null) {
			try {
				this.handleAction(action, this.selected);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		this.selected = new ArrayList<T>();
		if (this.objectClass.isInstance(selection)) {
			this.selected.add(this.objectClass.cast(selection));
		} else if (selection instanceof IStructuredSelection) {
			IStructuredSelection structuredSelection = (IStructuredSelection) selection;
			for (Object element : structuredSelection.toList()) {
				if (this.objectClass.isInstance(element)) {
					this.selected.add(this.objectClass.cast(element));
				}
			}
		}
	}

	abstract void handleAction(IAction action, List<T> item) throws Exception;

}
