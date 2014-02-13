package br.ufmg.dcc.labsoft.jextract.ranking;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

public class EmrRankFileExporter {

	private final Iterable<ExtractMethodRecomendation> data;
	private final EmrScoringFn scoringFn;
	private final File outputFile;

	public EmrRankFileExporter(Iterable<ExtractMethodRecomendation> data, EmrScoringFn scoringFn, String outputFile) {
		this.data = data;
		this.scoringFn = scoringFn;
		this.outputFile = new File(outputFile);
	}

	public void export() {
		try {
			this.outputFile.getParentFile().mkdirs();
			PrintWriter writer = new PrintWriter(this.outputFile);
			try {
				for (ExtractMethodRecomendation rec : this.data) {
					try {
//						IResource resource = rec.getSourceFile().getUnderlyingResource();
//						IFile ifile = (IFile) resource;
//						String loc = ifile.getLocation().toPortableString();
						double score = this.scoringFn.score(rec);
						writer.println(String.format("%s\t%s\t%s\t%d\t%d\t%s", rec.className, rec.method, rec.slice.toString(), rec.getOriginalSize(), rec.getExtractedSize(), Double.toString(score)));
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

	public static void exportAll(List<ExtractMethodRecomendation> recomendations, String baseFolder) {
		for (EmrScoringFn scoringFn : EmrScoringFn.values()) {
			Utils.sort(recomendations, scoringFn, false);
			String outputPath = String.format("%s/%s.txt", baseFolder, scoringFn.toString());
			new EmrRankFileExporter(recomendations, scoringFn, outputPath).export();
		}
	}
}
