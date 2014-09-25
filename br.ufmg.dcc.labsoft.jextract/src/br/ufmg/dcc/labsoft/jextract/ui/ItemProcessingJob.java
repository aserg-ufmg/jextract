package br.ufmg.dcc.labsoft.jextract.ui;

import java.util.List;

public interface ItemProcessingJob<T> {

	List<T> getItems();

	void processItem(T item) throws Exception;

	void updateUI() throws Exception;
}
