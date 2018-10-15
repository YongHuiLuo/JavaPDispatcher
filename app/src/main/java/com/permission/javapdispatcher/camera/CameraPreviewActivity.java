/*
 * Copyright 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.permission.javapdispatcher.camera;

import android.app.Activity;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import com.permission.javapdispatcher.R;

public class CameraPreviewActivity extends Activity {

    private static final String TAG = "CameraPreview";

    private static final int CAMERA_ID = 0;

    private CameraPreview preview;
    private Camera camera;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        Button backButton = findViewById(R.id.back);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackClick();
            }
        });
        initCamera();
    }

    private void initCamera() {
        camera = getCameraInstance(CAMERA_ID);
        Camera.CameraInfo cameraInfo = null;

        if (camera != null) {
            cameraInfo = new Camera.CameraInfo();
            Camera.getCameraInfo(CAMERA_ID, cameraInfo);
        }

        final int displayRotation = getWindowManager().getDefaultDisplay()
                .getRotation();

        FrameLayout preview = findViewById(R.id.camera_preview);
        preview.removeAllViews();

        if (this.preview == null) {
            this.preview = new CameraPreview(this, camera, cameraInfo, displayRotation);
        } else {
            this.preview.setCamera(camera, cameraInfo, displayRotation);
        }

        preview.addView(this.preview);
    }

    private void onBackClick() {
        getFragmentManager().popBackStack();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (camera == null) {
            initCamera();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        releaseCamera();
    }

    public static Camera getCameraInstance(int cameraId) {
        Camera c = null;
        try {
            c = Camera.open(cameraId);
        } catch (Exception e) {
            Log.d(TAG, "Camera " + cameraId + " is not available: " + e.getMessage());
        }
        return c;
    }

    private void releaseCamera() {
        if (camera != null) {
            camera.release();
            camera = null;
        }
    }
}
