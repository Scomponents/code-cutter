package com.intechcore.scomponents.tools.cutter.processor;

import com.google.gson.Gson;
import com.intechcore.scomponents.tools.cutter.annotations.CutCode;
import com.intechcore.scomponents.tools.cutter.annotations.CutCodeProcessConfig;
import com.intechcore.scomponents.tools.cutter.annotations.common.CutCodes;
import com.sun.tools.javac.code.TypeTag;
import com.sun.tools.javac.model.JavacElements;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Names;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CutCodeProcessor extends AbstractProcessor {
    private static final String SETTINGS_CONFIG_KEY = "INTECHCORE_CUTTER_SETTINGS";
    private static final String PROFILES_CONFIG_KEY = "INTECHCORE_CUTTER_PROFILES";

    private Messager messager;
    private JavacElements elementUtils;
    private TreeMaker treeMaker;
    private CodeGenerator helper;

    private ProcessingConfig config = new ProcessingConfig();

    public CutCodeProcessor() {
    }

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        this.messager = processingEnv.getMessager();
        this.elementUtils = (JavacElements) processingEnv.getElementUtils();
        Context context = ((JavacProcessingEnvironment) processingEnv).getContext();
        this.treeMaker = TreeMaker.instance(context);

        Map<String, ProfileConfig> profiles = new HashMap<>();
        Map<String, String> options = processingEnv.getOptions();
        if (options.containsKey(PROFILES_CONFIG_KEY)) {
            Gson gson = new Gson();
            ProfileConfig[] profilesConfig = gson.fromJson(options.get(PROFILES_CONFIG_KEY), ProfileConfig[].class);
            profiles = Arrays.stream(profilesConfig)
                    .filter(pc -> pc.name != null && !pc.name.isEmpty())
                    .collect(Collectors.toMap(pc -> pc.name, pc -> pc));
        }
        if (options.containsKey(SETTINGS_CONFIG_KEY)) {
            Gson gson = new Gson();
            this.config = gson.fromJson(options.get(SETTINGS_CONFIG_KEY), ProcessingConfig.class);
        }

        this.helper = new CodeGenerator(this.treeMaker, this.elementUtils, this.messager, profiles);

        super.init(processingEnv);
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Stream.of(
                CutCode.class.getCanonicalName(),
                CutCodes.class.getCanonicalName()
        )
        .collect(Collectors.collectingAndThen(Collectors.toSet(), Collections::unmodifiableSet)); // for Java8
    }

    @Override
    public Set<String> getSupportedOptions() {
        return Stream.of(
                SETTINGS_CONFIG_KEY,
                PROFILES_CONFIG_KEY
        )
        .collect(Collectors.collectingAndThen(Collectors.toSet(), Collections::unmodifiableSet));
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latest();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (roundEnv.processingOver()) {
            return false;
        }

        roundEnv.getElementsAnnotatedWith(CutCodes.class).forEach(this::replacingProcess);
        roundEnv.getElementsAnnotatedWith(CutCode.class).forEach(this::replacingProcess);

        return true;
    }

    private void replacingProcess(Element element) {
        CutCode[] annotations = element.getAnnotationsByType(CutCode.class);
        if (annotations == null || annotations.length == 0) {
            return;
        }
        if (element.getKind() != ElementKind.METHOD) {
            this.messager.printMessage(Diagnostic.Kind.WARNING, "Code Cutter : unsupported annotation for \"" + element.getSimpleName() + "\"");
            return;
        }

        this.config.setLocalProcessingConfig(element.getAnnotation(CutCodeProcessConfig.class));

        if (this.config.logProcessing()) {
            this.messager.printMessage(Diagnostic.Kind.NOTE, "Code Cutter : processing of \"" + element.getSimpleName() + "\"");
        }

        this.replaceMethod(element, annotations);
    }

    private void replaceMethod(Element element, CutCode[] annotations) {
        JCTree.JCMethodDecl jcMethodDecl = (JCTree.JCMethodDecl) this.elementUtils.getTree(element);
        JCTree returnType = jcMethodDecl.getReturnType();
        String returnTypeStr = returnType.type.asElement().toString();

        List<JCTree.JCStatement> finalStatements = List.nil();

        if (CompletableFuture.class.getCanonicalName().equals(returnTypeStr)) {
//            Type futureType = methodSymbol.getReturnType().getTypeArguments().get(0);
//            JCTree.JCVariableDecl varDecl = this.getProxyVarDecl(futureType);
            finalStatements = List.of(
//                    varDecl,
                    this.treeMaker.Return(this.treeMaker.Apply(
                            List.nil(),
                            this.helper.createClassExpression(CompletableFuture.class.getCanonicalName() + ".completedFuture"),
//                            List.of(this.treeMaker.Ident(this.elementUtils.getName("returnedValue")))
                                    List.of(this.treeMaker.Literal(TypeTag.BOT, null))
                            )
                    )
            );
        } else {
            finalStatements = List.of(this.helper.createDefaultReturnStatement(jcMethodDecl, this.config));

        }

        this.helper.replaceBody(jcMethodDecl, annotations, finalStatements, this.config);
    }
}
