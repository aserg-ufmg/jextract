package br.ufmg.dcc.labsoft.jextract.model;


public interface StatementBasedEmrDescriptor {

	String getFilePath();

	String getMethodBindingKey();

	int getBlockIndex();

	int getBlockLength();

	Placement getPlacement(int i);

}
