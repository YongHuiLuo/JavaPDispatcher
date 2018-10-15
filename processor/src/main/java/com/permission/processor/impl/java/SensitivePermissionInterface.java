package com.permission.processor.impl.java;

import com.squareup.javapoet.MethodSpec;

/**
 * create date:  2018/8/1
 * creator:  luoyonghui
 * functional description:
 */
public interface SensitivePermissionInterface {
    void addHasSelfPermissionsCondition(MethodSpec.Builder builder, String activityVar, String permissionField);

    void addRequestPermissionsStatement(MethodSpec.Builder builder, String targetParam, String activityVar, String requestCodeField);
}

