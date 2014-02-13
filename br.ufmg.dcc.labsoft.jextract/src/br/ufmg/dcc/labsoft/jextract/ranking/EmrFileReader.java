package br.ufmg.dcc.labsoft.jextract.ranking;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class EmrFileReader {

	private static final int COL_CLASSNAME = 0;
	private static final int COL_METHOD = 1;
	private static final int COL_FRAGMENTS = 2;

	public List<ExtractMethodRecomendation> read(String filePath) throws IOException {
		
		List<ExtractMethodRecomendation> recomendations = new ArrayList<ExtractMethodRecomendation>();
		
		BufferedReader br = new BufferedReader(new FileReader(filePath));
		try {
			String line;
			int id = 1;
			while ((line = br.readLine()) != null) {
				String[] cols = line.split("\t");
				String className = cols[COL_CLASSNAME];
				String method = cols[COL_METHOD];
				String fragments = cols[COL_FRAGMENTS];
				ExtractMethodRecomendation emr = new ExtractMethodRecomendation(id++, className, method, ExtractionSlice.fromString(fragments));
				recomendations.add(emr);
			}
		} finally {
			br.close();
		}
		
		return recomendations;
	}

}
