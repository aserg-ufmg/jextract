package br.ufmg.dcc.labsoft.jextract.codeanalysis;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

public class AstParser {

	public ICompilationUnit getICompilationUnit(IJavaProject project, String path) {
		try {
			IPath projectPath = project.getPath();
			IPath filePath = projectPath.append(path);
			ICompilationUnit icu  = (ICompilationUnit) project.findElement(filePath);
			return icu;
		} catch (JavaModelException e) {
			throw new RuntimeException(e);
		}
	}

	public CompilationUnit getCompilationUnit(ICompilationUnit icu, boolean resolveBindings) {
		ASTParser parser = ASTParser.newParser(AST.JLS4);
		parser.setSource(icu);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setResolveBindings(resolveBindings);
		return (CompilationUnit) parser.createAST(null);
	}

	public CompilationUnit getCompilationUnit(IJavaProject project, String path, boolean resolveBindings) {
		return this.getCompilationUnit(this.getICompilationUnit(project, path), resolveBindings);
	}
}
