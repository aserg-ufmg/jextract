package br.ufmg.dcc.labsoft.jextract.ranking;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

import br.ufmg.dcc.labsoft.jextract.generation.Settings;

public abstract class DependenciesAstVisitor extends ASTVisitor {

	private final ITypeBinding myClass;
	
	private final Settings settings; 
	
	public DependenciesAstVisitor(ITypeBinding myClass, Settings settings) {
		this.myClass = myClass;
		this.settings = settings;
	}

	@Override
	public boolean visit(MethodInvocation node) {
		IMethodBinding methodBinding = node.resolveMethodBinding();
		handleTypeBinding(node, methodBinding.getDeclaringClass(), false);
		handleMethodBinding(node, methodBinding);
		return true;
	}

	@Override
	public boolean visit(FieldAccess node) {
		IVariableBinding fieldBinding = node.resolveFieldBinding();
		handleTypeBinding(node, fieldBinding.getDeclaringClass(), false);
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
				handleTypeBinding(node, declaringClass, false);
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
		handleTypeBinding(node, typeBinding, true);
		return true;
	}

	@Override
	public boolean visit(VariableDeclarationStatement node) {
		ITypeBinding typeBinding = node.getType().resolveBinding();
		handleTypeBinding(node, typeBinding, true);
		//typeBinding.get
		//supertypes
		return true;
	}

	@Override
	public boolean visit(CatchClause node) {
		ITypeBinding typeBinding = node.getException().getType().resolveBinding();
		handleTypeBinding(node, typeBinding, true);
		return true;
	}

	@Override
	public boolean visit(MarkerAnnotation node) {
		ITypeBinding typeBinding = node.getTypeName().resolveTypeBinding();
		handleTypeBinding(node, typeBinding, true);
		return true;
	}

	@Override
	public boolean visit(CastExpression node) {
		Type type = node.getType();
		handleTypeBinding(node, type.resolveBinding(), true);
		return true;
	}

	@Override
	public boolean visit(TypeLiteral node) {
		Type type = node.getType();
		handleTypeBinding(node, type.resolveBinding(), true);
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

	private void handleTypeBinding(ASTNode node, ITypeBinding typeBinding, boolean includeTypeParameters) {
		if (typeBinding == null) {
			StructuralPropertyDescriptor locationInParent = node.getLocationInParent();
			//System.out.println(locationInParent.getId() + " has no type binding");
		} else {
			List<ITypeBinding> rawTypes = new ArrayList<ITypeBinding>();
			Set<String> dejavu = new HashSet<String>();
			this.appendRawTypes(rawTypes, dejavu, typeBinding, includeTypeParameters);
			for (ITypeBinding rawType : rawTypes) {
				if (!this.ignoreType(rawType)) {
					this.onTypeAccess(node, rawType);
					
					IPackageBinding iPackage = rawType.getPackage();
					String fullName = iPackage.getName();
					if (this.settings.splitParentPackages) {
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
	}

	private void appendRawTypes(List<ITypeBinding> rawTypes, Set<String> dejavu, ITypeBinding typeBinding, boolean includeTypeParameters) {
		String key = typeBinding.getKey();
		if (dejavu.contains(key)) {
			return;
		}
		dejavu.add(key);
		ITypeBinding erasure = typeBinding.getErasure();
		rawTypes.add(erasure);
		
		if (!includeTypeParameters || !this.settings.includeTypeArguments) {
			return;
		}
		
		ITypeBinding elementType = typeBinding.getElementType();
		if (elementType != null) {
			this.appendRawTypes(rawTypes, dejavu, elementType, includeTypeParameters);
		}
		
		ITypeBinding[] typeArguments = typeBinding.getTypeArguments();
		if (typeArguments != null) {
			for (ITypeBinding typeArgument : typeArguments) {
				this.appendRawTypes(rawTypes, dejavu, typeArgument, includeTypeParameters);
			}
		}
		
		ITypeBinding[] typeBounds = typeBinding.getTypeBounds();
		if (typeBounds != null) {
			for (ITypeBinding typeBound : typeBounds) {
				this.appendRawTypes(rawTypes, dejavu, typeBound, includeTypeParameters);
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
				if (this.settings.includeExternalFields || declaringClass.equals(this.myClass)) {
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
		if (this.settings.ignoreJavaLang && typeId.startsWith("Ljava/lang")) {
			return true;
		}
		if (this.settings.ignoreJavaUtil && typeId.startsWith("Ljava/util")) {
			return true;
		}
		return false;
	}
	
	private boolean ignoreModule(String moduleName) {
		return moduleName.equals("com") || moduleName.equals("org") || moduleName.equals("java") || moduleName.equals("javax");
	}

}
