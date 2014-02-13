package br.ufmg.dcc.labsoft.jextract.ranking;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.IPackageBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

public abstract class DependenciesAstVisitor extends ASTVisitor {

	private final ITypeBinding myClass;

	public DependenciesAstVisitor(ITypeBinding myClass) {
		this.myClass = myClass;
	}

	@Override
	public boolean visit(MethodInvocation node) {
		IMethodBinding methodBinding = node.resolveMethodBinding();
		handleTypeBinding(node, methodBinding.getDeclaringClass());
		handleMethodBinding(node, methodBinding);
		return true;
	}

	@Override
	public boolean visit(FieldAccess node) {
		IVariableBinding fieldBinding = node.resolveFieldBinding();
		handleTypeBinding(node, fieldBinding.getDeclaringClass());
		handleFieldBinding(node, fieldBinding);
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

	private boolean visitNameNode(Name node) {
		IBinding binding = node.resolveBinding();
		if (binding instanceof IVariableBinding) {
			IVariableBinding variableBindig = (IVariableBinding) binding;
			if (variableBindig.isField()) {
				ITypeBinding declaringClass = variableBindig.getDeclaringClass();
				handleTypeBinding(node, declaringClass);
				handleFieldBinding(node, variableBindig);
			} else if (!variableBindig.isEnumConstant()) {
				handleVariableBinding(node, variableBindig);
			}
		}
		return true;
	}

	@Override
	public boolean visit(ClassInstanceCreation node) {
		ITypeBinding typeBinding = node.resolveTypeBinding();
		handleTypeBinding(node, typeBinding);
		return true;
	}

	@Override
	public boolean visit(VariableDeclarationStatement node) {
		ITypeBinding typeBinding = node.getType().resolveBinding();
		handleTypeBinding(node, typeBinding);
		return true;
	}

	@Override
	public boolean visit(CatchClause node) {
		ITypeBinding typeBinding = node.getException().getType().resolveBinding();
		handleTypeBinding(node, typeBinding);
		return true;
	}

	@Override
	public boolean visit(MarkerAnnotation node) {
		ITypeBinding typeBinding = node.getTypeName().resolveTypeBinding();
		handleTypeBinding(node, typeBinding);
		return true;
	}

	public void onTypeAccess(ASTNode node, ITypeBinding binding) {
		// override
	}

	public void onModuleAccess(ASTNode node, String packageName) {
		// override
	}

	public void onVariableAccess(ASTNode node, IVariableBinding binding) {
		// override
	}

	public void onMethodAccess(ASTNode node, IMethodBinding binding) {
		// override
	}

	private void handleTypeBinding(ASTNode node, ITypeBinding typeBinding) {
		if (typeBinding == null) {
			StructuralPropertyDescriptor locationInParent = node.getLocationInParent();
			System.out.println(locationInParent.getId() + " has no type binding");
		} else {
			if (!this.ignoreType(typeBinding)) {
				this.onTypeAccess(node, typeBinding);
				
				IPackageBinding iPackage = typeBinding.getPackage();
				String fullName = iPackage.getName();
				int pos = fullName.length();
				while (pos > 0) {
					String moduleName = fullName.substring(0, pos);
					if (!this.ignoreModule(moduleName)) {
						this.onModuleAccess(node, moduleName);
					}
					pos = fullName.lastIndexOf('.', pos - 1);
				}
			}
		}
	}

	private void handleVariableBinding(ASTNode node, IVariableBinding variableBindig) {
		if (variableBindig == null) {
			StructuralPropertyDescriptor locationInParent = node.getLocationInParent();
			System.out.println(locationInParent.getId() + " has no variable binding");
		} else {
			this.onVariableAccess(node, variableBindig);
		}
	}

	private void handleFieldBinding(ASTNode node, IVariableBinding variableBindig) {
		if (variableBindig == null) {
			StructuralPropertyDescriptor locationInParent = node.getLocationInParent();
			System.out.println(locationInParent.getId() + " has no variable binding");
		} else {
			ITypeBinding declaringClass = variableBindig.getDeclaringClass();
			if (declaringClass != null && declaringClass.equals(this.myClass)) {
				this.onVariableAccess(node, variableBindig);
			}
		}
	}

	private void handleMethodBinding(ASTNode node, IMethodBinding methodBinding) {
		if (methodBinding == null) {
			StructuralPropertyDescriptor locationInParent = node.getLocationInParent();
			System.out.println(locationInParent.getId() + " has no method binding");
		} else {
			ITypeBinding declaringClass = methodBinding.getDeclaringClass();
			if (declaringClass != null) {
				this.onMethodAccess(node, methodBinding);
			}
		}
	}

	private boolean ignoreType(ITypeBinding typeBinding) {
		if (typeBinding.isPrimitive() || typeBinding.isArray() || typeBinding.getPackage() == null) {
			return true;
		}
		String typeId = typeBinding.getKey();
		return typeId.startsWith("Ljava/lang") || typeId.startsWith("Ljava/util");
	}
	
	private boolean ignoreModule(String moduleName) {
		return moduleName.equals("com") || moduleName.equals("org") || moduleName.equals("java") || moduleName.equals("javax");
	}

}
