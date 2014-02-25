package gr.uom.java.ast.decomposition.pdg;

import gr.uom.java.ast.decomposition.AbstractVariable;
import gr.uom.java.ast.decomposition.cfg.CFGBranchNode;

public class PDGOutputDependence extends PDGAbstractDataDependence {

	public PDGOutputDependence(PDGNode src, PDGNode dst,
			AbstractVariable data, CFGBranchNode loop) {
		super(src, dst, PDGDependenceType.OUTPUT, data, loop);
	}
	
}
