package br.ufmg.dcc.labsoft.jextract.generation;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.MethodDeclaration;

public class EmrMethod {

	List<EmrBlock> blocks;

	public static EmrMethod create(MethodDeclaration methodDeclaration) {
		List<EmrBlock> blocks = new ArrayList<EmrBlock>();
		methodDeclaration.accept(new EmrAstVisitor(blocks));
		return new EmrMethod(blocks);
	}

	private EmrMethod(List<EmrBlock> blocks) {
		this.blocks = blocks;
	}

	private List<EmrBlock> getBlocks() {
		return this.blocks;
	}

	public void forEachSlice(EmrSliceHandler handler, int minSize) {
		int methodSize = this.getTotalSize();
		for (EmrBlock block: this.getBlocks()) {
			List<EmrStatement> children = block.getChildren();
			for (int last = children.size() - 1; last >= 0; last--) {
				int sliceSize = 0;
				for (int first = last; first >= 0; first--) {
					sliceSize += children.get(first).getSize();
					if (sliceSize >= minSize) {
						int remaining = methodSize - sliceSize;
						if (remaining >= minSize) {
							handler.handleSlice(new EmrSlice(children, first, last, sliceSize));
						}
					}
				}
			}
		}
	}

	public int getTotalSize() {
		if (this.blocks.isEmpty()) {
			return 0;
		}
		return this.blocks.get(this.blocks.size() - 1).getSize();
	}

}
