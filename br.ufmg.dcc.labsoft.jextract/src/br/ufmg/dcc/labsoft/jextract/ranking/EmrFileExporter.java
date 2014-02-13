package br.ufmg.dcc.labsoft.jextract.ranking;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

public class EmrFileExporter {

	private final Iterable<ExtractMethodRecomendation> data;
	private final File outputFile;
	
	public EmrFileExporter(Iterable<ExtractMethodRecomendation> data, String outputFile) {
		this.data = data;
		this.outputFile = new File(outputFile);
	}

	public void export() {
		try {
			PrintWriter writer = new PrintWriter(this.outputFile);
			try {
				for (ExtractMethodRecomendation rec : this.data) {
					writer.println(rec);
				}
			} 
			finally {
				writer.close();
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
