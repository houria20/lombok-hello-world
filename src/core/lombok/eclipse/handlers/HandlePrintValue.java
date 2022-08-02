/*
 * Copyright (C) 2009-2014 The Project Lombok Authors.
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package lombok.eclipse.handlers;

import static lombok.core.handlers.HandlerUtil.handleFlagUsage;
import static lombok.eclipse.Eclipse.ECLIPSE_DO_NOT_TOUCH_FLAG;
import static lombok.eclipse.handlers.EclipseHandlerUtil.*;

import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.MessageSend;
import org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.NameReference;
import org.eclipse.jdt.internal.compiler.ast.Statement;
import org.eclipse.jdt.internal.compiler.ast.StringLiteral;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.lookup.TypeIds;
import org.mangosdk.spi.ProviderFor;

import lombok.AccessLevel;
import lombok.ConfigurationKeys;
import lombok.PrintValue;
import lombok.core.AnnotationValues;
import lombok.eclipse.EclipseAnnotationHandler;
import lombok.eclipse.EclipseNode;

@ProviderFor(EclipseAnnotationHandler.class) public class HandlePrintValue extends EclipseAnnotationHandler<PrintValue> {
	
	@Override public void handle(AnnotationValues<PrintValue> annotation, Annotation ast, EclipseNode annotationNode) {
		System.out.println("Hiiiiiiiiiiiii");
		handleFlagUsage(annotationNode, ConfigurationKeys.PRINT_VALUE_FLAG_USAGE, "@PrintValue");
		System.out.println("222222222222222222");
		EclipseHandlerUtil.unboxAndRemoveAnnotationParameter(ast, "onType", "@PrintValue(onType=", annotationNode);
		EclipseNode typeNode = annotationNode.up();
		MethodDeclaration printValMethod = createPrintVal(typeNode, annotationNode, annotationNode.get(), ast);
		injectMethod(typeNode, printValMethod);
		return;
	}
	
	private MethodDeclaration createPrintVal(EclipseNode typeNode, EclipseNode errorNode, ASTNode astNode, Annotation source) {
		TypeDeclaration typeDecl = (TypeDeclaration) typeNode.get();
		
		MethodDeclaration method = new MethodDeclaration(typeDecl.compilationResult);
		setGeneratedBy(method, astNode);
		method.annotations = null;
		method.modifiers = toEclipseModifier(AccessLevel.PUBLIC);
		method.typeParameters = null;
		method.returnType = TypeReference.baseTypeReference(TypeIds.T_void, 0);
		method.selector = "printVal".toCharArray();
		method.arguments = null;
		method.binding = null;
		method.thrownExceptions = null;
		method.bits |= ECLIPSE_DO_NOT_TOUCH_FLAG;
		
		NameReference systemOutReference = createNameReference("System.out", source);
		Expression[] printlnArguments = new Expression[] {new StringLiteral("Hiiii".toCharArray(), astNode.sourceStart, astNode.sourceEnd, 0)};
		
		MessageSend printlnInvocation = new MessageSend();
		printlnInvocation.arguments = printlnArguments;
		printlnInvocation.receiver = systemOutReference;
		printlnInvocation.selector = "println".toCharArray();
		setGeneratedBy(printlnInvocation, source);
		
		method.bodyStart = method.declarationSourceStart = method.sourceStart = astNode.sourceStart;
		method.bodyEnd = method.declarationSourceEnd = method.sourceEnd = astNode.sourceEnd;
		method.statements = new Statement[] {printlnInvocation};
		return method;
	}
	
}
