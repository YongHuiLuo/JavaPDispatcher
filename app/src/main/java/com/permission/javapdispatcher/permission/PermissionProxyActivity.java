package com.permission.javapdispatcher.permission;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;

//import com.estrongs.android.ui.dialog.CommonAlertDialog;
import com.permission.javapdispatcher.R;

/**
 *
 * 用途：权限代理
 * 触发时机 ： 当用户拒绝授权某一个或多个权限时，会触发当前Activity，试图引导用户授权
 * 创建者： luoyonghui
 * 创建时间： 2018.7.30
 */
public class PermissionProxyActivity extends AppCompatActivity {

    interface PermissionListener {
        void onResult(boolean isOk);
    }

    static final String KEY_REQUEST_PERMISSIONS = "key_request_permissions";
    private static PermissionListener mPermissionListener;
    private String[] mPermissions;

    public static void setPermissionListener(PermissionListener permissionListener) {
        mPermissionListener = permissionListener;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        mPermissions = intent.getStringArrayExtra(KEY_REQUEST_PERMISSIONS);
        if (mPermissions == null) {
            mPermissionListener = null;
            finish();
            return;
        }
        checkPermissions();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (!PermissionUtils.verifyPermissions(grantResults)) {
            callbackPermissionResult(false);
            if (!PermissionUtils.shouldShowRequestPermissionRationale(this, permissions)) {
//                showAskUserDialog();
            } else {
                finish();
            }
        } else {
            callbackPermissionResult(true);
            finish();
        }
    }

    private void checkPermissions() {
        if (PermissionUtils.shouldShowRequestPermissionRationale(this, mPermissions)) {
//            showAskRationaleDialog();
        } else {
            ActivityCompat.requestPermissions(this, mPermissions, 1);
        }
    }

//    private void showAskRationaleDialog() {
//        CommonAlertDialog.Builder builder = new CommonAlertDialog.Builder(this);
//        builder.setTitle(getString(R.string.permission_rationale_title));
//        builder.setMessage(getString(R.string.permission_rationale_message));
//        builder.setConfirmButton(getString(R.string.allow_grant), new DialogInterface.OnClickListener() {
//            public void onClick(DialogInterface dialog, int whichButton) {
//                dialog.dismiss();
//                ActivityCompat.requestPermissions(PermissionProxyActivity.this, mPermissions, 1);
//            }
//        });
//        builder.setCancelButton(getString(R.string.exit_app), new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                dialog.dismiss();
//                finish();
//            }
//        }).setCancelable(false).create().show();
//    }

    private void callbackPermissionResult(boolean isOk) {
        if (mPermissionListener != null) {
            mPermissionListener.onResult(isOk);
            mPermissionListener = null;
        }
    }

//    private void showAskUserDialog() {
//        CommonAlertDialog.Builder builder = new CommonAlertDialog.Builder(this);
//        builder.setTitle(getString(R.string.permissions_title));
//        builder.setMessage(getString(R.string.dangerous_permissions_msg));
//        builder.setConfirmButton(getString(R.string.allow_grant), new DialogInterface.OnClickListener() {
//            public void onClick(DialogInterface dialog, int whichButton) {
//                startAppSettings();
//                dialog.dismiss();
//            }
//        });
//        builder.setCancelButton(getString(R.string.exit_app), new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                dialog.dismiss();
//                finish();
//            }
//        }).setCancelable(false).create().show();
//    }

    protected void startAppSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivity(intent);
    }
}
