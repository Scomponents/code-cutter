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
import com.intechcore.scomponents.tools.cutter.annotations.CutCodeProcessConfig;
import com.intechcore.scomponents.tools.cutter.annotations.common.CutCodes;
import com.sun.tools.javac.code.TypeTag;
import com.sun.tools.javac.model.JavacElements;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.List;

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

/**
 * The {@code CutCodeProcessor} is an annotation processor that handles
 * {@link com.intechcore.scomponents.tools.cutter.annotations.CutCode} and
 * {@link com.intechcore.scomponents.tools.cutter.annotations.common.CutCodes} annotations.
 * It modifies the Abstract Syntax Tree (AST) of annotated methods to replace
 * their original bodies with generated code based on the annotation's configuration.
 * This processor operates at compile time, effectively "cutting" and replacing code.
 */
public class CutCodeProcessor extends AbstractProcessor {
    private static final String SETTINGS_CONFIG_KEY = "INTECHCORE_CUTTER_SETTINGS";
    private static final String PROFILES_CONFIG_KEY = "INTECHCORE_CUTTER_PROFILES";

    private Messager messager;
    private JavacElements elementUtils;
    private TreeMaker treeMaker;
    private CodeGenerator helper;

    private ProcessingConfig config = new ProcessingConfig();

    /**
     * Default constructor for the {@code CutCodeProcessor}.
     */
    public CutCodeProcessor() {
    }

    /**
     * Initializes the annotation processor with the processing environment.
     * This method is called once per compiler run. It sets up the necessary
     * utilities like {@code Messager}, {@code JavacElements}, and {@code TreeMaker},
     * and loads global {@link ProcessingConfig} and {@link ProfileConfig} from options.
     *
     * @param processingEnv The processing environment provided by the compiler.
     */
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

    /**
     * Returns the set of annotation types supported by this processor.
     *
     * @return A set of strings containing the canonical names of the supported annotation types.
     */
    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Stream.of(
                CutCode.class.getCanonicalName(),
                CutCodes.class.getCanonicalName()
        )
        .collect(Collectors.collectingAndThen(Collectors.toSet(), Collections::unmodifiableSet)); // for Java8
    }

    /**
     * Returns the names of the annotation processing options recognized by this processor.
     * These options are used to configure the behavior of the {@link CutCodeProcessor}.
     *
     * @return A set of strings containing the names of the supported options.
     */
    @Override
    public Set<String> getSupportedOptions() {
        return Stream.of(
                SETTINGS_CONFIG_KEY,
                PROFILES_CONFIG_KEY
        )
        .collect(Collectors.collectingAndThen(Collectors.toSet(), Collections::unmodifiableSet));
    }

    /**
     * Returns the latest source version supported by this annotation processor.
     *
     * @return The latest {@link SourceVersion} supported.
     */
    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latest();
    }

    /**
     * Processes annotations on a set of program elements. This is the main entry point
     * for the annotation processor. It iterates over elements annotated with
     * {@link CutCode} or {@link CutCodes} and initiates the code replacement process.
     *
     * @param annotations The set of annotations to process.
     * @param roundEnv    The current processing environment.
     * @return {@code true} if the annotations were processed by this processor; {@code false} otherwise.
     */
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (roundEnv.processingOver()) {
            return false;
        }

        roundEnv.getElementsAnnotatedWith(CutCodes.class).forEach(this::replacingProcess);
        roundEnv.getElementsAnnotatedWith(CutCode.class).forEach(this::replacingProcess);

        return true;
    }

    /**
     * Handles the replacement process for a single element annotated with {@link CutCode} or {@link CutCodes}.
     * It extracts the {@link CutCode} annotations, validates the element kind (must be a method),
     * applies any local processing configuration, and then calls {@link #replaceMethod(Element, CutCode[])}
     * to modify the method's body.
     *
     * @param element The {@code Element} annotated with {@link CutCode} or {@link CutCodes}.
     */
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

    /**
     * Replaces the body of an annotated method with generated code.
     * This method constructs the final statements, including default return values,
     * and delegates to the {@link CodeGenerator} to perform the actual AST modification.
     * It also handles special cases like {@code CompletableFuture} return types.
     *
     * @param element     The {@code Element} representing the method to be replaced.
     * @param annotations An array of {@link CutCode} annotations applied to the method.
     */
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
