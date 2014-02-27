package br.ufmg.dcc.labsoft.jextract.model;

import java.util.List;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;

public interface MethodModel {

	List<? extends BlockModel> getBlocks();

	int getTotalSize();

	String getMethodSignature();

	String getDeclaringType();

	ICompilationUnit getCompilationUnit();

	MethodDeclaration getAstNode();

}
