package br.ufmg.dcc.labsoft.jextract.generation;

import java.util.ArrayList;
import java.util.List;

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
import org.eclipse.jdt.core.dom.Statement;

import br.ufmg.dcc.labsoft.jextract.evaluation.ProjectRelevantSet;
import br.ufmg.dcc.labsoft.jextract.model.BlockModel;
import br.ufmg.dcc.labsoft.jextract.model.MethodModel;
import br.ufmg.dcc.labsoft.jextract.model.StatementModel;
import br.ufmg.dcc.labsoft.jextract.model.impl.MethodModelBuilder;
import br.ufmg.dcc.labsoft.jextract.ranking.ExtractMethodRecomendation;
import br.ufmg.dcc.labsoft.jextract.ranking.ExtractionSlice;
import br.ufmg.dcc.labsoft.jextract.ranking.ExtractionSlice.Fragment;
import br.ufmg.dcc.labsoft.jextract.ranking.Utils;

public class SimpleEmrGenerator {

	private final List<ExtractMethodRecomendation> recomendations;
	private List<ExtractMethodRecomendation> recomendationsForMethod;
	protected final int minSize;
	private EmrRecommender recommender;
	private ProjectRelevantSet goldset = null;

	public SimpleEmrGenerator(List<ExtractMethodRecomendation> recomendations, int minSize) {
		super();
		this.recomendations = recomendations;
		this.minSize = minSize;
		this.recommender = new EmrRecommender();
	}

	public void setGoldset(ProjectRelevantSet goldset) {
		this.goldset = goldset;
		this.recommender.setGoldset(goldset);
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
			@Override
			public boolean visit(MethodDeclaration methodDeclaration) {
				IMethod javaElement = (IMethod) methodDeclaration.resolveBinding().getJavaElement();
				if (onlyThisMethod == null || onlyThisMethod.isSimilar(javaElement)) {
					analyseMethod(src, methodDeclaration);
				}
				return false;
			}
		});
	}

	protected void forEachSlice(MethodModel model) {
		int methodSize = model.getTotalSize();
		for (BlockModel block: model.getBlocks()) {
			List<? extends StatementModel> children = block.getChildren();
			for (int last = children.size() - 1; last >= 0; last--) {
				int sliceSize = 0;
				for (int first = last; first >= 0; first--) {
					sliceSize += children.get(first).getTotalSize();
					if (sliceSize >= this.minSize) {
						int remaining = methodSize - sliceSize;
						if (remaining >= this.minSize) {
							int start = block.get(first).getAstNode().getStartPosition();
							StatementModel lastStatement = block.get(last);
							Statement lastStatementAstNode = lastStatement.getAstNode();
							int length = lastStatementAstNode.getStartPosition() + lastStatementAstNode.getLength() - start;
							if (Utils.canExtract(model.getCompilationUnit(), start, length)) {
								this.handleSequentialSlice(model, block, first, last, sliceSize);
							}
						}
					}
				}
			}
		}
	}

	private void handleSequentialSlice(MethodModel model, BlockModel block, int first, int last, int totalSize) {
		int start = block.get(first).getAstNode().getStartPosition();
		Statement lastStatementAstNode = block.get(last).getAstNode();
		int end = lastStatementAstNode.getStartPosition() + lastStatementAstNode.getLength();
		this.addRecomendation(model, totalSize, new Fragment(start, end, false));
	}

	protected void addRecomendation(MethodModel model, int totalSize, Fragment ... fragments) {
		ExtractMethodRecomendation recomendation = new ExtractMethodRecomendation(recomendations.size() + 1,
				model.getDeclaringType(), model.getMethodSignature(), new ExtractionSlice(fragments));

		recomendation.setDuplicatedSize(0);
		recomendation.setExtractedSize(totalSize);
		recomendation.setSourceFile(model.getCompilationUnit());
		recomendation.setOriginalSize(model.getTotalSize());

		recomendation.setOk(true);

		this.recomendationsForMethod.add(recomendation);
    }

	void analyseMethod(final ICompilationUnit src, MethodDeclaration methodDeclaration) {
		IMethodBinding methodBinding = methodDeclaration.resolveBinding();
		final String methodSignature = methodBinding.toString();
		final String declaringType = methodBinding.getDeclaringClass().getQualifiedName();

		if (this.goldset != null && !this.goldset.isMethodAvailable(declaringType, methodSignature)) {
			return;
		}
		
		String key = declaringType + "\t" + methodSignature;
		System.out.print("Analysing recomendations for " + key + " ... ");
		long time1 = System.currentTimeMillis();

		final MethodModel emrMethod = MethodModelBuilder.create(src, methodDeclaration);
		this.recomendationsForMethod = new ArrayList<ExtractMethodRecomendation>();
		this.forEachSlice(emrMethod);
		System.out.println("done in " + (System.currentTimeMillis() - time1) + " ms.");
		
		System.out.print("Ranking ... ");
		long time2 = System.currentTimeMillis();
		this.recomendations.addAll(this.recommender.rankAndFilterForMethod(src, methodDeclaration, this.recomendationsForMethod));
		System.out.println("done in " + (System.currentTimeMillis() - time2) + " ms.");
		
	}

}
