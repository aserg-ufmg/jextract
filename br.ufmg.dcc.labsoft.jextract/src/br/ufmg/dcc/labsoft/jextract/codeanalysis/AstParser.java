package br.ufmg.dcc.labsoft.jextract.codeanalysis;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;

public class AstParser {

	public ICompilationUnit getICompilationUnit(IProject project, String path) {
		IFile file = project.getFile(path);
		ICompilationUnit icu = JavaCore.createCompilationUnitFrom(file);
		if (icu == null) {
			throw new IllegalArgumentException(String.format("ICompilationUnit %s not found", file.getFullPath()));
		}
		return icu;
	}

	public CompilationUnit getCompilationUnit(ICompilationUnit icu, boolean resolveBindings) {
		ASTParser parser = ASTParser.newParser(AST.JLS4);
		parser.setSource(icu);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setResolveBindings(resolveBindings);
		return (CompilationUnit) parser.createAST(null);
	}

//	public CompilationUnit getCompilationUnit(IJavaProject project, String path, boolean resolveBindings) {
//		return this.getCompilationUnit(this.getICompilationUnit(project, path), resolveBindings);
//	}

	public MethodDeclaration getMethodDeclaration(ICompilationUnit icu, String mKey) {
		CompilationUnit cu = this.getCompilationUnit(icu, true);
		MethodDeclaration methodDeclaration = (MethodDeclaration) cu.findDeclaringNode(mKey);
		if (methodDeclaration == null) {
			throw new IllegalArgumentException(String.format("Method %s not found", mKey));
		}
		return methodDeclaration;
	}

}
