package br.ufmg.dcc.labsoft.jextract.ranking;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import br.ufmg.dcc.labsoft.jextract.metrics.DependenciesAstVisitor;
import br.ufmg.dcc.labsoft.jextract.ranking.ExtractionSlice.Fragment;

public class JavaProjectAnalyser {

	private final HashMap<String, List<ExtractMethodRecomendation>> rmap;
	private final List<ExtractMethodRecomendation> recomendations;
	private boolean checkEclipsePreconditions = false;
	private int methodCount = 0;
	private int methodCountMinSize = 0;
	
	public JavaProjectAnalyser(List<ExtractMethodRecomendation> recomendations, boolean checkEclipsePreconditions) {
		this.recomendations = recomendations;
		this.checkEclipsePreconditions = checkEclipsePreconditions;
		this.rmap = new HashMap<String, List<ExtractMethodRecomendation>>();
		for (ExtractMethodRecomendation recomendation : recomendations) {
			String key = recomendation.className + "\t" + recomendation.method;
			List<ExtractMethodRecomendation> alternatives;
			if (!this.rmap.containsKey(key)) {
				alternatives = new ArrayList<ExtractMethodRecomendation>();
				this.rmap.put(key, alternatives);
			} else {
				alternatives = this.rmap.get(key);
			}
			alternatives.add(recomendation);
		}
	}

	public void analyseProject(IProject project) throws Exception {
		this.methodCount = 0;
		this.methodCountMinSize = 0;
		project.accept(new IResourceVisitor() {
			@Override
			public boolean visit(IResource resource) throws CoreException {
				if (resource instanceof IFile && resource.getName().endsWith(".java")) {
					ICompilationUnit unit = ((ICompilationUnit) JavaCore.create((IFile) resource));
					analyseMethods(unit);
				}
				return true;
			}
		});
		
		this.sortRecomendations();
		System.out.println(this.methodCount + " methods analysed.");
		System.out.println(this.methodCountMinSize + " pass minSize.");
	}

	public void analyseMethod(IMethod method) throws Exception {
		this.methodCount = 0;
		this.methodCountMinSize = 0;
		analyseMethods(method.getCompilationUnit());
		this.sortRecomendations();
		System.out.println(this.methodCount + " methods analysed.");
		System.out.println(this.methodCountMinSize + " pass minSize.");
	}

	private void sortRecomendations() {
		Utils.sort(this.recomendations, EmrScoringFn.KUL_TVM, false);
	}

	// use ASTParse to parse string
	void analyseMethods(final ICompilationUnit src) throws CoreException {

		ASTParser parser = ASTParser.newParser(AST.JLS4);
		parser.setSource(src);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setResolveBindings(true);
		final CompilationUnit cu = (CompilationUnit) parser.createAST(null);

		cu.accept(new ASTVisitor() {
			public boolean visit(MethodDeclaration methodDeclaration) {
				analyseMethod(src, methodDeclaration);
				return false;
			}
		});

	}

	void analyseMethod(ICompilationUnit src, MethodDeclaration methodDeclaration) {
		this.methodCount++;
		
		StatementsCountVisitor counter = new StatementsCountVisitor();
		methodDeclaration.accept(counter);
		int size = counter.getCount();
		if (size >= 6) {
			this.methodCountMinSize++;
		}
		
		IMethodBinding methodBinding = methodDeclaration.resolveBinding();
		String methodSignature = methodBinding.toString();
		String declaringType = methodBinding.getDeclaringClass().getQualifiedName();
		
		String key = declaringType + "\t" + methodSignature;
		if (this.rmap.containsKey(key)) {
			System.out.println("Analysing recomendations for " + key);
			
			final List<ExtractMethodRecomendation> alternatives = this.rmap.get(key);
			for (ExtractMethodRecomendation alternative : alternatives) {
				final ExtractionSlice slice = alternative.slice;
				StatementsSliceCountVisitor statementCounter = new StatementsSliceCountVisitor(slice);
				methodDeclaration.accept(statementCounter);
				alternative.setOriginalSize(statementCounter.getCount());
				alternative.setDuplicatedSize(statementCounter.getDuplicatedCount());
				alternative.setExtractedSize(statementCounter.getExtractedCount());

				if (alternative.getSourceFile() == null) {
					alternative.setSourceFile(src);
				}

				SetsSimilarity<String> ssimT = this.computeSetsSimilarity(methodDeclaration, slice, true, false, false);
				SetsSimilarity<String> ssimV = this.computeSetsSimilarity(methodDeclaration, slice, false, true, false);
				SetsSimilarity<String> ssimM = this.computeSetsSimilarity(methodDeclaration, slice, false, false, true);
				alternative.setSsimT(ssimT);
				alternative.setSsimV(ssimV);
				alternative.setSsimM(ssimM);
				
				alternative.setPt(computeProb(methodDeclaration, slice, true, false, false, alternative));
				alternative.setPv(computeProb(methodDeclaration, slice, false, true, false, alternative));
				alternative.setPm(computeProb(methodDeclaration, slice, false, false, true, alternative));
				if (this.checkEclipsePreconditions) {
					Fragment frag = slice.getEnclosingFragment();
					alternative.setOk(Utils.canExtract(src, frag.start, frag.length()));
				}
//				alternative.setPtv(computeProb(methodDeclaration, slice, true, true, false, alternative));
			}
		}
	}

