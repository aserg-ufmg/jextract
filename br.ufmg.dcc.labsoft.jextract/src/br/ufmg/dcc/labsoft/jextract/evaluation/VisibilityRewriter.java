package br.ufmg.dcc.labsoft.jextract.evaluation;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jface.text.Document;
import org.eclipse.text.edits.TextEdit;

public class VisibilityRewriter extends ASTVisitor {

	private final IProgressMonitor pm;

    public VisibilityRewriter(IProgressMonitor pm) {
	    this.pm = pm;
    }

	public void rewrite(ICompilationUnit icu) throws Exception {
		String source = icu.getSource();
		Document document = new Document(source);

		ASTParser parser = ASTParser.newParser(AST.JLS4);
		parser.setSource(icu);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setResolveBindings(false);
		final CompilationUnit cu = (CompilationUnit) parser.createAST(null);
		cu.recordModifications();
		cu.accept(new ASTVisitor() {
			@Override
			public boolean visit(FieldDeclaration field) {
				changeModifiersToPublic(cu, field.modifiers());
				return true;
			}
			@Override
			public boolean visit(MethodDeclaration method) {
				changeModifiersToPublic(cu, method.modifiers());
				return true;
			}
			@Override
			public boolean visit(EnumDeclaration node) {
			    // Não mexer na visibilidade de enums
			    return false;
			}
		});

		// computation of the text edits
		TextEdit edits = cu.rewrite(document, icu.getJavaProject().getOptions(true));

		// computation of the new source code
		edits.apply(document);
		String newSource = document.get();

		// update of the compilation unit
		IBuffer buffer = icu.getBuffer();
		buffer.setContents(newSource);
		buffer.save(this.pm, false);
	}

	private void changeModifiersToPublic(CompilationUnit cu, List<Object> modifiers) {
		for (int i = modifiers.size() - 1; i >= 0; i--) {
			Object item = modifiers.get(i);
			if (item instanceof Modifier) {
				Modifier modifier = (Modifier) item;
				if (modifier.isPrivate() || modifier.isProtected()) {
					modifiers.remove(i);
				}
				if (modifier.isPublic()) {
					// member is already public, nothing to do
					return;
				}
			}
		}
		// add public modifier
		modifiers.add(cu.getAST().newModifier(Modifier.ModifierKeyword.PUBLIC_KEYWORD));
	}

}
