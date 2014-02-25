package gr.uom.java.ast.decomposition.pdg;

import gr.uom.java.ast.decomposition.AbstractVariable;
import gr.uom.java.ast.decomposition.cfg.CFGBranchNode;

public class PDGDataDependence extends PDGAbstractDataDependence {

	public PDGDataDependence(PDGNode src, PDGNode dst,
			AbstractVariable data, CFGBranchNode loop) {
		super(src, dst, PDGDependenceType.DATA, data, loop);
	}
	
}
