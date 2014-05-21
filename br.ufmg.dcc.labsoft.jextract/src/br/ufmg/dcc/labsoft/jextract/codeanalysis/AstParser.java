package br.ufmg.dcc.labsoft.jextract.codeanalysis;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

public class AstParser {

	public CompilationUnit getCompilationUnit(ICompilationUnit icu, boolean resolveBindings) {
		ASTParser parser = ASTParser.newParser(AST.JLS4);
		parser.setSource(icu);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setResolveBindings(resolveBindings);
		return (CompilationUnit) parser.createAST(null);
	}

}
