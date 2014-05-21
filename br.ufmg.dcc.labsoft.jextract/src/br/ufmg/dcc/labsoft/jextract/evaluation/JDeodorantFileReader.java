package br.ufmg.dcc.labsoft.jextract.evaluation;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import br.ufmg.dcc.labsoft.jextract.model.OffsetBasedEmrDescriptor;
import br.ufmg.dcc.labsoft.jextract.ranking.ExtractionSlice;

public class JDeodorantFileReader {

	private static final int COL_FILEPATH = 0;
	private static final int COL_METHOD = 1;
	private static final int COL_FRAGMENTS = 2;

	public List<OffsetBasedEmrDescriptor> read(String filePath) throws IOException {
		
		List<OffsetBasedEmrDescriptor> recomendations = new ArrayList<OffsetBasedEmrDescriptor>();
		
		BufferedReader br = new BufferedReader(new FileReader(filePath));
		try {
			String line;
			while ((line = br.readLine()) != null) {
				String[] cols = line.split("\t");
				final String sourcePath = cols[COL_FILEPATH];
				final String method = cols[COL_METHOD];
				final ExtractionSlice slice = ExtractionSlice.fromString(cols[COL_FRAGMENTS]);
				recomendations.add(new OffsetBasedEmrDescriptor(){
					@Override
                    public String getFilePath() {
	                    return sourcePath;
                    }
					@Override
                    public String getMethodBindingKey() {
	                    return method;
                    }
					@Override
                    public ExtractionSlice getExtractionSlice() {
	                    return slice;
                    }
				});
			}
		} finally {
			br.close();
		}
		
		return recomendations;
	}
}
