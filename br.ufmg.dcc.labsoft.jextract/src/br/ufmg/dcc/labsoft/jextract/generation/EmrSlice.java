package br.ufmg.dcc.labsoft.jextract.generation;

import java.util.List;

public class EmrSlice {

	private final List<EmrStatement> data;
	private final int first;
	private final int last;
	private final int totalSize;
	
	public EmrSlice(List<EmrStatement> data, int first, int last, int totalSize) {
		this.data = data;
		this.first = first;
		this.last = last;
		this.totalSize = totalSize;
	}

	public EmrStatement get(int index) {
		return this.data.get(this.first + index);
	}
	
	public EmrStatement getFirstStatement() {
		return this.data.get(this.first);
	}

	public EmrStatement getLastStatement() {
		return this.data.get(this.last);
	}
	
	public int getLength() {
		return this.last - this.first + 1;
	}
	
	public int getTotalSize() {
		return totalSize;
	}
	
}
