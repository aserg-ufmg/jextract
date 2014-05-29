package br.ufmg.dcc.labsoft.jextract.model.impl;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.ReturnStatement;

import br.ufmg.dcc.labsoft.jextract.model.BlockModel;
import br.ufmg.dcc.labsoft.jextract.model.StatementModel;

public class BlockBasedPdg {

	private boolean deps[][][]; 
	
	public BlockBasedPdg() {
	}

	public void build(MethodDeclaration methodDeclaration, LinkedHashMap<Object, ? extends StatementModel> statementsMap, List<? extends BlockModel> blocks) {
		
		this.deps = new boolean[blocks.size()][][];
		for (int block = 0; block < this.deps.length; block++) {
			int blockDimension = blocks.get(block).getChildren().size();
			this.deps[block] = new boolean[blockDimension][blockDimension];
			for (int i = 0; i < blockDimension; i++) {
				for (int j = 0; j < blockDimension; j++) {
					this.deps[block][i][j] = false;
				}
			}
		}
		
		/*
		// this is the code that generates the control flow graph and program
		// dependence graph of the method
		MethodObject methodObject = new MethodObject(methodDeclaration);
		CFG cfg = new CFG(methodObject);
		PDG pdg = new PDG(cfg);
		// from the pdg we can get the nodes and for each node the incoming and
		// outgoing dependencies
		Set<GraphNode> nodes = pdg.getNodes();
		for (GraphNode node : nodes) {
			PDGNode pdgNode1 = (PDGNode) node;
			StatementModel stm1 = statementsMap.get(pdgNode1.getASTStatement());
			
			Iterator<GraphEdge> outgoingDeps = pdgNode1.getOutgoingDependenceIterator();
			while (outgoingDeps.hasNext()) {
				PDGDependence dep = (PDGDependence) outgoingDeps.next();
				PDGNode pdgNode2 = (PDGNode) dep.getDst();
				StatementModel stm2 = statementsMap.get(pdgNode2.getASTStatement());
				
				for (StatementModel a : this.getSelfAndParents(stm2)) {
					for (StatementModel b : this.getSelfAndParents(stm1)) {
						
						int aIndex = a.getIndexInBlock();
						int aBlock = a.getParentBlock().getIndex();
						int bIndex = b.getIndexInBlock();
						int bBlock = b.getParentBlock().getIndex();
						
						if (aBlock == bBlock) {
							this.deps[aBlock][aIndex][bIndex] = true;
//							System.out.print(a);
//							System.out.print(" DEPENDS ON ");
//							System.out.println(b);
						}
					}	
				}
			}
		}
		*/
		
		for (int block = 0; block < this.deps.length; block++) {
			List<? extends StatementModel> children = blocks.get(block).getChildren();
			int blockDimension = children.size();
			if (blockDimension > 0 && children.get(blockDimension - 1).getAstNode() instanceof ReturnStatement) {
				// A return statements is marked as it depends on every statement of the block.
				for (int j = 0; j < blockDimension - 1; j++) {
					this.deps[block][blockDimension - 1][j] = true;
				}
			}
		}
	}

	private List<StatementModel> getSelfAndParents(StatementModel statement) {
		ArrayList<StatementModel> list = new ArrayList<StatementModel>();
		StatementModel s = statement;
		while (s != null && s.getParentBlock() != null) {
			list.add(s);
			s = s.getParentStatement();
		}
	    return list;
    }

	public boolean depends(int block, int i, int j) {
		return this.deps[block][i][j];
	}

}
