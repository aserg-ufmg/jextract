package br.ufmg.dcc.labsoft.jextract.model.impl;

import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import br.ufmg.dcc.labsoft.jextract.model.MethodModel;

class MethodModelImpl implements MethodModel {

	private final ICompilationUnit compilationUnit;
	private final IMethodBinding methodBinding;
	private final MethodDeclaration astNode;
	//private final EmrStatement[] statements;
	private final BlockImpl[] blocks;

    public MethodModelImpl(ICompilationUnit src, MethodDeclaration methodDeclaration, BlockImpl[] blocks) {
    	this.compilationUnit = src;
	    this.astNode = methodDeclaration;
	    this.methodBinding = methodDeclaration.resolveBinding();
	    //this.statements = statements;
	    this.blocks = blocks;
    }

    @Override
	public List<BlockImpl> getBlocks() {
		return Arrays.asList(blocks);
	}

    @Override
	public int getTotalSize() {
		if (this.blocks.length == 0) {
			return 0;
		}
		return this.blocks[this.blocks.length - 1].getTotalSize();
	}

    @Override
	public String getMethodSignature() {
		return this.methodBinding.toString();
	}
	
    @Override
	public String getDeclaringType() {
		return methodBinding.getDeclaringClass().getQualifiedName();
	}

    @Override
	public ICompilationUnit getCompilationUnit() {
		return this.compilationUnit;
	}

    @Override
	public MethodDeclaration getAstNode() {
		return this.astNode;
	}

}
