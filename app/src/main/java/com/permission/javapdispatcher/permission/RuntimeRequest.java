package com.permission.javapdispatcher.permission;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

public class RuntimeRequest implements Request, PermissionProxyActivity.PermissionListener {

    private Context mContext;

    private String[] mPermissions;

    private String[] mDeniedPermissions;

    private IPermissionResultListener mListener;


    public RuntimeRequest(Context context) {
        this.mContext = context;
    }

    @Override
    public Request setPermissions(String... permissions) {
        this.mPermissions = permissions;
        return this;
    }

    @Override
    public Request setPermissions(String[]... permissionsArray) {
        List<String> permissionList = new ArrayList<>();
        for (String[] permissions : permissionsArray) {
            for (String permission : permissions) {
                permissionList.add(permission);
            }
        }
        this.mPermissions = permissionList.toArray(new String[permissionList.size()]);
        return this;
    }

    @Override
    public Request setCallback(IPermissionResultListener listener) {
        this.mListener = listener;
        return this;
    }

    @Override
    public void commit() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            permissionGranted();
        } else {
            mDeniedPermissions = getDeniedPermissions(mContext, mPermissions);
            if (mDeniedPermissions.length > 0) {
                PermissionProxyActivity.setPermissionListener(this);
                Intent intent = new Intent(mContext, PermissionProxyActivity.class);
                intent.putExtra(PermissionProxyActivity.KEY_REQUEST_PERMISSIONS, mDeniedPermissions);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(intent);
            } else {
                permissionGranted();
            }
        }
    }

    private void permissionGranted() {
        if (mListener != null) {
            mListener.onGranted();
        }
    }

    private void permissionDenied() {
        if (mListener != null) {
            mListener.onDenied();
        }
    }

    private static String[] getDeniedPermissions(Context context, @NonNull String... permissions) {
        List<String> deniedList = new ArrayList<>(3);
        for (String permission : permissions) {
            if (!PermissionUtils.hasSelfPermissions(context, permission)) {
                deniedList.add(permission);
            }
        }
        return deniedList.toArray(new String[deniedList.size()]);
    }

    @Override
    public void onResult(boolean isOk) {
        if (isOk) {
            permissionGranted();
        } else {
            permissionDenied();
        }
    }
}
