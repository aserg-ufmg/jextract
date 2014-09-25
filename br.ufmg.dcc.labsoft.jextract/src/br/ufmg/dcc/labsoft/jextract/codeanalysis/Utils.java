package br.ufmg.dcc.labsoft.jextract.codeanalysis;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

public class Utils {

	public static List<ICompilationUnit> findJavaResources(IProject project) {
		final List<ICompilationUnit> cu = new ArrayList<ICompilationUnit>();
		try {
			project.accept(new IResourceVisitor() {
				@Override
				public boolean visit(IResource resource) throws CoreException {
					if (resource instanceof IFile && resource.getName().endsWith(".java")) {
						ICompilationUnit unit = ((ICompilationUnit) JavaCore.create((IFile) resource));
						cu.add(unit);
//						try {
//							unit.getSource();
//						} catch (Exception e) {
//							return true;
//							// ICompilationUnit ignored when its source is not available
//						}
//						analyseMethods(unit, null);
					}
					return true;
				}
			});
		} catch (CoreException e) {
			throw new RuntimeException(e);
		}
		return cu;
	}

	public static List<ICompilationUnit> findJavaResources(IPackageFragment packg) {
		ArrayList<ICompilationUnit> list = new ArrayList<ICompilationUnit>();
		findJavaResourcesRecursive(packg, list);
		return list;
	}

	private static void findJavaResourcesRecursive(IPackageFragment packg, List<ICompilationUnit> list) {
		try {
			for (ICompilationUnit icu : packg.getCompilationUnits()) {
				list.add(icu);
			}
			for (IJavaElement e : packg.getChildren()) {
				if (e instanceof IPackageFragment) {
					findJavaResourcesRecursive((IPackageFragment) e, list);
				}
			}
		} catch (JavaModelException e) {
			throw new RuntimeException(e);
		}
	}

}
