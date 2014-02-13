package br.ufmg.dcc.labsoft.jextract.generation;

import java.util.List;

import br.ufmg.dcc.labsoft.jextract.ranking.ExtractMethodRecomendation;
import br.ufmg.dcc.labsoft.jextract.ranking.ExtractionSlice;
import br.ufmg.dcc.labsoft.jextract.ranking.Utils;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;

public class EmrGenerator {

	private final List<ExtractMethodRecomendation> recomendations;
	private final int minSize;

	public EmrGenerator(List<ExtractMethodRecomendation> recomendations, int minSize) {
		super();
		this.recomendations = recomendations;
		this.minSize = minSize;
	}

	public void generateRecomendations(IProject project) throws Exception {
		project.accept(new IResourceVisitor() {
			@Override
			public boolean visit(IResource resource) throws CoreException {
				if (resource instanceof IFile && resource.getName().endsWith(".java")) {
					ICompilationUnit unit = ((ICompilationUnit) JavaCore.create((IFile) resource));
					analyseMethods(unit, null);
				}
				return true;
			}
		});
	}

	public void generateRecomendations(IMethod method) throws Exception {
		analyseMethods(method.getCompilationUnit(), method);
	}

	// use ASTParse to parse string
	void analyseMethods(final ICompilationUnit src, final IMethod onlyThisMethod) throws JavaModelException {

		ASTParser parser = ASTParser.newParser(AST.JLS4);
		parser.setSource(src);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setResolveBindings(true);
		final CompilationUnit cu = (CompilationUnit) parser.createAST(null);

		cu.accept(new ASTVisitor() {
			public boolean visit(MethodDeclaration methodDeclaration) {
				IMethod javaElement = (IMethod) methodDeclaration.resolveBinding().getJavaElement();
				if (onlyThisMethod == null || onlyThisMethod.isSimilar(javaElement)) {
					analyseMethod(src, methodDeclaration);
				}
				return false;
			}
		});
	}

	void analyseMethod(final ICompilationUnit src, MethodDeclaration methodDeclaration) {
		IMethodBinding methodBinding = methodDeclaration.resolveBinding();
		final String methodSignature = methodBinding.toString();
		final String declaringType = methodBinding.getDeclaringClass().getQualifiedName();

		String key = declaringType + "\t" + methodSignature;
		System.out.println("Analysing recomendations for " + key);

		final EmrMethod emrMethod = EmrMethod.create(methodDeclaration);
		emrMethod.forEachSlice(new EmrSliceHandler() {
			@Override
			public void handleSlice(EmrSlice slice) {
				int start = slice.getFirstStatement().getStartChar();
				EmrStatement lastStatement = slice.getLastStatement();
				int length = lastStatement.getStartChar() + lastStatement.getCharLength() - start;

				ExtractMethodRecomendation recomendation = new ExtractMethodRecomendation(recomendations.size() + 1,
				        declaringType, methodSignature, ExtractionSlice.fromString(String.format("e%d:%d;", start,
				                length)));

				recomendation.setDuplicatedSize(0);
				recomendation.setExtractedSize(slice.getTotalSize());
				recomendation.setSourceFile(src);
				recomendation.setOriginalSize(emrMethod.getTotalSize());

				recomendation.setOk(Utils.canExtract(src, start, length));

				if (recomendation.isOk()) {
					recomendations.add(recomendation);
				}
			}
		}, minSize);

	}

}
