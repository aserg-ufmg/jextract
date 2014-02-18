package br.ufmg.dcc.labsoft.jextract.generation;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;

public class EmrMethodModel {

	private final ICompilationUnit compilationUnit;
	private final IMethodBinding methodBinding;
	private final EmrStatement[] statements;
	private final EmrBlock[] blocks;

	public static EmrMethodModel create(ICompilationUnit src, MethodDeclaration methodDeclaration) {
		return new EmrMethodModelBuilder().getModel(src, methodDeclaration);
	}

    public EmrMethodModel(ICompilationUnit src, MethodDeclaration methodDeclaration, EmrStatement[] statements, EmrBlock[] blocks) {
	    this.compilationUnit = src;
	    this.methodBinding = methodDeclaration.resolveBinding();
	    this.statements = statements;
	    this.blocks = blocks;
    }

	public EmrBlock[] getBlocks() {
		return blocks;
	}

	public EmrStatement get(int index) {
		return this.statements[index];
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
