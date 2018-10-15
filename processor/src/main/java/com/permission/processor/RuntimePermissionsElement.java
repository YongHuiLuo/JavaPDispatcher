package com.permission.processor;

import com.permission.annotation.NeedsPermission;
import com.permission.annotation.OnNeverAsk;
import com.permission.annotation.OnDenied;
import com.permission.annotation.OnRationale;
import com.permission.processor.util.Constants;
import com.permission.processor.util.Extension;
import com.permission.processor.util.Helper;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeVariableName;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;

/**
 * create date:  2018/8/1
 * creator:  luoyonghui
 * functional description:
 */
public class RuntimePermissionsElement {

    private TypeName mTypeName;
    private List mTypeVariables;

    private String mPackageName;
    private String mInputClassName;
    private String mGeneratedClassName;
    private List mNeedsElements;
    private List mOnRationaleElements;
    private List mOnDeniedElements;
    private List mOnNeverAskElements;

    public TypeName getTypeName() {
        return mTypeName;
    }

    public List getTypeVariables() {
        return mTypeVariables;
    }

    public String getPackageName() {
        return mPackageName;
    }

    public String getInputClassName() {
        return mInputClassName;
    }

    public String getGeneratedClassName() {
        return mGeneratedClassName;
    }

    public List getNeedsElements() {
        return mNeedsElements;
    }

    public RuntimePermissionsElement(TypeElement typeElement) {
        this.mTypeName = TypeName.get(typeElement.asType());
        this.mPackageName = Extension.packageName(typeElement);
        List params = typeElement.getTypeParameters();
        Iterator iterator = params.iterator();
        List<TypeVariableName> parameterElements = new ArrayList<>();
        while (iterator.hasNext()) {
            TypeVariableName typeVariableName = (TypeVariableName) iterator.next();
            parameterElements.add(typeVariableName);
        }
        this.mTypeVariables = parameterElements;
        this.mInputClassName = Extension.simpleString(typeElement);
        this.mGeneratedClassName = mInputClassName + Constants.GEN_CLASS_SUFFIX;
        this.mNeedsElements = Extension.childElementsAnnotatedWith(typeElement, NeedsPermission.class);
        this.mOnRationaleElements = Extension.childElementsAnnotatedWith(typeElement, OnRationale.class);
        this.mOnDeniedElements = Extension.childElementsAnnotatedWith(typeElement, OnDenied.class);
        this.mOnNeverAskElements = Extension.childElementsAnnotatedWith(typeElement, OnNeverAsk.class);
    }

    public ExecutableElement findOnRationaleForNeeds(ExecutableElement needsElement) {
        return Helper.findMatchingMethodForNeeds(needsElement, mOnRationaleElements, OnRationale.class);
    }

    public ExecutableElement findOnDeniedForNeeds(ExecutableElement needsElement) {
        return Helper.findMatchingMethodForNeeds(needsElement, mOnDeniedElements, OnDenied.class);
    }

    public ExecutableElement findOnNeverAskForNeeds(ExecutableElement needsElement) {
        return Helper.findMatchingMethodForNeeds(needsElement, mOnNeverAskElements, OnNeverAsk.class);
    }
}
