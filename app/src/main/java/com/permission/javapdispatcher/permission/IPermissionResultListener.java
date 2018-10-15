package com.permission.javapdispatcher.permission;

public interface IPermissionResultListener {
    void onGranted();

    void onDenied();
}
