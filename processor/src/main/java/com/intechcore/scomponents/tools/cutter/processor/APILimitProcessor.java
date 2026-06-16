package com.intechcore.scomponents.tools.cutter.processor;

import com.intechcore.scomponents.tools.cutter.annotations.APILogger;
import com.intechcore.scomponents.tools.cutter.annotations.BusinessLicenceRequired;
import com.intechcore.scomponents.tools.cutter.processor.helpers.JavaSource;
import com.intechcore.scomponents.tools.cutter.processor.helpers.ReplaceHelper;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.code.TypeTag;
import com.sun.tools.javac.model.JavacElements;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Name;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.ToolProvider;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Set;

public class APILimitProcessor extends AbstractProcessor {
    public static final String PATH_TO_BUSINESS_REQUIRED_FUNCTION =
            "com.intechcore.scomponents.api.utils.LoggerUtils.logBusinessLicenseRequiredMessage";
    public static final String REFLECT_METHOD = "java.lang.reflect.Method";
    private Messager messager;
    private Context context;
    private JavacElements elementUtils;
    private TreeMaker treeMaker;

    private ReplaceHelper helper;

    private HashMap<Symbol, Name> loggers;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);

        this.messager = processingEnv.getMessager();
        this.context = ((JavacProcessingEnvironment) processingEnv).getContext();
        this.elementUtils = (JavacElements) processingEnv.getElementUtils();
        this.treeMaker = TreeMaker.instance(this.context);

        this.helper = new ReplaceHelper(this.treeMaker, this.elementUtils);
        this.loggers = new HashMap<>();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> annotations = new LinkedHashSet<>();
        annotations.add(BusinessLicenceRequired.class.getCanonicalName());
        return annotations;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latest();
    }

    @Override
    public boolean process(Set<? extends TypeElement> typeElements, RoundEnvironment roundEnvironment) {
        if (roundEnvironment.processingOver()) {
            return false;
        }

        roundEnvironment.getElementsAnnotatedWith(APILogger.class)
                .stream()
                .map(Symbol.VarSymbol.class::cast)
                .forEach(loggerAnno -> this.loggers.put(loggerAnno.owner, loggerAnno.name));

        roundEnvironment.getElementsAnnotatedWith(BusinessLicenceRequired.class).forEach(this::replacingProcess);

        return true;
    }

    private void replacingProcess(javax.lang.model.element.Element element) {
        if (element.getKind() == ElementKind.METHOD) {
            this.replaceMethod(element);
            return;
        }
        this.messager.printMessage(Diagnostic.Kind.WARNING, element.getKind().toString() + " is not implemented.");
    }

    private void replaceMethod(javax.lang.model.element.Element element) {
        Symbol.MethodSymbol methodSymbol = (Symbol.MethodSymbol) element;
        String returnType = methodSymbol.getReturnType().asElement().toString();

        JCTree.JCMethodDecl jcMethodDecl = (JCTree.JCMethodDecl) this.elementUtils.getTree(element);
        this.treeMaker.pos = jcMethodDecl.pos;

        if ("java.util.concurrent.CompletableFuture".equals(returnType)) {
            this.completableFutureReplace(methodSymbol, jcMethodDecl);
            return;
        }

        if ("void".equals(returnType)) {
            this.voidReplace(methodSymbol, jcMethodDecl);
            return;
        }

        boolean isPrimitive = Character.isLowerCase(returnType.charAt(0)) && !returnType.contains(".");
        if (isPrimitive) {
            this.messager.printMessage(Diagnostic.Kind.WARNING, returnType + " is not implemented.");
            return;
        }

        this.typeReplace(methodSymbol, jcMethodDecl);

//        String newContent = jcMethodDecl.toString();
//        if (!this.isValidJavaCode(newContent)) {
//            this.messager.printMessage(Diagnostic.Kind.ERROR, newContent);
//        }
    }

    public static boolean isValidJavaCode(String code) {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        JavaFileObject file = new JavaSource(
                "Test", "class Test { " + code.replaceAll("@Override\\(\\)", "") + "}");
        Iterable<? extends JavaFileObject> compilationUnits = Arrays.asList(file);

        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        compiler.getTask(null, null, diagnostics, null, null, compilationUnits).call();
        return diagnostics.getDiagnostics().isEmpty();
    }

    private void voidReplace(Symbol.MethodSymbol methodSymbol, JCTree.JCMethodDecl jcMethodDecl) {
        Name loggerName = this.loggers.get(methodSymbol.owner);

        if (loggerName == null) {
            this.messager.printMessage(
                    Diagnostic.Kind.ERROR,
                    "For class " + methodSymbol.owner.toString() + " no logger field with annotation");
            return;
        }

        jcMethodDecl.body = this.treeMaker.Block(0, List.of(this.treeMaker.Exec(
                this.treeMaker.Apply(List.nil(),
                        this.helper.createClassExpression(PATH_TO_BUSINESS_REQUIRED_FUNCTION),
                        List.of(this.helper.createClassExpression("this." + loggerName))))));
    }

    private void typeReplace(Symbol.MethodSymbol methodSymbol, JCTree.JCMethodDecl jcMethodDecl) {
        Name loggerName = this.loggers.get(methodSymbol.owner);

        if (loggerName == null) {
            this.messager.printMessage(
                    Diagnostic.Kind.ERROR,
                    "For class " + methodSymbol.owner.toString() + " no logger field with annotation");
            return;
        }

        Type type = methodSymbol.getReturnType();

        boolean shouldReturnThis = jcMethodDecl.getBody().getStatements().stream()
                .anyMatch(jcStatement -> "return this;".equals(jcStatement.toString()));

        ArrayList<JCTree.JCStatement> bodyContent = new ArrayList<>();
        bodyContent.add(this.treeMaker.Exec(
                this.treeMaker.Apply(List.nil(),
                        this.helper.createClassExpression(PATH_TO_BUSINESS_REQUIRED_FUNCTION),
                        List.of(this.helper.createClassExpression("this." + loggerName)))));

        JCTree.JCReturn returnStatement;
        if (shouldReturnThis) {
            returnStatement = this.treeMaker.Return(this.helper.createClassExpression("this"));
        } else {
            JCTree.JCVariableDecl returnValue;
            if (!type.getTypeArguments().isEmpty()) {
                returnValue = this.getProxyVarDecl(type.asElement());
            } else {
                returnValue = this.getProxyVarDecl(type);
            }
            bodyContent.add(returnValue);
            returnStatement = this.treeMaker.Return(this.treeMaker.Ident(this.elementUtils.getName("returnedValue")));
        }

        bodyContent.add(returnStatement);

        jcMethodDecl.body = this.treeMaker.Block(0, List.from(bodyContent));
    }

    private void completableFutureReplace(Symbol.MethodSymbol methodSymbol, JCTree.JCMethodDecl jcMethodDecl) {
        Name loggerName = this.loggers.get(methodSymbol.owner);

        if (loggerName == null) {
            this.messager.printMessage(
                    Diagnostic.Kind.ERROR,
                    "For class " + methodSymbol.owner.toString() + " no logger field with annotation");
            return;
        }

        Type futureType = methodSymbol.getReturnType().getTypeArguments().get(0);

        JCTree.JCVariableDecl varDecl = this.getProxyVarDecl(futureType);

        jcMethodDecl.body = this.treeMaker.Block(0, List.of(this.treeMaker.Exec(
                        this.treeMaker.Apply(List.nil(),
                                this.helper.createClassExpression(PATH_TO_BUSINESS_REQUIRED_FUNCTION),
                                List.of(this.helper.createClassExpression("this." + loggerName)))),
                varDecl,
                this.treeMaker.Return(this.treeMaker.Apply(List.nil(),
                    this.helper.createClassExpression("java.util.concurrent.CompletableFuture.completedFuture"),
                    List.of(this.treeMaker.Ident(this.elementUtils.getName("returnedValue")))))));
    }

    private JCTree.JCVariableDecl getProxyVarDecl(Object futureType) {

        return this.treeMaker.VarDef(
                this.treeMaker.Modifiers(0, List.nil()),
                this.elementUtils.getName("returnedValue"),
                this.helper.createClassExpression(futureType.toString()),
                this.treeMaker.TypeCast(
                        this.helper.createClassExpression(futureType.toString()),
                        this.treeMaker.Apply(
                                List.nil(),
                                this.helper.createClassExpression("java.lang.reflect.Proxy.newProxyInstance"),
                                List.of(this.resolveClassLoader(),
                                        this.createClassArray(futureType),
                                        this.createAnonymousClass()
                                ))));
    }

    private JCTree.JCNewClass createAnonymousClass() {
        return this.treeMaker.NewClass(null, List.nil(),
                this.helper.createClassExpression(
                        "java.lang.reflect.InvocationHandler"),
                List.nil(),
                this.createInvocationHandlerLambda());
    }

    private JCTree.JCNewArray createClassArray(Object futureType) {
        return this.treeMaker.NewArray(
                this.treeMaker.Ident(this.elementUtils.getName("Class")),
                List.nil(),
                List.of(this.helper.createClassExpression(futureType + ".class"))
        );
    }

    private JCTree.JCMethodInvocation resolveClassLoader() {
        return this.treeMaker.Apply(
                List.nil(),
                this.treeMaker.Select(
                        this.treeMaker.Apply(
                                List.nil(),
                                this.treeMaker.Ident(this.elementUtils.getName("getClass")),
                                List.nil()),
                        this.elementUtils.getName(
                                "getClassLoader")),
                List.nil());
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
                                this.treeMaker.Ident(
                                        returnObjectName), List.nil(),
                                List.of(
                                        this.treeMaker.VarDef(
                                                this.createParameterModifier(),
                                                this.elementUtils.getName("proxy"),
                                                this.treeMaker.Ident(returnObjectName),
                                                null),
                                        this.treeMaker.VarDef(
                                                this.createParameterModifier(),
                                                this.elementUtils.getName("method"),
                                                this.helper.createClassExpression(REFLECT_METHOD),
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

    private JCTree.JCBlock resolveNullReturn() {
        return this.treeMaker.Block(
                0, List.of(
                        this.treeMaker.Return(
                                this.treeMaker.Literal(
                                        TypeTag.BOT,
                                        null)
                        )));
    }

    private JCTree.JCModifiers createParameterModifier() {
        return this.treeMaker.Modifiers(
                Flags.PARAMETER,
                List.nil());
    }

}
