package com.permission.javapdispatcher.permission;

public interface Request {
    Request setPermissions(String... permissions);

    Request setPermissions(String[]... permissions);

    Request setCallback(IPermissionResultListener callback);

    void commit();
}
