# JavaPDispatcher

为Android 动态权限申请提供基于注解实现的类库，方便开发者使用尽可能少的代码对Android 动态权限进行适配。


# Use Example

被**RuntimePermissions** 修饰的类，在程序编译的过程中，才会自动生成授权代码；
被**NeedsPermission** 修饰的方法，在权限成功赋予之后执行，支持函数参数的传递。

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


# Download

To add JavaPDispatcher to your project, include the following in your app module build.gradle file:

${latest.version} is  download  **0.0.1**

```
dependencies {
    implementation 'com.github.luoyonghui:javapdispatcher:${latest.version}'
    annotationProcessor 'com.github.luoyonghui:javapdispatcher-processor:${latest.version}'
}
```
