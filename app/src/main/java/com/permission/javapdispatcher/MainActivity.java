package com.permission.javapdispatcher;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.permission.PermissionProxy;
import com.permission.annotation.NeedsPermission;
import com.permission.annotation.OnNeverAsk;
import com.permission.annotation.OnDenied;
import com.permission.annotation.OnRationale;
import com.permission.annotation.PermissionRequest;
import com.permission.annotation.RuntimePermissions;
import com.permission.javapdispatcher.camera.CameraPreviewActivity;

@RuntimePermissions
public class MainActivity extends AppCompatActivity {

    public static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.button_camera).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int param = 0;
                showCamera(param);
                //PermissionProxy.bind(MainActivity.this);
                MainActivity_PermissionsDispatcher.showCamera(MainActivity.this, param);
            }
        });
        findViewById(R.id.button_storage).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                MainActivityPermissionsDispatcher.storageFileWithPermissionCheck(MainActivity.this);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        MainActivity_PermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }


    @NeedsPermission(Manifest.permission.CAMERA)
    void showCamera(int params) {
        Log.d(TAG, "showCamera 我是什么鬼  但是我执行了 " + params);
        Intent intent = new Intent(this, CameraPreviewActivity.class);
        startActivity(intent);
    }

    @NeedsPermission(value = Manifest.permission.CAMERA, position = 1)
    void showCamera2(int params) {
        Log.d(TAG, "showCamera 我是什么鬼  但是我执行了 " + params);
        Intent intent = new Intent(this, CameraPreviewActivity.class);
        startActivity(intent);
    }

    @OnDenied(value = Manifest.permission.CAMERA, position = 1)
    void onCameraDenied() {
        Log.d(TAG, "onCameraDenied  我是什么鬼  但是我执行了 ");
        Toast.makeText(this, R.string.permission_camera_denied, Toast.LENGTH_SHORT).show();
    }

    @OnRationale(value = Manifest.permission.CAMERA, position = 1)
    void showRationaleForCamera(PermissionRequest request) {
        Log.d(TAG, "showRationaleForCamera 我是什么鬼  但是我执行了 ");
        showRationaleDialog(R.string.permission_camera_rationale, request);
    }

    @OnNeverAsk(value = Manifest.permission.CAMERA, position = 1)
    void onCameraNeverAskAgain() {
        Toast.makeText(this, R.string.permission_camera_never_ask_again, Toast.LENGTH_SHORT).show();
    }

    private void showRationaleDialog(@StringRes int messageResId, final PermissionRequest request) {
        new AlertDialog.Builder(this)
                .setPositiveButton(R.string.button_allow, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(@NonNull DialogInterface dialog, int which) {
                        request.proceed();
                    }
                })
                .setNegativeButton(R.string.button_deny, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(@NonNull DialogInterface dialog, int which) {
                        request.cancel();
                    }
                })
                .setCancelable(false)
                .setMessage(messageResId)
                .show();
    }


}
