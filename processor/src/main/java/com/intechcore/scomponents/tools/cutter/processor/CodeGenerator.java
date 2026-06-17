/*
 * Copyright (c) 2026-present, Intechcore GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.intechcore.scomponents.tools.cutter.processor;

import com.google.gson.Gson;
import com.intechcore.scomponents.tools.cutter.annotations.CutCode;
import com.intechcore.scomponents.tools.cutter.annotations.common.ParamType;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.code.TypeTag;
import com.sun.tools.javac.model.JavacElements;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Name;

import javax.annotation.processing.Messager;
import javax.tools.Diagnostic;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * The {@code CodeGenerator} class is responsible for generating and manipulating Java code
 * using the {@code com.sun.tools.javac} API. It provides utility methods for creating
 * Abstract Syntax Tree (AST) elements, such as expressions, statements, and method bodies,
 * in the context of the {@link CutCodeProcessor}.
 *
 * This class primarily helps in replacing original method bodies with generated code
 * based on {@link com.intechcore.scomponents.tools.cutter.annotations.CutCode} annotations.
 */
public class CodeGenerator {
    private final TreeMaker treeMaker;
    private final JavacElements elementUtils;
    private final Messager messager;
    private final Map<String, ProfileConfig> profiles;

    /**
     * Constructs a new {@code CodeGenerator}.
     *
     * @param treeMaker    The {@code TreeMaker} instance for creating AST nodes.
     * @param elementUtils The {@code JavacElements} instance for utility methods related to elements.
     * @param messager     The {@code Messager} instance for reporting errors and warnings.
     * @param profiles     A map of {@link ProfileConfig} instances, keyed by profile name,
     *                     used to merge annotation data.
     */
    public CodeGenerator(TreeMaker treeMaker,
                         JavacElements elementUtils,
                         Messager messager,
                         Map<String, ProfileConfig> profiles) {
        this.treeMaker = treeMaker;
        this.elementUtils = elementUtils;
        this.messager = messager;
        this.profiles = profiles;
    }

    /**
     * Creates a {@code JCTree.JCExpression} representing a fully qualified class name or an expression.
     * For example, "java.lang.String" would be converted into a selection expression
     * representing {@code java.lang.String}.
     *
     * @param className The fully qualified name of the class or the expression string.
     * @return A {@code JCTree.JCExpression} representing the class or expression.
     */
    public JCTree.JCExpression createClassExpression(String className) {
        String[] split = className.split("\\.");

        JCTree.JCExpression select = this.treeMaker.Ident(this.elementUtils.getName(split[0]));

        for (int i = 1; i < split.length; i++) {
            select = this.treeMaker.Select(select, this.elementUtils.getName(split[i]));
        }

        return select;
    }

