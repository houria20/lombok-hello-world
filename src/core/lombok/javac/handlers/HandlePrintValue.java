
package lombok.javac.handlers;

import static lombok.core.handlers.HandlerUtil.handleFlagUsage;
import static lombok.javac.Javac.CTC_VOID;

import org.mangosdk.spi.ProviderFor;

import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.tree.JCTree.JCAnnotation;
import com.sun.tools.javac.tree.JCTree.JCBlock;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import com.sun.tools.javac.tree.JCTree.JCMethodInvocation;
import com.sun.tools.javac.tree.JCTree.JCModifiers;
import com.sun.tools.javac.tree.JCTree.JCStatement;
import com.sun.tools.javac.tree.JCTree.JCTypeParameter;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Name;

import lombok.ConfigurationKeys;
import lombok.PrintValue;
import lombok.core.AnnotationValues;
import lombok.javac.Javac;
import lombok.javac.Javac8BasedLombokOptions;
import lombok.javac.JavacAnnotationHandler;
import lombok.javac.JavacNode;
import lombok.javac.JavacTreeMaker;

@ProviderFor(JavacAnnotationHandler.class)
public class HandlePrintValue extends JavacAnnotationHandler<PrintValue> {
	
	@Override
	public void handle(AnnotationValues<PrintValue> annotation, JCAnnotation ast, JavacNode annotationNode) {
		// JavacHandlerUtil.markAnnotationAsProcessed(annotationNode,
		// HelloWorld.class);
		handleFlagUsage(annotationNode, ConfigurationKeys.PRINT_VALUE_FLAG_USAGE, "@PrintValue");
		Context context = annotationNode.getContext();
		Javac8BasedLombokOptions options = Javac8BasedLombokOptions.replaceWithDelombokOptions(context);
		options.deleteLombokAnnotations();
		JavacHandlerUtil.deleteAnnotationIfNeccessary(annotationNode, PrintValue.class);
		JavacHandlerUtil.deleteImportFromCompilationUnit(annotationNode, "lombok.AccessLevel");
		JavacNode typeNode = annotationNode.up();
		JCMethodDecl printValMethod = createPrintVal(typeNode);
		
		JavacHandlerUtil.injectMethod(typeNode, printValMethod);
		return;
	}
	
	private JCMethodDecl createPrintVal(JavacNode type) {
		JavacTreeMaker treeMaker = type.getTreeMaker();
		
		JCModifiers modifiers = treeMaker.Modifiers(Flags.PUBLIC);
		List<JCTypeParameter> methodGenericTypes = List.<JCTypeParameter>nil();
		JCExpression methodType = treeMaker.Type(Javac.createVoidType(treeMaker, CTC_VOID));
		Name methodName = type.toName("printVal");
		List<JCVariableDecl> methodParameters = List.<JCVariableDecl>nil();
		List<JCExpression> methodThrows = List.<JCExpression>nil();
		
		JCExpression printlnMethod = JavacHandlerUtil.chainDots(type, "System", "out", "println");
		List<JCExpression> printlnArgs = List.<JCExpression>of(treeMaker.Literal("val"));
		JCMethodInvocation printlnInvocation = treeMaker.Apply(List.<JCExpression>nil(), printlnMethod, printlnArgs);
		JCBlock methodBody = treeMaker.Block(0, List.<JCStatement>of(treeMaker.Exec(printlnInvocation)));
		
		JCExpression defaultValue = null;
		
		return treeMaker.MethodDef(modifiers, methodName, methodType, methodGenericTypes, methodParameters, methodThrows, methodBody, defaultValue);
	}
}
