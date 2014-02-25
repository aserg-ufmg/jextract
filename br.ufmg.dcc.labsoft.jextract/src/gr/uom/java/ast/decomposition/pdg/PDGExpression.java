package gr.uom.java.ast.decomposition.pdg;

import gr.uom.java.ast.CreationObject;
import gr.uom.java.ast.MethodInvocationObject;
import gr.uom.java.ast.SuperMethodInvocationObject;
import gr.uom.java.ast.TypeObject;
import gr.uom.java.ast.decomposition.AbstractExpression;
import gr.uom.java.ast.decomposition.AbstractVariable;
import gr.uom.java.ast.decomposition.CompositeVariable;
import gr.uom.java.ast.decomposition.PlainVariable;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.dom.VariableDeclaration;

public class PDGExpression {
	private Set<AbstractVariable> declaredVariables;
	private Set<AbstractVariable> definedVariables;
	private Set<AbstractVariable> usedVariables;
	private Set<TypeObject> createdTypes;
	private Set<String> thrownExceptionTypes;
	
	public PDGExpression(AbstractExpression expression, Set<VariableDeclaration> variableDeclarationsInMethod) {
		this.declaredVariables = new LinkedHashSet<AbstractVariable>();
		this.definedVariables = new LinkedHashSet<AbstractVariable>();
		this.usedVariables = new LinkedHashSet<AbstractVariable>();
		this.createdTypes = new LinkedHashSet<TypeObject>();
		this.thrownExceptionTypes = new LinkedHashSet<String>();
		determineDefinedAndUsedVariables(expression);
	}

	public boolean definesLocalVariable(AbstractVariable variable) {
		return definedVariables.contains(variable);
	}

	public boolean usesLocalVariable(AbstractVariable variable) {
		return usedVariables.contains(variable);
	}

