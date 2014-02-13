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

public class EmrSettingsDialog extends Dialog {
	private Text txtMinSize;
	private Text txtMaxRatio;
	private Integer minSize = 3;
	//private Double maxRatio = 0.8;

	public EmrSettingsDialog(Shell parentShell) {
		super(parentShell);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		GridLayout layout = new GridLayout(2, false);
		layout.marginRight = 5;
		layout.marginLeft = 10;
		container.setLayout(layout);

		Label lblUser = new Label(container, SWT.NONE);
		lblUser.setText("Minimum extracted statements:");

		txtMinSize = new Text(container, SWT.BORDER);
		txtMinSize.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false, 1, 1));
		txtMinSize.setText(minSize.toString());

//		Label lblPassword = new Label(container, SWT.NONE);
//		GridData gd_lblNewLabel = new GridData(SWT.LEFT, SWT.CENTER, false,
//				false, 1, 1);
//		gd_lblNewLabel.horizontalIndent = 1;
//		lblPassword.setLayoutData(gd_lblNewLabel);
//		lblPassword.setText("Maximum extracted/total ratio :");
//
//		txtMaxRatio = new Text(container, SWT.BORDER);
//		txtMaxRatio.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
//				false, 1, 1));
//		txtMaxRatio.setText(maxRatio.toString());
		return container;
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
			minSize = Integer.valueOf(txtMinSize.getText());
		} catch (NumberFormatException e) {}
//		try {
//			maxRatio = Double.valueOf(txtMaxRatio.getText());
//		} catch (NumberFormatException e) {}
		super.okPressed();
	}

	public Integer getMinSize() {
		return minSize;
	}

//	public int getFirstK() {
//		return 3;
//	}

	public void setMinSize(Integer minSize) {
		this.minSize = minSize;
	}

//	public Double getMaxRatio() {
//		return maxRatio;
//	}
//
//	public void setMaxRatio(Double maxRatio) {
//		this.maxRatio = maxRatio;
//	}

}
