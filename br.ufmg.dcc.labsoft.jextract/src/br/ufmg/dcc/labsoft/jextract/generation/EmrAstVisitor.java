package br.ufmg.dcc.labsoft.jextract.generation;

import java.util.LinkedHashMap;
import java.util.List;

import br.ufmg.dcc.labsoft.jextract.ranking.Utils;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SwitchStatement;

public class EmrAstVisitor extends ASTVisitor {

	private final LinkedHashMap<Object, EmrStatement> statementsMap;
	private final List<EmrBlock> blocks;

	public EmrAstVisitor(List<EmrBlock> blocks) {
		this.statementsMap = new LinkedHashMap<Object, EmrStatement>();
		this.blocks = blocks;
	}

	@Override
	public void preVisit(ASTNode node) {
		if (node instanceof Statement) {
			Statement node1 = (Statement) node;
			// O pai direto de um statement pode não ser um statement quando existe inner class na jogada.
			EmrStatement parent = this.statementsMap.get(Utils.findEnclosingStatement(node1.getParent()));
			boolean blockLike = node1 instanceof Block || node1 instanceof SwitchStatement;
			EmrStatement emrStatement = new EmrStatement(node1, parent, blockLike);
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
