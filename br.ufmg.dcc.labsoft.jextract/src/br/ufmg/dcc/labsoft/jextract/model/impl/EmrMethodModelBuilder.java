package br.ufmg.dcc.labsoft.jextract.model.impl;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SwitchStatement;

import br.ufmg.dcc.labsoft.jextract.model.MethodModel;
import br.ufmg.dcc.labsoft.jextract.ranking.Utils;

public class EmrMethodModelBuilder extends ASTVisitor {

	private LinkedHashMap<Object, EmrStatement> statementsMap;
	private List<EmrBlock> blocks;

	private EmrMethodModelBuilder() {
		// private constructor
	}

	public static MethodModel create(ICompilationUnit src, MethodDeclaration methodDeclaration) {
		return new EmrMethodModelBuilder().getModel(src, methodDeclaration);
	}

	public MethodModel getModel(ICompilationUnit src, MethodDeclaration methodDeclaration) {
		this.statementsMap = new LinkedHashMap<Object, EmrStatement>();
		this.blocks = new ArrayList<EmrBlock>();
		methodDeclaration.accept(this);
		EmrStatement[] sa = this.statementsMap.values().toArray(new EmrStatement[this.statementsMap.size()]);
		EmrBlock[] ba = this.blocks.toArray(new EmrBlock[this.blocks.size()]);
		return new EmrMethodModel(src, methodDeclaration, sa, ba);
	}

	@Override
	public void preVisit(ASTNode node) {
		if (node instanceof Statement) {
			Statement node1 = (Statement) node;
			// O pai direto de um statement pode não ser um statement quando existe inner class na jogada.
			EmrStatement parent = this.statementsMap.get(Utils.findEnclosingStatement(node1.getParent()));
			boolean blockLike = node1 instanceof Block || node1 instanceof SwitchStatement;
			EmrStatement emrStatement = new EmrStatement(this.statementsMap.size(), node1, parent, blockLike);
			this.statementsMap.put(node1, emrStatement);
		}
	}

	@Override
	public void postVisit(ASTNode node) {
		if (node instanceof Statement) {
			Statement stmNode = (Statement) node;
			EmrStatement thisStatement = this.statementsMap.get(stmNode);
			if (node instanceof Block) {
				// Cria um bloco com todos os statements filhos.
				createBlock(thisStatement, ((Block) node).statements());
			} else if (node instanceof SwitchStatement) {
				createBlock(thisStatement, ((SwitchStatement) node).statements());
			} else if (!thisStatement.isBlock() && !thisStatement.parent.isBlock()) {
				// Cria um bloco virtual englobando um único statement.
				createVirtualBlock(thisStatement);
			}
		}
	}

	private void createBlock(EmrStatement thisStatement, List statements) {
		EmrBlock emrBlock = new EmrBlock(thisStatement);
		for (Object stm : statements) {
			EmrStatement statement = this.statementsMap.get(stm);
			emrBlock.appendStatement(statement);
		}
		this.blocks.add(emrBlock);
	}

	private void createVirtualBlock(EmrStatement thisStatement) {
		EmrBlock emrBlock = new EmrBlock(thisStatement);
		emrBlock.appendStatement(thisStatement);
		this.blocks.add(emrBlock);
	}

}