	private void determineDefinedAndUsedVariables(AbstractExpression expression) {
		List<CreationObject> creations = expression.getCreations();
		for(CreationObject creation : creations) {
			createdTypes.add(creation.getType());
		}
		for(PlainVariable variable : expression.getDeclaredLocalVariables()) {
			declaredVariables.add(variable);
			definedVariables.add(variable);
		}
		for(PlainVariable variable : expression.getDefinedLocalVariables()) {
			definedVariables.add(variable);
		}
		for(PlainVariable variable : expression.getUsedLocalVariables()) {
			usedVariables.add(variable);
		}
		Map<AbstractVariable, LinkedHashSet<MethodInvocationObject>> invokedMethodsThroughLocalVariables = expression.getInvokedMethodsThroughLocalVariables();
		for(AbstractVariable variable : invokedMethodsThroughLocalVariables.keySet()) {
			LinkedHashSet<MethodInvocationObject> methodInvocations = invokedMethodsThroughLocalVariables.get(variable);
			for(MethodInvocationObject methodInvocationObject : methodInvocations) {
				thrownExceptionTypes.addAll(methodInvocationObject.getThrownExceptions());
				processArgumentsOfInternalMethodInvocation(methodInvocationObject, variable);
			}
		}
		Map<AbstractVariable, LinkedHashSet<MethodInvocationObject>> invokedMethodsThroughParameters = expression.getInvokedMethodsThroughParameters();
		for(AbstractVariable variable : invokedMethodsThroughParameters.keySet()) {
			LinkedHashSet<MethodInvocationObject> methodInvocations = invokedMethodsThroughParameters.get(variable);
			for(MethodInvocationObject methodInvocationObject : methodInvocations) {
				thrownExceptionTypes.addAll(methodInvocationObject.getThrownExceptions());
				processArgumentsOfInternalMethodInvocation(methodInvocationObject, variable);
			}
		}
		
		for(PlainVariable field : expression.getDefinedFieldsThroughThisReference()) {
			definedVariables.add(field);
		}
		for(PlainVariable field : expression.getUsedFieldsThroughThisReference()) {
			usedVariables.add(field);
		}
		for(AbstractVariable field : expression.getDefinedFieldsThroughFields()) {
			definedVariables.add(field);
		}
		for(AbstractVariable field : expression.getUsedFieldsThroughFields()) {
			usedVariables.add(field);
		}
		for(AbstractVariable field : expression.getDefinedFieldsThroughParameters()) {
			definedVariables.add(field);
		}
		for(AbstractVariable field : expression.getUsedFieldsThroughParameters()) {
			usedVariables.add(field);
		}
		for(AbstractVariable field : expression.getDefinedFieldsThroughLocalVariables()) {
			definedVariables.add(field);
		}
		for(AbstractVariable field : expression.getUsedFieldsThroughLocalVariables()) {
			usedVariables.add(field);
		}
		Map<AbstractVariable, LinkedHashSet<MethodInvocationObject>> invokedMethodsThroughFields = expression.getInvokedMethodsThroughFields();
		for(AbstractVariable variable : invokedMethodsThroughFields.keySet()) {
			LinkedHashSet<MethodInvocationObject> methodInvocations = invokedMethodsThroughFields.get(variable);
			for(MethodInvocationObject methodInvocationObject : methodInvocations) {
				thrownExceptionTypes.addAll(methodInvocationObject.getThrownExceptions());
				processArgumentsOfInternalMethodInvocation(methodInvocationObject, variable);
			}
		}
		for(MethodInvocationObject methodInvocationObject : expression.getInvokedMethodsThroughThisReference()) {
			thrownExceptionTypes.addAll(methodInvocationObject.getThrownExceptions());
			processArgumentsOfInternalMethodInvocation(methodInvocationObject, null);
		}
		for(MethodInvocationObject methodInvocationObject : expression.getInvokedStaticMethods()) {
			thrownExceptionTypes.addAll(methodInvocationObject.getThrownExceptions());
			processArgumentsOfInternalMethodInvocation(methodInvocationObject, null);
		}
		List<SuperMethodInvocationObject> superMethodInvocations = expression.getSuperMethodInvocations();
		for(SuperMethodInvocationObject superMethodInvocationObject : superMethodInvocations) {
			thrownExceptionTypes.addAll(superMethodInvocationObject.getThrownExceptions());
		}
	}

	private void processArgumentsOfInternalMethodInvocation(MethodInvocationObject methodInvocationObject, AbstractVariable variable) {
		if(variable != null) {
			//create pseudo state variable
			String originClass = methodInvocationObject.getOriginClassName();
			String variableName = "state";
			String variableBindingKey = variableName + originClass;
			String variableType = "String";
			PlainVariable pseudoVariable = new PlainVariable(variableBindingKey, variableName, variableType, true, false);
			AbstractVariable composite = composeVariable(variable, pseudoVariable);
			definedVariables.add(composite);
			usedVariables.add(composite);
		}
	}
	
	private AbstractVariable composeVariable(AbstractVariable leftSide, AbstractVariable rightSide) {
		if(leftSide instanceof CompositeVariable) {
			CompositeVariable leftSideCompositeVariable = (CompositeVariable)leftSide;
			PlainVariable finalVariable = leftSideCompositeVariable.getFinalVariable();
			CompositeVariable newRightSide = new CompositeVariable(finalVariable.getVariableBindingKey(), finalVariable.getVariableName(),
					finalVariable.getVariableType(), finalVariable.isField(), finalVariable.isParameter(), rightSide);
			AbstractVariable newLeftSide = leftSideCompositeVariable.getLeftPart();
			return composeVariable(newLeftSide, newRightSide);
		}
		else {
			return new CompositeVariable(leftSide.getVariableBindingKey(), leftSide.getVariableName(),
					leftSide.getVariableType(), leftSide.isField(), leftSide.isParameter(), rightSide);
		}
	}
}
