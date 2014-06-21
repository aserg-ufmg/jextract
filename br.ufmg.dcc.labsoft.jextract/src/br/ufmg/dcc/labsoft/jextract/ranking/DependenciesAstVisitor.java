package br.ufmg.dcc.labsoft.jextract.ranking;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CastExpression;
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
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeLiteral;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

public abstract class DependenciesAstVisitor extends ASTVisitor {

	private final ITypeBinding myClass;
	private final boolean includeExternalFields = true;
	private final boolean ignoreJavaLang = false;
	private final boolean ignoreJavaUtil = false;
	private final boolean splitParentPackages = true;

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

	@Override
	public boolean visit(CastExpression node) {
		Type type = node.getType();
		handleTypeBinding(node, type.resolveBinding());
		return true;
	}

	@Override
	public boolean visit(TypeLiteral node) {
		Type type = node.getType();
		handleTypeBinding(node, type.resolveBinding());
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
			//System.out.println(locationInParent.getId() + " has no type binding");
		} else {
			if (!this.ignoreType(typeBinding)) {
				this.onTypeAccess(node, typeBinding);
				
				IPackageBinding iPackage = typeBinding.getPackage();
				String fullName = iPackage.getName();
				if (this.splitParentPackages) {
					int pos = fullName.length();
					while (pos > 0) {
						String moduleName = fullName.substring(0, pos);
						if (!this.ignoreModule(moduleName)) {
							this.onModuleAccess(node, moduleName);
						}
						pos = fullName.lastIndexOf('.', pos - 1);
					}
				} else {
					String moduleName = fullName;
					if (!this.ignoreModule(moduleName)) {
						this.onModuleAccess(node, moduleName);
					}
				}
			}
		}
	}

	private void handleVariableBinding(ASTNode node, IVariableBinding variableBindig) {
		if (variableBindig == null) {
			StructuralPropertyDescriptor locationInParent = node.getLocationInParent();
			//System.out.println(locationInParent.getId() + " has no variable binding");
		} else {
			this.onVariableAccess(node, variableBindig);
		}
	}

	private void handleFieldBinding(ASTNode node, IVariableBinding variableBindig) {
		if (variableBindig == null) {
			StructuralPropertyDescriptor locationInParent = node.getLocationInParent();
			//System.out.println(locationInParent.getId() + " has no field binding");
		} else {
			ITypeBinding declaringClass = variableBindig.getDeclaringClass();
			if (declaringClass != null) {
				if (this.includeExternalFields || declaringClass.equals(this.myClass)) {
					this.onVariableAccess(node, variableBindig);
				}
			}
		}
	}

	private void handleMethodBinding(ASTNode node, IMethodBinding methodBinding) {
		if (methodBinding == null) {
			StructuralPropertyDescriptor locationInParent = node.getLocationInParent();
			//System.out.println(locationInParent.getId() + " has no method binding");
		} else {
			ITypeBinding declaringClass = methodBinding.getDeclaringClass();
			if (declaringClass != null) {
				this.onMethodAccess(node, methodBinding);
			}
		}
	}

	private boolean ignoreType(ITypeBinding typeBinding) {
		if (/*typeBinding.isPrimitive() || typeBinding.isArray() || */typeBinding.getPackage() == null) {
			return true;
		}
		String typeId = typeBinding.getKey();
		if (this.ignoreJavaLang && typeId.startsWith("Ljava/lang")) {
			return true;
		}
		if (this.ignoreJavaUtil && typeId.startsWith("Ljava/util")) {
			return true;
		}
		return false;
	}
	
	private boolean ignoreModule(String moduleName) {
		return moduleName.equals("com") || moduleName.equals("org") || moduleName.equals("java") || moduleName.equals("javax");
	}

}
