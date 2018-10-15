package com.permission.processor.util;

import com.permission.annotation.NeedsPermission;
import com.permission.processor.RuntimePermissionsElement;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.TypeName;

import java.lang.annotation.Annotation;
import java.util.Iterator;
import java.util.List;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

import static com.permission.processor.PermissionsProcessor.ELEMENT_UTILS;


/**
 * create date:  2018/8/1
 * creator:  luoyonghui
 * functional description:
 */
public class Helper {

    public static String permissionFieldName(ExecutableElement e) {
        return Constants.GEN_PERMISSION_PREFIX.concat(Extension.simpleString(e).toUpperCase());
    }

    public static String requestCodeFieldName(ExecutableElement e) {
        return Constants.GEN_REQUEST_CODE_PREFIX.concat(Extension.simpleString(e).toUpperCase());
    }

    public static String pendingRequestFieldName(ExecutableElement e) {
        return Constants.GEN_PENDING_PREFIX.concat(Extension.simpleString(e).toUpperCase());
    }

    public static String withPermissionCheckMethodName(ExecutableElement executableElement) {
        return Extension.simpleString(executableElement);
//        return Extension.simpleString(executableElement).concat(Constants.GEN_WITH_PERMISSION_CHECK_SUFFIX);
    }

    public static String permissionRequestTypeName(RuntimePermissionsElement rpe, ExecutableElement e) {
        return rpe.getInputClassName().concat(capitalize(Extension.simpleString(e))).concat(Constants.GEN_PERMISSION_REQUEST_SUFFIX);
    }

    private static String capitalize(String str) {
        if (str == null || str == "") {
            return str;
        }
        return str.substring(0, 1).toLowerCase() + str.substring(1);
    }

    public static TypeName typeNameOf(Element element) {
        return TypeName.get(element.asType());
    }

    public static TypeMirror typeMirrorOf(String className) {
        return ELEMENT_UTILS.getTypeElement(className).asType();
    }

    public static ExecutableElement findMatchingMethodForNeeds(ExecutableElement needsElement,
                                                               List<ExecutableElement> otherElement,
                                                               Class<? extends Annotation> annotationType) {
        List<String> value = Extension.permissionValue(needsElement.getAnnotation(NeedsPermission.class));
        int needPos = Extension.permissionPosition(needsElement.getAnnotation(NeedsPermission.class));
        Iterator iterator = otherElement.iterator();
        Annotation annotation;
        while (iterator.hasNext()) {
            ExecutableElement element = (ExecutableElement) iterator.next();
            annotation = element.getAnnotation(annotationType);
            int otherPos = Extension.permissionPosition(annotation);
            List<String> otherPermissionValue = Extension.permissionValue(annotation);
            if (otherPermissionValue.toString().equals(value.toString()) && (needPos == otherPos)) {
                return element;
            }
        }
        return null;
    }

    public static CodeBlock argsParametersCodeBlock(ExecutableElement needsElement) {
        CodeBlock.Builder builder = CodeBlock.builder();
        List<VariableElement> variableElements = (List<VariableElement>) needsElement.getParameters();
        int size = variableElements.size();
        for (int i = 0; i < size; i++) {
            builder.add("$L", Extension.simpleString(variableElements.get(i)));
            if (i < size - 1) {
                builder.add(",");
            }
        }
        return builder.build();
    }
}
