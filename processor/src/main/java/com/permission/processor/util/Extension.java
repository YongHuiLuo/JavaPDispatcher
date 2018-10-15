package com.permission.processor.util;

import com.permission.annotation.NeedsPermission;
import com.permission.annotation.OnNeverAsk;
import com.permission.annotation.OnDenied;
import com.permission.annotation.OnRationale;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

import static com.permission.processor.PermissionsProcessor.TYPE_UTILS;

/**
 * create date:  2018/8/1
 * creator:  luoyonghui
 * functional description:
 */
public class Extension {


    public static String packageName(TypeElement typeElement) {
        return packageName(typeElement.getEnclosingElement());
    }

    public static boolean isSubtypeOf(TypeMirror typeMirror, TypeMirror typeOf) {
        return TYPE_UTILS.isSubtype(typeMirror, typeOf);
    }

    private static String packageName(Element element) {
        if (element instanceof TypeElement) {
            return packageName(element);
        } else if (element instanceof PackageElement) {
            return ((PackageElement) element).getQualifiedName().toString();
        } else {
            String packageName = packageName(element.getEnclosingElement());
            return packageName == null ? "" : packageName;
        }
    }

    public static List<String> permissionValue(Annotation annotation) {
        if (annotation instanceof NeedsPermission) {
            return Arrays.asList(((NeedsPermission) annotation).value());
        } else if (annotation instanceof OnRationale) {
            return Arrays.asList(((OnRationale) annotation).value());
        } else if (annotation instanceof OnDenied) {
            return Arrays.asList(((OnDenied) annotation).value());
        } else if (annotation instanceof OnNeverAsk) {
            return Arrays.asList(((OnNeverAsk) annotation).value());
        }
        return Collections.emptyList();
    }

    public static int permissionPosition(Annotation annotation) {
        if (annotation instanceof NeedsPermission) {
            return ((NeedsPermission) annotation).position();
        } else if (annotation instanceof OnRationale) {
            return ((OnRationale) annotation).position();
        } else if (annotation instanceof OnDenied) {
            return ((OnDenied) annotation).position();
        } else if (annotation instanceof OnNeverAsk) {
            return ((OnNeverAsk) annotation).position();
        }
        return -1;
    }

    public static String simpleString(Element element) {
        return trimDollarIfNeed(element.getSimpleName().toString());
    }

    private static String trimDollarIfNeed(String str) {
        int index = str.indexOf("$");
        return (index == -1) ? str : str.substring(0, index);
    }

    public static List<ExecutableElement> childElementsAnnotatedWith(Element element, Class<? extends Annotation> annotation) {
        List<ExecutableElement> executableElements = new ArrayList<>();
        List<Element> elements = (List<Element>) element.getEnclosedElements();
        for (Element e : elements) {
            if (hasAnnotation(e, annotation)) {
                if (e instanceof ExecutableElement) {
                    executableElements.add((ExecutableElement) e);
                }
            }
        }
        return executableElements;
    }

    public static boolean hasAnnotation(Element element, Class<? extends Annotation> annotation) {
        return element.getAnnotation(annotation) != null;
    }
}
