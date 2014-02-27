package br.ufmg.dcc.labsoft.jextract.model.impl;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SwitchStatement;

import br.ufmg.dcc.labsoft.jextract.model.HasEntityDependencies;
import br.ufmg.dcc.labsoft.jextract.model.MethodModel;
import br.ufmg.dcc.labsoft.jextract.ranking.DependenciesAstVisitor;
import br.ufmg.dcc.labsoft.jextract.ranking.Utils;

public class MethodModelBuilder extends ASTVisitor {

	private LinkedHashMap<Object, StatementImpl> statementsMap;
	private List<BlockImpl> blocks;
	private Pdg pdg;
	private MethodDeclaration methodDeclaration;

	private MethodModelBuilder() {
		// private constructor
	}

	public static MethodModel create(ICompilationUnit src, MethodDeclaration methodDeclaration) {
		return new MethodModelBuilder().getModel(src, methodDeclaration);
	}

	public MethodModel getModel(ICompilationUnit src, MethodDeclaration methodDeclaration) {
		this.methodDeclaration = methodDeclaration;
		this.statementsMap = new LinkedHashMap<Object, StatementImpl>();
		this.blocks = new ArrayList<BlockImpl>();
		this.pdg = new Pdg();
		
		methodDeclaration.accept(this);
		
		// TODO build pdg
		
		BlockImpl[] ba = this.blocks.toArray(new BlockImpl[this.blocks.size()]);
		MethodModelImpl methodModel = new MethodModelImpl(src, methodDeclaration, ba);
		
		return methodModel;
	}

	@Override
	public void preVisit(ASTNode node) {
		if (node instanceof Statement) {
			Statement node1 = (Statement) node;
			// O pai direto de um statement pode não ser um statement quando existe inner class na jogada.
			StatementImpl parent = this.statementsMap.get(Utils.findEnclosingStatement(node1.getParent()));
			boolean blockLike = node1 instanceof Block || node1 instanceof SwitchStatement;
			StatementImpl emrStatement = new StatementImpl(this.statementsMap.size(), node1, parent, blockLike, this.pdg);
			this.statementsMap.put(node1, emrStatement);
		}
	}

	@Override
	public void postVisit(ASTNode node) {
		if (node instanceof Statement) {
			Statement stmNode = (Statement) node;
			final StatementImpl thisStatement = this.statementsMap.get(stmNode);
			if (node instanceof Block) {
				// Creates a block with all children.
				createBlock(thisStatement, ((Block) node).statements());
			} else if (node instanceof SwitchStatement) {
				createBlock(thisStatement, ((SwitchStatement) node).statements());
			} else if (!thisStatement.isBlock() && !thisStatement.parent.isBlock()) {
				// Creates a block with a single statement.
				createVirtualBlock(thisStatement);
			}
			fillEntities(stmNode, thisStatement);
		}
	}

	private void fillEntities(ASTNode stmNode, final HasEntityDependencies thisStatement) {
	    stmNode.accept(new DependenciesAstVisitor(this.methodDeclaration.resolveBinding().getDeclaringClass()) {
	    	@Override
	    	public void onModuleAccess(ASTNode node, String packageName) {
	    		thisStatement.getEntitiesP().add(packageName);
	    	}
	    	@Override
	    	public void onTypeAccess(ASTNode node, ITypeBinding binding) {
	    		thisStatement.getEntitiesP().add(binding.getKey());
	    	}
	    	@Override
	    	public void onVariableAccess(ASTNode node, IVariableBinding binding) {
	    		thisStatement.getEntitiesP().add(binding.getKey());
	    	}
	    });
    }

	private void createBlock(StatementImpl thisStatement, @SuppressWarnings("rawtypes") List statements) {
		BlockImpl emrBlock = new BlockImpl(thisStatement);
		for (Object stm : statements) {
			StatementImpl statement = this.statementsMap.get(stm);
			emrBlock.appendStatement(statement);
		}
		this.blocks.add(emrBlock);
	}

	private void createVirtualBlock(StatementImpl thisStatement) {
		BlockImpl emrBlock = new BlockImpl(thisStatement);
		emrBlock.appendStatement(thisStatement);
		this.blocks.add(emrBlock);
	}

}
