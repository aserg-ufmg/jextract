package br.ufmg.dcc.labsoft.jextract.ui;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import br.ufmg.dcc.labsoft.jextract.generation.Settings;

public class EmrSettingsDialog extends Dialog {
	private Text txtMinSize;
	private Text txtMaxPerMethod;
	private Text txtMaxFragments;
	private Text txtPenalty;
	private Text txtMinScore;
	private final Settings settings;

	public EmrSettingsDialog(Shell parentShell) {
		super(parentShell);
		this.settings = new Settings();
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		GridLayout layout = new GridLayout(2, false);
		layout.marginRight = 5;
		layout.marginLeft = 10;
		container.setLayout(layout);

		this.txtMinSize = this.createTextField(container, "Minimum extracted statements:", this.settings.getMinSize().toString());
		this.txtMaxPerMethod = this.createTextField(container, "Maximum recommendations per method:", this.settings.getMaxPerMethod().toString());
		this.txtMaxFragments = this.createTextField(container, "Maximum extraction fragments:", this.settings.getMaxFragments().toString());
		this.txtPenalty = this.createTextField(container, "Statement reordering penalty:", this.settings.getPenalty().toString());
		this.txtMinScore = this.createTextField(container, "Minimum score value:", this.settings.getMinScore().toString());
		
		return container;
	}

	private Text createTextField(Composite container, String label, String initialValue) {
	    Label labelWidget = new Label(container, SWT.NONE);
		labelWidget.setText(label);

		Text textField = new Text(container, SWT.BORDER);
		textField.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		textField.setText(initialValue);
		return textField;
    }

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL,
				true);
		createButton(parent, IDialogConstants.CANCEL_ID,
				IDialogConstants.CANCEL_LABEL, false);
	}

	@Override
	protected Point getInitialSize() {
		return new Point(450, 300);
	}

	@Override
	protected void okPressed() {
		try {
			this.settings.setMinSize(Integer.valueOf(txtMinSize.getText()));
			this.settings.setMaxPerMethod(Integer.valueOf(txtMaxPerMethod.getText()));
			this.settings.setMaxFragments(Integer.valueOf(txtMaxFragments.getText()));
			this.settings.setPenalty(Double.valueOf(txtPenalty.getText()));
			this.settings.setMinScore(Double.valueOf(txtMinScore.getText()));
		} catch (NumberFormatException e) {}
//		try {
//			maxRatio = Double.valueOf(txtMaxRatio.getText());
//		} catch (NumberFormatException e) {}
		super.okPressed();
	}

	public Settings getSettings() {
	    return this.settings;
    }

}
