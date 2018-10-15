package com.permission.javapdispatcher.permission;

import android.Manifest;
import android.content.Context;

public class PermissionManager {

    private boolean isApplicationApply = false;
    public static final int PERMISSION_REQ = -1;
    public static final int PERMISSION_DENIED = 0;
    public static final int PERMISSION_GRANTED = 1;
    private int mPermissionState = PERMISSION_REQ;
    private boolean isRequestDrawOverlaysPermission = false;
    private boolean isRequestWriteSettingsPermission = false;

    private IPermissionCallback mPermissionCallback;

    public final String[] mPermissions = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_PHONE_STATE
    };


    private static PermissionManager sInstance = null;

    private PermissionManager() {
        mPermissionState = PERMISSION_REQ;
    }

    public static PermissionManager getInstance() {
        if (sInstance == null) {
            synchronized (PermissionManager.class) {
                if (sInstance == null) {
                    sInstance = new PermissionManager();
                }
            }
        }

        return sInstance;
    }

    public boolean isApplicationApply() {
        return isApplicationApply;
    }

    /**
     *
     * @param applicationApply
     */
    public void setApplicationApply(boolean applicationApply) {
        isApplicationApply = applicationApply;
    }

    public int getPermissionState() {
        return mPermissionState;
    }

    public void setPermissionState(int state) {
        mPermissionState = state;
    }

    /**
     * 请求权限
     * 如果没有传递permission， 默认请求
     * {@Manifest.permission.READ_EXTERNAL_STORAGE}
     * {@Manifest.permission.WRITE_EXTERNAL_STORAGE}
     * {@Manifest.permission.READ_PHONE_STATE}
     * @param context
     * @param permissions 需要请求的权限列表
     */
    public void request(Context context, String[] permissions) {
        if (permissions == null) {
            permissions = mPermissions;
        }
        new PermissionRequest(context, permissions, new IPermissionResultListener() {

            @Override
            public void onGranted() {
                mPermissionState = PERMISSION_GRANTED;
                if (mPermissionCallback != null) {
                    mPermissionCallback.onPermissionGranted();
                }
            }

            @Override

            public void onDenied() {
                mPermissionState = PERMISSION_DENIED;
                if (mPermissionCallback != null) {
                    mPermissionCallback.onPermissionDenied();
                }
            }
        }).request();
    }

    public void authGrantedCallback() {

    }

    public void authDeniedCallback() {

    }

    public IPermissionCallback getPermissionCallback() {
        return mPermissionCallback;
    }

    public void setPermissionCallback(IPermissionCallback callback) {
        this.mPermissionCallback = callback;
    }

    public boolean isRequestDrawOverlaysPermission() {
        return isRequestDrawOverlaysPermission;
    }

    public void setRequestDrawOverlaysPermission(boolean isRequest) {
        isRequestDrawOverlaysPermission = isRequest;
    }

    public boolean isRequestWriteSettingsPermission() {
        return isRequestWriteSettingsPermission;
    }

    public void setRequestWriteSettingsPermission(boolean isRequest) {
        isRequestWriteSettingsPermission = isRequest;
    }

    public interface IPermissionCallback {
        void onPermissionGranted();

        void onPermissionDenied();
    }
}