    /**
     * Creates a default return statement based on the method's return type.
     * For {@code void} methods, it returns {@code return;}.
     * For primitive types, it returns the default value (e.g., 0 for int, false for boolean).
     * For object types, it returns {@code null} or {@code this} if {@code returnThisIfFound} is enabled
     * in the {@link ProcessingConfig} and the original method body contained "return this;".
     *
     * @param jcMethodDecl The {@code JCTree.JCMethodDecl} representing the method.
     * @param config       The {@link ProcessingConfig} providing processing options.
     * @return A {@code JCTree.JCStatement} representing the default return statement.
     */
    public JCTree.JCStatement createDefaultReturnStatement(JCTree.JCMethodDecl jcMethodDecl, ProcessingConfig config) {
        Type returnType = jcMethodDecl.getReturnType().type;
        JCTree.JCStatement result;
        if (returnType.getTag() == TypeTag.VOID) {
            // void – just return;
            result = this.treeMaker.Return(null);
        } else if (returnType.isPrimitive()) {
            JCTree.JCLiteral literal;
            switch (returnType.getTag()) {
                case BOOLEAN:
                    literal = this.treeMaker.Literal(TypeTag.BOOLEAN, 0); // false
                    break;
                case BYTE:
                    literal = this.treeMaker.Literal(TypeTag.BYTE, 0);
                    break;
                case SHORT:
                    literal = this.treeMaker.Literal(TypeTag.SHORT, 0);
                    break;
                case INT:
                    literal = this.treeMaker.Literal(TypeTag.INT, 0);
                    break;
                case LONG:
                    literal = this.treeMaker.Literal(TypeTag.LONG, 0L);
                    break;
                case CHAR:
                    literal = this.treeMaker.Literal(TypeTag.CHAR, 0); // '\0'
                    break;
                case FLOAT:
                    literal = this.treeMaker.Literal(TypeTag.FLOAT, 0.0f);
                    break;
                case DOUBLE:
                    literal = this.treeMaker.Literal(TypeTag.DOUBLE, 0.0D);
                    break;
                default:
                    this.messager.printMessage(Diagnostic.Kind.ERROR, "Unknown primitive type: " + returnType);
                    throw new AssertionError("Unknown primitive type: " + returnType);
            }
            result = this.treeMaker.Return(literal);
        } else {

            boolean shouldReturnThis = config.returnThisIfFound() && jcMethodDecl.getBody().getStatements().stream()
                    .anyMatch(jcStatement -> "return this;".equals(jcStatement.toString()));
            JCTree.JCExpression returnLiteral;
            if (shouldReturnThis) {
                returnLiteral = this.createClassExpression("this");
            } else {
                returnLiteral = this.treeMaker.Literal(TypeTag.BOT, null);
            }
            result = this.treeMaker.Return(returnLiteral);

//            JCTree.JCVariableDecl returnValue;
//             if (!returnType.getTypeArguments().isEmpty()) {
//                 returnValue = this.getProxyVarDecl(returnType.asElement());
//             } else {
//                 returnValue = this.getProxyVarDecl(returnType);
//             }
//             bodyContent.add(returnValue);
//             returnStatement = this.treeMaker.Return(this.treeMaker.Ident(this.elementUtils.getName("returnedValue")));
//         bodyContent.add(returnStatement);
//         finalStatements = List.from(bodyContent);
        }

        return result;
    }

