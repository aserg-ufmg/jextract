package br.ufmg.dcc.labsoft.jextract.generation;

import gr.uom.java.ast.ASTInformationGenerator;
import gr.uom.java.ast.CompilationUnitCache;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
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
import br.ufmg.dcc.labsoft.jextract.model.Placement;
import br.ufmg.dcc.labsoft.jextract.model.StatementModel;
import br.ufmg.dcc.labsoft.jextract.model.impl.MethodModelBuilder;
import br.ufmg.dcc.labsoft.jextract.ranking.ExtractMethodRecomendation;
import br.ufmg.dcc.labsoft.jextract.ranking.ExtractionSlice;
import br.ufmg.dcc.labsoft.jextract.ranking.ExtractionSlice.Fragment;

public class EmrGenerator {

	private final Settings settings;
	private final List<ExtractMethodRecomendation> recomendations;
	private List<ExtractMethodRecomendation> recomendationsForMethod;
	private EmrRecommender recommender;
	private ProjectRelevantSet goldset = null;
	private EmrScoringFunction scoringFn;
	
	private int recursiveCalls = 0;
	private MethodModel model;
	
	private BlockModel block;
	private StatementSelection selected;
	
	public EmrGenerator(List<ExtractMethodRecomendation> recomendations, Settings settings) {
		this.settings = settings;
		this.recomendations = recomendations;
		this.recommender = new EmrRecommender(settings);
		this.scoringFn = EmrScoringFunction.getInstance(settings);
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
					try {
						unit.getSource();
					} catch (Exception e) {
						return true;
						// ignora arquivo se não conseguiu pegar o source
					}
					analyseMethods(unit, null);
				}
				return true;
			}
		});
		this.recommender.printReport(project);
	}

	public void generateRecomendations(IProject project, ProjectRelevantSet goldset) throws Exception {
		this.setGoldset(goldset);
		IJavaProject jp = (IJavaProject) JavaCore.create(project);
		Set<String> classes = goldset.getCoveredClasses();
		for (String className : classes) {
			IType type = jp.findType(className);
			analyseMethods(type.getCompilationUnit(), null);
		}
		this.recommender.printReport(project);
	}

	public void generateRecomendations(IMethod method) throws Exception {
		analyseMethods(method.getCompilationUnit(), method);
	}

	// use ASTParse to parse string
	private void analyseMethods(final ICompilationUnit src, final IMethod onlyThisMethod) throws JavaModelException {

		ASTParser parser = ASTParser.newParser(AST.JLS4);
		parser.setSource(src);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setResolveBindings(true);
		final CompilationUnit cu = (CompilationUnit) parser.createAST(null);
		//we need that for JDeodorant :)
		//CompilationUnitCache.getInstance().addCompilationUnit(src, cu);
		//ASTInformationGenerator.setCurrentITypeRoot(src);

		cu.accept(new ASTVisitor() {
			@Override
			public boolean visit(MethodDeclaration methodDeclaration) {
				IMethodBinding binding = methodDeclaration.resolveBinding();
				if (binding != null) {
					IMethod javaElement = (IMethod) binding.getJavaElement();
					if (onlyThisMethod == null || onlyThisMethod.isSimilar(javaElement)) {
						analyseMethod(src, methodDeclaration);
					}
				}
				return false;
			}
		});
	}

	private void forEachSlice(MethodModel m) {
		this.recursiveCalls = 0;
		this.model = m;
		for (BlockModel b: m.getBlocks()) {
			this.block = b;
			this.selected = new StatementSelection(m, b);
			this.init(0);
		}
		//System.out.println("cost: " + this.recursiveCalls);
	}

	private ExtractMethodRecomendation addRecomendation(MethodModel model, int totalSize, int reorderedSize, Fragment ... fragments) {
		ExtractMethodRecomendation recomendation = new ExtractMethodRecomendation(recomendations.size() + 1,
				model.getDeclaringType(), model.getMethodSignature(), new ExtractionSlice(fragments));

		recomendation.setDuplicatedSize(0);
		recomendation.setExtractedSize(totalSize);
		recomendation.setSourceFile(model.getCompilationUnit());
		recomendation.setMethodBindingKey(model.getAstNode().resolveBinding().getKey());
		recomendation.setOriginalSize(model.getTotalSize());
		recomendation.setReorderedSize(reorderedSize);

		recomendation.setOk(true);

		this.recomendationsForMethod.add(recomendation);
		return recomendation;
    }

	private void analyseMethod(final ICompilationUnit src, MethodDeclaration methodDeclaration) {
		IMethodBinding methodBinding = methodDeclaration.resolveBinding();
		final String methodSignature = methodBinding.toString();
		final String declaringType = methodBinding.getDeclaringClass().getQualifiedName();

		if (this.goldset != null && !this.goldset.isMethodAvailable(declaringType, methodSignature)) {
			return;
		}
		
		String key = declaringType + "\t" + methodSignature;
		//System.out.print("Analysing recomendations for " + key + " ... ");
		long time1 = System.currentTimeMillis();

		final MethodModel emrMethod = MethodModelBuilder.create(src, methodDeclaration);
		this.recomendationsForMethod = new ArrayList<ExtractMethodRecomendation>();
		this.forEachSlice(emrMethod);
		//System.out.println("done in " + (System.currentTimeMillis() - time1) + " ms.");
		
		//System.out.print("Ranking ... ");
		long time2 = System.currentTimeMillis();
		this.recomendations.addAll(this.recommender.rankAndFilterForMethod(src, methodDeclaration, this.recomendationsForMethod));
		//System.out.println("done in " + (System.currentTimeMillis() - time2) + " ms.");
	}

	private void init(int i) {
		if (!this.checkBounds(i)) {
			return;
		}
		this.selected.set(i, Placement.BEFORE);
		init(i + 1);
		this.selected.set(i, Placement.UNASSIGNED);
		extract(i, 1);
    }
	
	private void extract(int i, int fragments) {
		if (!this.checkBounds(i)) {
			return;
		}
		int newSize = this.selected.getTotalSize() + this.block.get(i).getTotalSize();
		if ((this.model.getTotalSize() - newSize) < this.settings.getMinMethodSize()) {
			return;
		}
		if (!this.canBePlaced(i, Placement.INSIDE)) {
			return;
		}
		this.selected.set(i, Placement.INSIDE);
		extract(i + 1, fragments);
		end();
		skip(i + 1, fragments + 1);
		this.selected.set(i, Placement.UNASSIGNED);
	}
	
	private void skip(int i, int fragments) {
		if (!this.checkBounds(i) || fragments > this.settings.getMaxFragments()) {
			return;
		}
		if (this.canBePlaced(i, Placement.MOVED_BEFORE)) {
			this.selected.set(i, Placement.MOVED_BEFORE);
		} else if (this.canBePlaced(i, Placement.MOVED_AFTER)) {
			this.selected.set(i, Placement.MOVED_AFTER);
		} else {
			return;
		}
		
		skip(i + 1, fragments);
		extract(i + 1, fragments);
		this.selected.set(i, Placement.UNASSIGNED);
	}
	
	private void end() {
		if (selected.getTotalSize() < this.settings.getMinExtractedSize()) {
			return;
    	}
		this.handleSlice(model, block, selected);
	}

	private boolean checkBounds(int i) {
		this.recursiveCalls++;
		if (i >= block.getChildren().size()) {
			return false;
		}
		return true;
	}

	private boolean canBePlaced(int i, Placement placement) {
		for (int j = i - 1; j >= 0; j--) {
			// If an statement will be placed before some of its depencies, fail
			boolean iIsBeforeJ = this.selected.get(j).compareTo(placement) > 0;
			boolean iDependsOnJ = this.block.depends(i, j);
			if (iIsBeforeJ && iDependsOnJ) {
				return false;
			}
		}
		return true;
	}
	
	private void handleSlice(MethodModel model, BlockModel block, StatementSelection selected) {
		List<? extends StatementModel> children = block.getChildren();
		List<Fragment> frags = new ArrayList<Fragment>();
		int length = children.size();
		int totalSize = 0;
		for (int i = 0, j = 0; i < length; i = j) {
			while (i < length && !selected.isSelected(i)) {
				i++;
				j++;
			}
			while (j < length && selected.isSelected(j)) {
				totalSize += children.get(j).getTotalSize();
				j++;
			}
			if (i < length) {
				Statement s1 = children.get(i).getAstNode();
				Statement s2 = children.get(j - 1).getAstNode();
				frags.add(new Fragment(s1.getStartPosition(), s2.getStartPosition() + s2.getLength(), false));
			}
		}
		
		if (!frags.isEmpty()) {
			Fragment[] fragmentsArray = frags.toArray(new Fragment[frags.size()]);
			ExtractMethodRecomendation rec = this.addRecomendation(model, totalSize, selected.getReorderedStatements(), fragmentsArray);
			ScoreResult scoreResult = this.scoringFn.computeScore(rec, selected);
			rec.setScore(scoreResult.getScore());
			rec.setExplanation(scoreResult.getExplanation());
		}
    }
}