	private SetsSimilarity<String> computeSetsSimilarity(MethodDeclaration methodDeclaration, final ExtractionSlice slice, final boolean typeAccess, final boolean variableAccess, final boolean packageAccess) {
		final SetsSimilarity<String> ssim = new SetsSimilarity<String>();
		methodDeclaration.accept(new DependenciesAstVisitor(methodDeclaration.resolveBinding().getDeclaringClass()) {
			@Override
			public void onTypeAccess(ASTNode node, ITypeBinding binding) {
				if (!typeAccess) {
					return;
				}
				if (slice.belongsToMethod(node.getStartPosition())) {
					ssim.addToSet1(binding.getKey());
				}
				if (slice.belongsToExtracted(node.getStartPosition())) {
					ssim.addToSet2(binding.getKey());
				}
			}
			@Override
			public void onVariableAccess(ASTNode node, IVariableBinding binding) {
				if (!variableAccess) {
					return;
				}
				if (slice.belongsToMethod(node.getStartPosition())) {
					ssim.addToSet1(binding.getKey());
				}
				if (slice.belongsToExtracted(node.getStartPosition())) {
					ssim.addToSet2(binding.getKey());
				}
			}
			@Override
			public void onModuleAccess(ASTNode node, String packageName) {
				if (!packageAccess) {
					return;
				}
				if (slice.belongsToMethod(node.getStartPosition())) {
					ssim.addToSet1(packageName);
				}
				if (slice.belongsToExtracted(node.getStartPosition())) {
					ssim.addToSet2(packageName);
				}
			}
		});
		ssim.end();
		return ssim;
	}
	
	private double computeProb(MethodDeclaration methodDeclaration, final ExtractionSlice slice, final boolean typeAccess, final boolean variableAccess, final boolean packageAccess, ExtractMethodRecomendation alternative) {
		final Map<String, Integer> counter = new HashMap<String, Integer>();
		final Set<String> extractedSet = new HashSet<String>();
		final Set<String> methodSet = new HashSet<String>();
		methodDeclaration.accept(new DependenciesAstVisitor(methodDeclaration.resolveBinding().getDeclaringClass()) {
			@Override
			public void onTypeAccess(ASTNode node, ITypeBinding binding) {
				if (!typeAccess) {
					return;
				}
				if (slice.belongsToExtracted(node.getStartPosition())) {
					extractedSet.add(binding.getKey());
				}
				if (slice.belongsToMethod(node.getStartPosition())) {
					methodSet.add(binding.getKey());
				}
				Integer currentCount = counter.get(binding.getKey());
				counter.put(binding.getKey(), currentCount == null ? 1 : currentCount + 1);
			}
			@Override
			public void onVariableAccess(ASTNode node, IVariableBinding binding) {
				if (!variableAccess) {
					return;
				}
				if (slice.belongsToExtracted(node.getStartPosition())) {
					extractedSet.add(binding.getKey());
				}
				if (slice.belongsToMethod(node.getStartPosition())) {
					methodSet.add(binding.getKey());
				}
				Integer currentCount = counter.get(binding.getKey());
				counter.put(binding.getKey(), currentCount == null ? 1 : currentCount + 1);
			}
			@Override
			public void onModuleAccess(ASTNode node, String packageName) {
				if (!packageAccess) {
					return;
				}
				if (slice.belongsToExtracted(node.getStartPosition())) {
					extractedSet.add(packageName);
				}
				if (slice.belongsToMethod(node.getStartPosition())) {
					methodSet.add(packageName);
				}
				Integer currentCount = counter.get(packageName);
				counter.put(packageName, currentCount == null ? 1 : currentCount + 1);
			}
		});
		
		int methodSize = (alternative.getOriginalSize() - alternative.getExtractedSize());
		double methodP = ((double) methodSize) / alternative.getOriginalSize();
		double extractedP = 1.0 - methodP;
		double p = 1.0;
		for (Map.Entry<String, Integer> entry : counter.entrySet()) {
			String entity = entry.getKey();
			int count = entry.getValue();
			if (extractedSet.contains(entity) && !methodSet.contains(entity)) {
				p *= Math.pow(extractedP, count);
			} else if (methodSet.contains(entity) && !extractedSet.contains(entity)) {
				p *= Math.pow(methodP, count);
			}
		}
		return p;
	}

}
