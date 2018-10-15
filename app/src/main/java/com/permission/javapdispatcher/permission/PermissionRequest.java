package com.permission.javapdispatcher.permission;

import android.content.Context;

public class PermissionRequest {
    private Context mContext;
    private String[] mPermissions;
    private IPermissionResultListener mPermissionResultListener;

    public PermissionRequest(Context pContext, String[] permissions, IPermissionResultListener listener) {
        this.mContext = pContext;
        this.mPermissions = permissions;
        this.mPermissionResultListener = listener;
    }

    public void request() {
        new RuntimeRequest(mContext)
                .setPermissions(mPermissions)
                .setCallback(mPermissionResultListener)
                .commit();
    }

}
