package br.ufmg.dcc.labsoft.jextract.model;

import br.ufmg.dcc.labsoft.jextract.ranking.ExtractionSlice;

public interface OffsetBasedEmrDescriptor {

	String getFilePath();

	String getMethodBindingKey();

	ExtractionSlice getExtractionSlice();

}
