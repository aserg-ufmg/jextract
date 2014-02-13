package br.ufmg.dcc.labsoft.jextract.ui;

import br.ufmg.dcc.labsoft.jextract.ranking.ExtractMethodRecomendation;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;

abstract class EmrTableColumnLabelProvider extends ColumnLabelProvider {
	
	@Override
	public String getText(Object element) {
		return this.getColumnText((ExtractMethodRecomendation) element);
	}

	public abstract String getColumnText(ExtractMethodRecomendation element);

	@Override
	public Color getForeground(Object element) {
		ExtractMethodRecomendation rec = (ExtractMethodRecomendation) element;
		return
			rec.isRelevant() ? Display.getDefault().getSystemColor(SWT.COLOR_DARK_GREEN) :
			rec.isSimilar() ? Display.getDefault().getSystemColor(SWT.COLOR_DARK_YELLOW) :
			super.getForeground(element);
	}
	
}

class IdLabelProvider extends EmrTableColumnLabelProvider {
	@Override
	public String getColumnText(ExtractMethodRecomendation element) {
		// TODO Auto-generated method stub
		return null;
	}
}