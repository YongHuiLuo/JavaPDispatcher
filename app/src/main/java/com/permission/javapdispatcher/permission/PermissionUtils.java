package com.permission.javapdispatcher.permission;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.AppOpsManagerCompat;
import android.support.v4.util.SimpleArrayMap;

import static android.support.v4.content.ContextCompat.checkSelfPermission;

public class PermissionUtils {

    private static final SimpleArrayMap<String, Integer> MIN_SDK_PERMISSIONS;

    static {
        MIN_SDK_PERMISSIONS = new SimpleArrayMap<>(8);
        MIN_SDK_PERMISSIONS.put("com.android.voicemail.permission.ADD_VOICEMAIL", 14);
        MIN_SDK_PERMISSIONS.put("android.permission.BODY_SENSORS", 20);
        MIN_SDK_PERMISSIONS.put("android.permission.READ_CALL_LOG", 16);
        MIN_SDK_PERMISSIONS.put("android.permission.READ_EXTERNAL_STORAGE", 16);
        MIN_SDK_PERMISSIONS.put("android.permission.USE_SIP", 9);
        MIN_SDK_PERMISSIONS.put("android.permission.WRITE_CALL_LOG", 16); //
        MIN_SDK_PERMISSIONS.put("android.permission.SYSTEM_ALERT_WINDOW", 23);
        MIN_SDK_PERMISSIONS.put("android.permission.WRITE_SETTINGS", 23);
    }

    private static volatile int targetSdkVersion = -1;

    private PermissionUtils() {
    }

    public static boolean verifyPermissions(int... grantResults) {
        if (grantResults.length == 0) {
            return false;
        }
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private static boolean permissionExists(String permission) {
        Integer minVersion = MIN_SDK_PERMISSIONS.get(permission);
        return minVersion == null || Build.VERSION.SDK_INT >= minVersion;
    }

    public static boolean hasSelfPermissions(Context context, String... permissions) {
        for (String permission : permissions) {
            if (permissionExists(permission) && !hasSelfPermission(context, permission)) {
                return false;
            }
        }
        return true;
    }

    private static boolean hasSelfPermission(Context context, String permission) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && "Xiaomi".equalsIgnoreCase(Build.MANUFACTURER)) {
            return hasSelfPermissionForXiaoMi(context, permission);
        }
        try {
            return checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
        } catch (RuntimeException t) {
            return false;
        }
    }

    private static boolean hasSelfPermissionForXiaoMi(Context context, String permission) {
        String permissionToOp = AppOpsManagerCompat.permissionToOp(permission);
        if (permissionToOp == null) {
            return true;
        }
        int noteOp = AppOpsManagerCompat.noteOp(context, permissionToOp, android.os.Process.myUid(), context.getPackageName());
        boolean isAllow = noteOp == AppOpsManagerCompat.MODE_ALLOWED;

        return isAllow && checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean shouldShowRequestPermissionRationale(Activity activity, String... permissions) {
        for (String permission : permissions) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                return true;
            }
        }
        return false;
    }

    @TargetApi(23)
    public static boolean hasDrawOverlaysPermissions(Context ctx) {
        // 在Android Oreo
        // 使用 SYSTEM_ALERT_WINDOW 权限的应用无法再使用以下窗口类型来在其他应用和系统窗口上方显示提醒窗口：
        //
        // TYPE_PHONE
        // TYPE_PRIORITY_PHONE
        // TYPE_SYSTEM_ALERT
        // TYPE_SYSTEM_OVERLAY
        // TYPE_SYSTEM_ERROR
        // 作为替代可以使用 TYPE_APPLICATION_OVERLAY 新窗口类型
        // TYPE_APPLICATION_OVERLAY 在输入法与状态栏下面，用户可以在通知中去禁用

        if (Build.VERSION.SDK_INT >= 26) {
            // TODO Android Oreo下无权限，此处暂时返回TRUE跳过权限申请
            return true;
        }
        if (Build.VERSION.SDK_INT >= 23) {
            return Settings.canDrawOverlays(ctx);
        }

        return true;
    }

    @TargetApi(23)
    public static boolean hasWriteSettingsPermissions(Context ctx) {
        if (Build.VERSION.SDK_INT >= 23) {
            return Settings.System.canWrite(ctx);
        }

        return true;
    }

}
