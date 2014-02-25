package gr.uom.java.ast.decomposition.pdg;

import gr.uom.java.ast.decomposition.graph.GraphEdge;
import gr.uom.java.ast.decomposition.graph.GraphNode;

public abstract class PDGDependence extends GraphEdge {
	private PDGDependenceType type;
	
	public PDGDependence(PDGNode src, PDGNode dst, PDGDependenceType type) {
		super(src, dst);
		this.type = type;
	}

	public GraphNode getSrc() {
		return src;
	}

	public GraphNode getDst() {
		return dst;
	}

	public PDGDependenceType getType() {
		return type;
	}
}
