package com.permission.processor.impl.java;

import com.permission.processor.util.Helper;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;

import javax.annotation.processing.Messager;
import javax.lang.model.type.TypeMirror;

/**
 * create date:  2018/8/2
 * creator:  luoyonghui
 * functional description:
 */
public class JavaActivityProcessorUnit extends JavaBaseProcessorUnit {

    private ClassName ACTIVITY_COMPAT = ClassName.get("android.support.v4.app", "ActivityCompat");

    public JavaActivityProcessorUnit(Messager messager) {
        super(messager);
    }

    @Override
    public String getActivityName(String targetParam) {
        return targetParam;
    }

    @Override
    public void addShouldShowRequestPermissionRationaleCondition(MethodSpec.Builder builder, String targetParam, String permissionField, boolean isPositiveCondition) {
        builder.beginControlFlow("if ($N$T.shouldShowRequestPermissionRationale($N, $N)) ", isPositiveCondition ? "" : "!", PERMISSION_UTILS, targetParam, permissionField);
    }

    @Override
    public void addRequestPermissionsStatement(MethodSpec.Builder builder, String target, String permissionField, String requestCodeField) {
        builder.addStatement("$T.requestPermissions($N, $N, $N)", ACTIVITY_COMPAT, target, permissionField, requestCodeField);
    }

    @Override
    public boolean isDeprecated() {
        return false;
    }

    @Override
    public TypeMirror getTargetType() {
        return Helper.typeMirrorOf("android.app.Activity");
    }
}
