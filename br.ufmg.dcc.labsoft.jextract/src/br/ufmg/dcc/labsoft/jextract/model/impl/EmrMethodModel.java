package br.ufmg.dcc.labsoft.jextract.model.impl;

import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import br.ufmg.dcc.labsoft.jextract.model.MethodModel;

class EmrMethodModel implements MethodModel {

	private final ICompilationUnit compilationUnit;
	private final IMethodBinding methodBinding;
	private final EmrStatement[] statements;
	private final EmrBlock[] blocks;

    public EmrMethodModel(ICompilationUnit src, MethodDeclaration methodDeclaration, EmrStatement[] statements, EmrBlock[] blocks) {
	    this.compilationUnit = src;
	    this.methodBinding = methodDeclaration.resolveBinding();
	    this.statements = statements;
	    this.blocks = blocks;
    }

	public List<EmrBlock> getBlocks() {
		return Arrays.asList(blocks);
	}

	public int getTotalSize() {
		if (this.blocks.length == 0) {
			return 0;
		}
		return this.blocks[this.blocks.length - 1].getSize();
	}

	public String getMethodSignature() {
		return this.methodBinding.toString();
	}
	
	public String getDeclaringType() {
		return methodBinding.getDeclaringClass().getQualifiedName();
	}

	public ICompilationUnit getCompilationUnit() {
		return compilationUnit;
	}

}
