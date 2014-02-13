package br.ufmg.dcc.labsoft.jextract.evaluation;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;

public class PrivateAccessVisitor extends ASTVisitor {

	private boolean usesPrivate = false;
	private boolean usesPackagePrivate = false;

	@Override
	public boolean visit(FieldAccess node) {
		IVariableBinding fieldBinding = node.resolveFieldBinding();
		if (fieldBinding.isField()) {
			this.checkVisibility(fieldBinding);
		}
		return true;
	}

	@Override
	public boolean visit(SimpleName node) {
		return visitNameNode(node);
	}

	@Override
	public boolean visit(QualifiedName node) {
		return visitNameNode(node);
	}

	public boolean visit(MethodInvocation node) {
		this.checkVisibility(node.resolveMethodBinding());
		return true;
	};

	private boolean visitNameNode(Name node) {
		IBinding binding = node.resolveBinding();
		if (binding instanceof IVariableBinding) {
			IVariableBinding variableBindig = (IVariableBinding) binding;
			if (variableBindig.isField()) {
				this.checkVisibility(variableBindig);
			}
		}
		return true;
	}

	private void checkVisibility(IBinding bindig) {
		if (bindig == null) {
			return;
		} 
		int mod = bindig.getModifiers();
		if (Modifier.isPrivate(mod) || Modifier.isProtected(mod)) {
			this.usesPrivate = true;
		}
		if (!Modifier.isPublic(mod)) {
			this.usesPackagePrivate = true;
		}
	}

	public boolean hasPrivate() {
		return usesPrivate;
	}

	public boolean hasPackagePrivate() {
		return usesPackagePrivate;
	}

}
