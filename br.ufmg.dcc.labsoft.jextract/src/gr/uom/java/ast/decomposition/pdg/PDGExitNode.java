package gr.uom.java.ast.decomposition.pdg;

import gr.uom.java.ast.decomposition.AbstractVariable;
import gr.uom.java.ast.decomposition.cfg.CFGExitNode;
import gr.uom.java.ast.decomposition.cfg.CFGNode;

import java.util.Set;

import org.eclipse.jdt.core.dom.VariableDeclaration;

public class PDGExitNode extends PDGStatementNode {
	private AbstractVariable returnedVariable;
	
	public PDGExitNode(CFGNode cfgNode, Set<VariableDeclaration> variableDeclarationsInMethod,
			Set<VariableDeclaration> fieldsAccessedInMethod) {
		super(cfgNode, variableDeclarationsInMethod, fieldsAccessedInMethod);
		if(cfgNode instanceof CFGExitNode) {
			CFGExitNode exitNode = (CFGExitNode)cfgNode;
			returnedVariable = exitNode.getReturnedVariable();
		}
	}

	public AbstractVariable getReturnedVariable() {
		return returnedVariable;
	}
}
