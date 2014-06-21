package br.ufmg.dcc.labsoft.jextract.ranking;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.internal.corext.refactoring.code.ExtractMethodRefactoring;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

import br.ufmg.dcc.labsoft.jextract.codeanalysis.AstParser;

public class Utils {

	public static void sort(List<ExtractMethodRecomendation> recomendations, boolean groupByMethod) {
		EmrComparator comparator = new EmrComparator(groupByMethod);
		Collections.sort(recomendations, comparator);
//		for (int i = 0, len = recomendations.size(); i < len; i++) {
//			recomendations.get(i).setRank(i);
//		}
	}

	public static List<ExtractMethodRecomendation> filterSameMethod(List<ExtractMethodRecomendation> recomendations, int maxCount) {
		ArrayList<ExtractMethodRecomendation> filtered = new ArrayList<ExtractMethodRecomendation>();
		Map<String, Integer> dejavu = new HashMap<String, Integer>();
		for (ExtractMethodRecomendation rec : recomendations) {
			String classAndMethod = rec.className + rec.method;
			if (!dejavu.containsKey(classAndMethod)) {
				dejavu.put(classAndMethod, 0);
			}
			Integer count = dejavu.get(classAndMethod) + 1;
			dejavu.put(classAndMethod, count);
			if (count <= maxCount) {
				filtered.add(rec);
			}
		}
		return filtered;
	}

	public static void asString(StringBuilder sb, Iterable<String> methodSet) {
		sb.append("{\n");
		for (String item : methodSet) {
			sb.append("  ");
			sb.append(item);
			sb.append("\n");
		}
		sb.append("}\n");
	}

	public static Statement findEnclosingStatement(ASTNode astNode) {
		ASTNode node = astNode;
		while (node != null) {
			if (node instanceof Statement) {
				return (Statement) node;
			}
			node = node.getParent();
		}
		return null;
	}

	public static boolean canExtract(ICompilationUnit src, int start, int length) {
		try {
			ExtractMethodRefactoring refactoring = new ExtractMethodRefactoring(src, start, length);
			RefactoringStatus status = refactoring.checkAllConditions(new NullProgressMonitor());
			return status.isOK();
		} catch (CoreException e) {
			throw new RuntimeException(e);
		}
	}

	public static CompilationUnit compile(ICompilationUnit icu, boolean resolveBindings) {
		return new AstParser().getCompilationUnit(icu, resolveBindings);
	}

}
