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

public class CodeGenerator {
    private final TreeMaker treeMaker;
    private final JavacElements elementUtils;
    private final Messager messager;
    private final Map<String, ProfileConfig> profiles;

    public CodeGenerator(TreeMaker treeMaker,
                         JavacElements elementUtils,
                         Messager messager,
                         Map<String, ProfileConfig> profiles) {
        this.treeMaker = treeMaker;
        this.elementUtils = elementUtils;
        this.messager = messager;
        this.profiles = profiles;
    }

    public JCTree.JCExpression createClassExpression(String className) {
        String[] split = className.split("\\.");

        JCTree.JCExpression select = this.treeMaker.Ident(this.elementUtils.getName(split[0]));

        for (int i = 1; i < split.length; i++) {
            select = this.treeMaker.Select(select, this.elementUtils.getName(split[i]));
        }

        return select;
    }

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

    public JCTree.JCThrow createThrowNewUnsupportedOperationException() {
        // new java.lang.UnsupportedOperationException()
        JCTree.JCExpression exceptionClass = this.createClassExpression("java.lang.UnsupportedOperationException");
        JCTree.JCNewClass newExpr = this.treeMaker.NewClass(null, List.nil(), exceptionClass, List.nil(), null);

        return this.treeMaker.Throw(newExpr);
    }

    // Example for "Void", instead of "Void" will be current type from incoming CompletableFuture
//    private CompletableFuture<Void> CompletableFutureWithVoid(String param1, int param2) {
//        System.out.println("from profile");
//        java.lang.Void returnedValue = (java.lang.Void)java.lang.reflect.Proxy.newProxyInstance(
//                getClass().getClassLoader(),
//                new Class[]{java.lang.Void.class},
//                new java.lang.reflect.InvocationHandler() {
//                    @Override()
//                    public Object invoke(Object proxy, java.lang.reflect.Method method, Object[] args) throws Throwable {
//                        return null;
//                    }
//                });
//        return java.util.concurrent.CompletableFuture.completedFuture(returnedValue);
//    }
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

    private JCTree.JCNewArray createClassArray(Object futureType) {
        return this.treeMaker.NewArray(
                this.treeMaker.Ident(this.elementUtils.getName("Class")),
                List.nil(),
                List.of(this.createClassExpression(futureType + ".class"))
        );
    }

    private JCTree.JCNewClass createAnonymousClass() {
        return this.treeMaker.NewClass(null, List.nil(),
                this.createClassExpression("java.lang.reflect.InvocationHandler"),
                List.nil(),
                this.createInvocationHandlerLambda()
        );
    }

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

    private JCTree.JCModifiers createParameterModifier() {
        return this.treeMaker.Modifiers(Flags.PARAMETER, List.nil());
    }

    private JCTree.JCBlock resolveNullReturn() {
        return this.treeMaker.Block(
                0,
                List.of(this.treeMaker.Return(this.treeMaker.Literal(TypeTag.BOT, null)))
        );
    }
}