    /**
     * Replaces the body of a given method declaration with new statements generated
     * based on {@link CutCode} annotations. This method iterates through the annotations,
     * applies profile merging if specified, generates the replacement call expressions,
     * and constructs the new method body.
     *
     * @param jcMethodDecl    The {@code JCTree.JCMethodDecl} whose body is to be replaced.
     * @param annotations     An array of {@link CutCode} annotations applied to the method.
     * @param finalStatements A list of {@code JCTree.JCStatement} to be appended to the generated body,
     *                        typically a return statement.
     * @param config          The {@link ProcessingConfig} providing processing options.
     */
    public void replaceBody(JCTree.JCMethodDecl jcMethodDecl,
                            CutCode[] annotations,
                            List<JCTree.JCStatement> finalStatements,
                            ProcessingConfig config) {
        this.treeMaker.pos = jcMethodDecl.pos;
        List<JCTree.JCStatement> bodyItems = List.from(Arrays.stream(annotations).map(annotation -> {
                    if (annotation.withCall() == null && annotation.profile() == null) {
                        return null;
                    }
                    if (annotation.profile() != null && (!annotation.profile().isEmpty())) {
                        if (!this.profiles.containsKey(annotation.profile())) {
                            this.messager.printMessage(
                                    Diagnostic.Kind.ERROR,
                                    "Code Cutter profile " + annotation.profile() + " not exists"
                            );
                        } else {
                            annotation = this.profiles.get(annotation.profile()).mergeWith(annotation);
                            if (config.logProcessing()) {
                                this.messager.printMessage(Diagnostic.Kind.NOTE, "Merged profile: " + new Gson().toJson(annotation));
                            }
                        }
                    }

                    final CutCode annotationClosure = annotation;
                    if (annotationClosure.withCall() == null || annotationClosure.withCall().isEmpty()) {
                        return null;
                    }
                    return this.treeMaker.Exec(
                            this.treeMaker.Apply(
                                    List.nil(),
                                    createClassExpression(annotationClosure.withCall()),
                                    List.from(
                                            IntStream.range(0, annotationClosure.callParams().length)
                                                    .mapToObj(paramInd -> {
                                                        String param = annotationClosure.callParams()[paramInd];
                                                        if (param == null || param.isEmpty()) {
                                                            return null;
                                                        }
                                                        JCTree.JCExpression expression;
                                                        if (annotationClosure.callParamsTypes() != null
                                                                && annotationClosure.callParamsTypes().length > paramInd
                                                                && annotationClosure.callParamsTypes()[paramInd] == ParamType.VARIABLE) {
                                                            expression = createClassExpression(param);
                                                        } else {
                                                            expression = this.treeMaker.Literal(param);
                                                        }
                                                        return expression;
                                                    })
                                                    .filter(Objects::nonNull)
                                                    .collect(Collectors.toList())
                                    )
                            ));
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList()));

        bodyItems = List.from(Stream.concat(bodyItems.stream(), finalStatements.stream()).collect(Collectors.toList()));

        jcMethodDecl.body = this.treeMaker.Block(0, bodyItems);

        if (config.logProcessing()) {
            this.messager.printMessage(Diagnostic.Kind.NOTE, "Result method \"" + jcMethodDecl.name + "\" " + jcMethodDecl.toString());
        }
    }

    /**
     * Creates a {@code JCTree.JCThrow} statement that throws a new
     * {@code java.lang.UnsupportedOperationException}.
     *
     * @return A {@code JCTree.JCThrow} statement.
     */
    public JCTree.JCThrow createThrowNewUnsupportedOperationException() {
        // new java.lang.UnsupportedOperationException()
        JCTree.JCExpression exceptionClass = this.createClassExpression("java.lang.UnsupportedOperationException");
        JCTree.JCNewClass newExpr = this.treeMaker.NewClass(null, List.nil(), exceptionClass, List.nil(), null);

        return this.treeMaker.Throw(newExpr);
    }

    //

    /**
     * Generates a {@code JCTree.JCVariableDecl} for a proxy instance.
     * This method is intended to create a variable declaration that uses
     * {@code java.lang.reflect.Proxy.newProxyInstance} to create an object
     * that can be returned from a method, particularly for handling specific return types
     * like {@code CompletableFuture}.
     *
     * @param futureType The type for which the proxy variable declaration is being created.
     *                   This is typically the generic type argument of a {@code CompletableFuture}.
     * @return A {@code JCTree.JCVariableDecl} representing the proxy variable.
     *
     * <p>Example for "Void", instead of "Void" will be current type from incoming CompletableFuture</p>
     * <pre>{@code
     *     private CompletableFuture<Void> CompletableFutureWithVoid(String param1, int param2) {
     *         System.out.println("from profile");
     *         java.lang.Void returnedValue = (java.lang.Void)java.lang.reflect.Proxy.newProxyInstance(
     *                 getClass().getClassLoader(),
     *                 new Class[]{java.lang.Void.class},
     *                 new java.lang.reflect.InvocationHandler() {
     *                     @Override()
     *                     public Object invoke(Object proxy, java.lang.reflect.Method method, Object[] args) throws Throwable {
     *                         return null;
     *                     }
     *                 });
     *         return java.util.concurrent.CompletableFuture.completedFuture(returnedValue);
     *     }
     * }</pre>
     */
    private JCTree.JCVariableDecl getProxyVarDecl(Object futureType) {
        return this.treeMaker.VarDef(
                this.treeMaker.Modifiers(0, List.nil()),
                this.elementUtils.getName("returnedValue"),
                this.createClassExpression(futureType.toString()),
                this.treeMaker.TypeCast(
                        this.createClassExpression(futureType.toString()),
                        this.treeMaker.Apply(
                                List.nil(),
                                this.createClassExpression("java.lang.reflect.Proxy.newProxyInstance"),
                                List.of(this.resolveClassLoader(),
                                        this.createClassArray(futureType),
                                        this.createAnonymousClass()
                                )
                        )
                )
        );
    }

    /**
     * Creates a {@code JCTree.JCMethodInvocation} that represents {@code getClass().getClassLoader()}.
     * This is used as an argument for {@code Proxy.newProxyInstance}.
     *
     * @return A {@code JCTree.JCMethodInvocation} for obtaining the current class loader.
     */
    private JCTree.JCMethodInvocation resolveClassLoader() {
        return this.treeMaker.Apply(
                List.nil(),
                this.treeMaker.Select(
                        this.treeMaker.Apply(
                                List.nil(),
                                this.treeMaker.Ident(this.elementUtils.getName("getClass")),
                                List.nil()
                        ),
                        this.elementUtils.getName("getClassLoader")),
                List.nil()
        );
    }

    /**
     * Creates a {@code JCTree.JCNewArray} representing an array of {@code Class<?>} objects.
     * Specifically, it creates {@code new Class[]{ futureType.class }} for use in proxy creation.
     *
     * @param futureType The type for which to create the Class array (e.g., the generic type of CompletableFuture).
     * @return A {@code JCTree.JCNewArray} representing the Class array.
     */
    private JCTree.JCNewArray createClassArray(Object futureType) {
        return this.treeMaker.NewArray(
                this.treeMaker.Ident(this.elementUtils.getName("Class")),
                List.nil(),
                List.of(this.createClassExpression(futureType + ".class"))
        );
    }

    /**
     * Creates a {@code JCTree.JCNewClass} representing an anonymous class
     * implementing {@code java.lang.reflect.InvocationHandler}.
     * This is used as the invocation handler for the proxy instance.
     *
     * @return A {@code JCTree.JCNewClass} representing the anonymous InvocationHandler.
     */
    private JCTree.JCNewClass createAnonymousClass() {
        return this.treeMaker.NewClass(null, List.nil(),
                this.createClassExpression("java.lang.reflect.InvocationHandler"),
                List.nil(),
                this.createInvocationHandlerLambda()
        );
    }

    /**
     * Creates a {@code JCTree.JCClassDecl} representing the anonymous class body
     * for an {@code InvocationHandler}. It defines the {@code invoke} method
     * which simply returns {@code null}.
     *
     * @return A {@code JCTree.JCClassDecl} for the InvocationHandler's anonymous class.
     */
    private JCTree.JCClassDecl createInvocationHandlerLambda() {
        JCTree.JCAnnotation overrideAnnotation = this.treeMaker.Annotation(
                this.treeMaker.Ident(this.elementUtils.getName("Override")),
                List.nil());

        Name returnObjectName = this.elementUtils.getName("Object");
        Name anonymousFunctionName = this.elementUtils.getName("invoke");

        return this.treeMaker.AnonymousClassDef(
                this.treeMaker.Modifiers(0),
                List.of(
                        this.treeMaker.MethodDef(
                                this.treeMaker.Modifiers(Flags.PUBLIC, List.of(overrideAnnotation)),
                                anonymousFunctionName,
                                this.treeMaker.Ident(returnObjectName),
                                List.nil(),
                                List.of(
                                        this.treeMaker.VarDef(
                                                this.createParameterModifier(),
                                                this.elementUtils.getName("proxy"),
                                                this.treeMaker.Ident(returnObjectName),
                                                null),
                                        this.treeMaker.VarDef(
                                                this.createParameterModifier(),
                                                this.elementUtils.getName("method"),
                                                this.createClassExpression("java.lang.reflect.Method"),
                                                null),
                                        this.treeMaker.VarDef(
                                                this.createParameterModifier(),
                                                this.elementUtils.getName("args"),
                                                this.treeMaker.TypeArray(this.treeMaker.Ident(returnObjectName)),
                                                null)),
                                List.of(this.treeMaker.Ident(this.elementUtils.getName("Throwable"))),
                                this.resolveNullReturn(),
                                null)
                )
        );
    }

    /**
     * Creates {@code JCTree.JCModifiers} for a method parameter, typically with the {@code PARAMETER} flag.
     *
     * @return A {@code JCTree.JCModifiers} instance.
     */
    private JCTree.JCModifiers createParameterModifier() {
        return this.treeMaker.Modifiers(Flags.PARAMETER, List.nil());
    }

    /**
     * Creates a {@code JCTree.JCBlock} containing a return statement for {@code null}.
     * This is used in the generated {@code InvocationHandler}'s {@code invoke} method.
     *
     * @return A {@code JCTree.JCBlock} with {@code return null;}.
     */
    private JCTree.JCBlock resolveNullReturn() {
        return this.treeMaker.Block(
                0,
                List.of(this.treeMaker.Return(this.treeMaker.Literal(TypeTag.BOT, null)))
        );
    }
}
