package br.ufmg.dcc.labsoft.jextract.ranking;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

public class EmrRankFileExporter {

	private final Iterable<ExtractMethodRecomendation> data;
	private final File outputFile;

	public EmrRankFileExporter(Iterable<ExtractMethodRecomendation> data, String outputFile) {
		this.data = data;
		this.outputFile = new File(outputFile);
	}

	public void export() {
		try {
			this.outputFile.getParentFile().mkdirs();
			PrintWriter writer = new PrintWriter(this.outputFile);
			try {
				for (ExtractMethodRecomendation rec : this.data) {
					try {
						if (rec.getProject() != null) {
							writer.print(rec.getProject());
							writer.print('\t');
						}
						writer.println(String.format("%s\t%s\t%s\t%d\t%d\t%s", rec.className, rec.method, rec.slice.toString(), rec.getOriginalSize(), rec.getExtractedSize(), Double.toString(rec.getScore())));
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
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
