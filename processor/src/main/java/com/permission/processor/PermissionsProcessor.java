package com.permission.processor;

import com.permission.annotation.RuntimePermissions;
import com.permission.processor.impl.ProcessorUnits;
import com.permission.processor.util.Validators;
import com.squareup.javapoet.JavaFile;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.logging.Logger;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;


/**
 * create date:  2018/8/1
 * creator:  luoyonghui
 * functional description:
 */
public class PermissionsProcessor extends AbstractProcessor {

    public static Elements ELEMENT_UTILS;
    public static Types TYPE_UTILS;
    private Filer mFiler;
    private Messager messager = null;
    private ProcessingEnvironment mProcessingEnv;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        Logger.getLogger("LYH").info("dPermissionsProcessor");
        mProcessingEnv = processingEnv;
        mFiler = processingEnv.getFiler();
        messager = processingEnv.getMessager();
        ELEMENT_UTILS = processingEnv.getElementUtils();
        TYPE_UTILS = processingEnv.getTypeUtils();
        System.out.println("你好  你好   你好 ");
    }

    public static Elements getElementUtils() {
        return ELEMENT_UTILS;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> hashSet = new LinkedHashSet();
        hashSet.add(RuntimePermissions.class.getCanonicalName());
        return hashSet;
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        System.out.print("process start ....");
        Messager messager = mProcessingEnv.getMessager();
        messager.printMessage(Diagnostic.Kind.NOTE, "process start ........");
        RequestCodeProvider requestCodeProvider = new RequestCodeProvider();
        Set<Element> set = (Set<Element>) roundEnv.getElementsAnnotatedWith(RuntimePermissions.class);
        Iterator iterator = set.iterator();
        while (iterator.hasNext()) {
            Element element = (Element) iterator.next();
            RuntimePermissionsElement runtimePermissionsElement = new RuntimePermissionsElement((TypeElement) element);
            processJava(element, runtimePermissionsElement, requestCodeProvider);
        }
        messager.printMessage(Diagnostic.Kind.NOTE, "process end ........");
        System.out.print("process end ....");
        return true;
    }

    private final void processJava(Element element, RuntimePermissionsElement rpe, RequestCodeProvider requestCodeProvider) {
        ProcessorUnit processorUnit = Validators.findAndValidateProcessorUnit(ProcessorUnits.javaProcessorUnits(messager), element);
        JavaFile javaFile = (JavaFile) processorUnit.createFile(rpe, requestCodeProvider);
        try {
            javaFile.writeTo(mFiler);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
