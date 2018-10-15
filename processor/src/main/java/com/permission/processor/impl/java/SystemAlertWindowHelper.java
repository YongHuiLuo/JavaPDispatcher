package com.permission.processor.impl.java;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;

/**
 * create date:  2018/8/1
 * creator:  luoyonghui
 * functional description:
 */
public class SystemAlertWindowHelper implements SensitivePermissionInterface {

    private final ClassName PERMISSION_UTILS = ClassName.get("com.permission.library", "PermissionUtils", new String[0]);
    private final ClassName SETTINGS = ClassName.get("android.provider", "Settings", new String[0]);
    private final ClassName INTENT = ClassName.get("android.content", "Intent", new String[0]);
    private final ClassName URI = ClassName.get("android.net", "Uri", new String[0]);

    @Override
    public void addHasSelfPermissionsCondition(MethodSpec.Builder builder, String activityVar, String permissionField) {
        builder.beginControlFlow("if ($T.hasSelfPermissions($N, $N) || $T.canDrawOverlays($N))", new Object[]{this.PERMISSION_UTILS, activityVar, permissionField, this.SETTINGS, activityVar});
    }

    @Override
    public void addRequestPermissionsStatement(MethodSpec.Builder builder, String targetParam, String activityVar, String requestCodeField) {
        builder.addStatement("$T intent = new $T($T.ACTION_MANAGE_OVERLAY_PERMISSION, $T.parse(\"package:\" + $N.getPackageName()))", new Object[]{this.INTENT, this.INTENT, this.SETTINGS, this.URI, activityVar});
        builder.addStatement("$N.startActivityForResult(intent, $N)", new Object[]{targetParam, requestCodeField});
    }
}
