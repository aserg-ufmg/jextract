package br.ufmg.dcc.labsoft.jextract.model;

import java.util.List;

public interface BlockModel {

	int getIndex();

	List<? extends StatementModel> getChildren();

	StatementModel get(int index);

	int getTotalSize();

	/**
	 * @param i Index of statement i in the block.
	 * @param j Index of statement j in the block.
	 * @return Returns true if statement i depends on statement j.
	 */
	boolean depends(int i, int j);

}
