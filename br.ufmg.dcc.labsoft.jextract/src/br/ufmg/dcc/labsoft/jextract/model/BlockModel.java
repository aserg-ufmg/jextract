package br.ufmg.dcc.labsoft.jextract.model;

import java.util.List;

public interface BlockModel {

	int getIndex();

	List<? extends StatementModel> getChildren();

	StatementModel get(int index);

	int getTotalSize();

}
