package com.intechcore.scomponents.tools.cutter.processor;

import com.intechcore.scomponents.tools.cutter.annotations.RemovableFunctionality;
import com.intechcore.scomponents.tools.cutter.processor.helpers.ReplaceHelper;
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
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.util.LinkedHashSet;
import java.util.Set;

// @RemovableFunctionality(sendNotification = true, notificationText = "Hello") =>
// just replaces method body with
// this.actionBus(com.intechcore.scell.model.action.SpreadsheetAction.WARNING_MESSAGE, notificationText);
// i.e. method must be void


// @BusinessLicenceRequired
// replaces method body with
// com.intechcore.scomponents.api.utils.LoggerUtils.logBusinessLicenseRequiredMessage(this.logger)
// where this.logger -> class member annotated with @APILogger
// Then analyses return type and return
// nothing if void
// CompletableFuture.completedFuture(null) if CompletableFuture
// null in other cases


public class LimitProcessor extends AbstractProcessor {
    private static final String ACTION_BUS_MEMBER_CALL_ACT_CODE = "this.actionBus.act";
    private static final String WARNING_MESSAGE_FULL_NAME =
            "com.intechcore.scell.model.action.SpreadsheetAction.WARNING_MESSAGE";

    private Messager messager;
    private Context context;
    private JavacElements elementUtils;
    private TreeMaker treeMaker;

    private ReplaceHelper replaceHelper;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);

        this.messager = processingEnv.getMessager();
        this.context = ((JavacProcessingEnvironment) processingEnv).getContext();
        this.elementUtils = (JavacElements) processingEnv.getElementUtils();
        this.treeMaker = TreeMaker.instance(this.context);

        this.replaceHelper = new ReplaceHelper(this.treeMaker, this.elementUtils);
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> annotations = new LinkedHashSet<>();
        annotations.add(RemovableFunctionality.class.getCanonicalName());
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

        roundEnvironment.getElementsAnnotatedWith(RemovableFunctionality.class).forEach(this::replacingProcess);

        return false;
    }

    private void replacingProcess(javax.lang.model.element.Element element) {
        if (element.getKind() == ElementKind.METHOD) {
            RemovableFunctionality annotation = element.getAnnotation(RemovableFunctionality.class);
            if (annotation == null) {
                return;
            }

            JCTree.JCMethodDecl jcMethodDecl = (JCTree.JCMethodDecl) this.elementUtils.getTree(element);
            this.treeMaker.pos = jcMethodDecl.pos;
            JCTree.JCExpression newBodyCode = this.replaceHelper.createClassExpression(ACTION_BUS_MEMBER_CALL_ACT_CODE);

            if (annotation.sendNotification()) {
                jcMethodDecl.body = this.treeMaker.Block(0, List.of(
                    this.treeMaker.Exec(this.treeMaker.Apply(
                        List.nil(),
                        newBodyCode,
                        List.of(
                            this.replaceHelper.createClassExpression(WARNING_MESSAGE_FULL_NAME),
                            this.treeMaker.Literal(annotation.notificationText())
                        )
                    )))
                );

            } else {
                jcMethodDecl.body = this.treeMaker.Block(0, List.nil());
            }
        } else {
            this.messager.printMessage(Diagnostic.Kind.WARNING, element.getKind().toString() + " is not implemented.");
        }
    }

}
