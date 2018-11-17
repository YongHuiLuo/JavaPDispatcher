# JavaPDispatcher

为Android 动态权限申请提供基于注解实现的类库，方便开发者使用尽可能少的代码对Android 动态权限进行适配。<br>
Provides an annotation-based implementation class library for Android dynamic permission requests, which allows developers to adapt Android dynamic permissions with as little code as possible.<br>


# Use Scene 

1. 如果您的项目中没有引入Kotlin支持库，那么**JavaPDispatcher**将非常适合你的项目，此开源库完全通过Java代码实现；<br>
If you don't have a Kotlin support library in your project, then **JavaPDispatcher** will work great for your project. This open source library is completely implemented in Java code;<br>
2. 学习任何使用注解简化重复代码开发；<br>
Learn to use annotations to simplify repetitive code development;<br>

# Download

To add JavaPDispatcher to your project, include the following in your app module build.gradle file:

${latest.version} is  download  **0.0.1**

```
dependencies {
    implementation 'com.github.luoyonghui:javapdispatcher:${latest.version}'
    annotationProcessor 'com.github.luoyonghui:javapdispatcher-processor:${latest.version}'
}
```


# Use Example

注解解释：

1. **RuntimePermissions**: 注册需要权限操作的 Activity或者Fragment (Register an Activity or Fragment that requires permission to operate)<br>
2. **NeedsPermission**: 注册需要权限的方法，支持参数传递；(Register a method that requires permission, Support for passing method parameters) <br>
3. **OnDenied**: 注册拒绝授予权限时执行的方法； (Registration method for denying permission)<br>
4. **OnRationale**： 注册展示自定义解释弹窗的方法； （Register a method that shows the custom permission rationale popup） <br>
5. **OnNeverAsk**： 注册点击不在询问后提示用户的方法； （Register a method that click is not asking for a prompt） <br>


```
@RuntimePermissions
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.button_camera).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int param = 0;
                showCamera(param);
                MainActivity_PermissionsDispatcher.showCamera(MainActivity.this, param);
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
        Intent intent = new Intent(this, CameraActivity.class);
        startActivity(intent);
    }

    @NeedsPermission(value = Manifest.permission.CAMERA, position = 1)
    void showCamera2(int params) {
        Intent intent = new Intent(this, CameraActivity.class);
        startActivity(intent);
    }

    @OnDenied(value = Manifest.permission.CAMERA, position = 1)
    void onCameraDenied() {
        Toast.makeText(this, "camera permission be denied", Toast.LENGTH_SHORT).show();
    }

    @OnRationale(value = Manifest.permission.CAMERA, position = 1)
    void showRationaleForCamera(PermissionRequest request) {
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
```

# License

This plugin is available under the Apache License, Version 2.0.

(c) All rights reserved luoyonghui 
