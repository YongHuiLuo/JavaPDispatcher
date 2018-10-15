package com.permission.processor.impl.java;

import com.permission.annotation.NeedsPermission;
import com.permission.processor.JavaProcessorUnit;
import com.permission.processor.RequestCodeProvider;
import com.permission.processor.RuntimePermissionsElement;
import com.permission.processor.util.Extension;
import com.permission.processor.util.Helper;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ArrayTypeName;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.annotation.processing.Messager;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;

/**
 * create date:  2018/8/1
 * creator:  luoyonghui
 * functional description:
 */
public abstract class JavaBaseProcessorUnit implements JavaProcessorUnit {

    private static final String DISPATCHER_PACKAGE_NAME = "com.permission.library";
    private static final String ANNOTATION_PACKAGE_NAME = "com.permission.annotation";
    private static final String SUPER_INTERFACE_GRANT_SIMPLE_NAME = "GrantedRequest";
    private static final String SUPER_INTERFACE_PERMISSION_SIMPLE_NAME = "PermissionRequest";
    private static final String PERMISSION_UTILS_SIMPLE_NAME = "PermissionUtils";

    protected static final ClassName PERMISSION_UTILS = ClassName.get(DISPATCHER_PACKAGE_NAME, PERMISSION_UTILS_SIMPLE_NAME);
    private static final ClassName BUILD = ClassName.get("android.os", "Build");
    private static final ClassName WEAK_REFERENCE = ClassName.get("java.lang.ref", "WeakReference");

    private static final String MANIFEST_WRITE_SETTING = "android.permission.WRITE_SETTINGS";
    private static final String MANIFEST_SYSTEM_ALERT_WINDOW = "android.permission.SYSTEM_ALERT_WINDOW";

    private final HashMap<String, SensitivePermissionInterface> ADD_WITH_CHECK_BODY_MAP = new HashMap<>();

    public JavaBaseProcessorUnit(Messager messager) {
        ADD_WITH_CHECK_BODY_MAP.put(MANIFEST_SYSTEM_ALERT_WINDOW, new SystemAlertWindowHelper());
        ADD_WITH_CHECK_BODY_MAP.put(MANIFEST_WRITE_SETTING, new WriteSettingsHelper());
    }

    @Override
    public JavaFile createFile(RuntimePermissionsElement rpe, RequestCodeProvider requestCodeProvider) {
        return JavaFile.builder(rpe.getPackageName(), createTypeSpec(rpe, requestCodeProvider))
                .build();
    }

    public abstract String getActivityName(String targetParam);

    public abstract void addShouldShowRequestPermissionRationaleCondition(MethodSpec.Builder builder, String targetParam, String permissionField, boolean isPositiveCondition);

    public abstract void addRequestPermissionsStatement(MethodSpec.Builder builder, String target, String permissionField, String requestCodeField);

    public abstract boolean isDeprecated();

    private TypeSpec createTypeSpec(RuntimePermissionsElement rpe, RequestCodeProvider requestCodeProvider) {
        TypeSpec.Builder classBuilder = TypeSpec.classBuilder(rpe.getGeneratedClassName())
                .addModifiers(Modifier.FINAL)
                .addFields(createFields(rpe.getNeedsElements(), requestCodeProvider))
                .addMethod(createConstructor())
                .addMethods(createWithPermissionCheckMethods(rpe))
                .addMethods(createPermissionHandlingMethods(rpe))
                .addTypes(createPermissionRequestClasses(rpe));

        if (isDeprecated()) {
            classBuilder.addAnnotation(createDeprecatedAnnotation());
        }
        return classBuilder.build();
    }

    private AnnotationSpec createDeprecatedAnnotation() {
        return AnnotationSpec.builder(Deprecated.class).build();
    }

    private List<FieldSpec> createFields(List<ExecutableElement> needsElements, RequestCodeProvider requestCodeProvider) {
        ArrayList<FieldSpec> fields = new ArrayList<>();
        for (ExecutableElement element : needsElements) {
            fields.add(createRequestCodeField(element, requestCodeProvider.nextRequestCode()));
            fields.add(createPermissionField(element));
            List<? extends VariableElement> params = element.getParameters();
            if (isNotEmpty(params)) {
                fields.add(createPendingRequestField(element));
            }
        }

        return fields;
    }

    private FieldSpec createRequestCodeField(ExecutableElement e, int index) {
        return FieldSpec.builder(TypeName.INT, Helper.requestCodeFieldName(e))
                .addModifiers(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                .initializer("$L", index)
                .build();
    }

    private FieldSpec createPermissionField(ExecutableElement e) {
        List<String> permissionValue = Extension.permissionValue(e.getAnnotation(NeedsPermission.class));
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        for (int i = 0; i < permissionValue.size(); i++) {
            builder.append("\"");
            builder.append(permissionValue.get(i));
            builder.append("\"");
            if (i != permissionValue.size() - 1) {
                builder.append(",");
            }
        }
        builder.append("}");

        String formattedValue = builder.toString();
        return FieldSpec.builder(ArrayTypeName.of(String.class), Helper.permissionFieldName(e))
                .addModifiers(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                .initializer("$N", "new String[]" + formattedValue)
                .build();
    }

    private FieldSpec createPendingRequestField(ExecutableElement e) {
        return FieldSpec.builder(ClassName.get(ANNOTATION_PACKAGE_NAME, SUPER_INTERFACE_GRANT_SIMPLE_NAME), Helper.pendingRequestFieldName(e))
                .addModifiers(Modifier.PRIVATE, Modifier.STATIC)
                .build();
    }

    private MethodSpec createConstructor() {
        return MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PRIVATE)
                .build();
    }

    private List<MethodSpec> createWithPermissionCheckMethods(RuntimePermissionsElement rpe) {
        ArrayList<MethodSpec> methods = new ArrayList<>();
        List<ExecutableElement> e = rpe.getNeedsElements();
        for (ExecutableElement executableElement : e) {
            methods.add(createWithPermissionCheckMethod(rpe, executableElement));
        }
        return methods;
    }

    private MethodSpec createWithPermissionCheckMethod(RuntimePermissionsElement rpe, ExecutableElement method) {
        String targetParam = "target";
        MethodSpec.Builder builder = MethodSpec.methodBuilder(Helper.withPermissionCheckMethodName(method))
                .addTypeVariables(rpe.getTypeVariables())
                .addModifiers(Modifier.STATIC)
                .returns(TypeName.VOID)
                .addParameter(rpe.getTypeName(), targetParam);

        List<VariableElement> variableElements = (List<VariableElement>) method.getParameters();
        for (VariableElement element : variableElements) {
            builder.addParameter(Helper.typeNameOf(element), Extension.simpleString(element));
        }

        addWithPermissionCheckBody(builder, method, rpe, targetParam);
        return builder.build();
    }

    public void addWithPermissionCheckBody(MethodSpec.Builder builder, ExecutableElement needsMethod, RuntimePermissionsElement rpe, String targetParam) {

        String requestCodeField = Helper.requestCodeFieldName(needsMethod);
        String permissionField = Helper.permissionFieldName(needsMethod);

        int maxSdkVersion = needsMethod.getAnnotation(NeedsPermission.class).maxSdkVersion();
        if (maxSdkVersion > 0) {
            builder.beginControlFlow("if ($T.VERSION.SDK_INT >$L)", BUILD, maxSdkVersion)
                    .addCode(CodeBlock.builder()
                            .add("$N.$N(", targetParam, Extension.simpleString(needsMethod))
                            .addStatement(")")
                            .addStatement("return")
                            .build())
                    .endControlFlow();
        }

        String needsPermissionParameter = needsMethod.getAnnotation(NeedsPermission.class).value()[0];
        String activityName = getActivityName(targetParam);

        if (ADD_WITH_CHECK_BODY_MAP.containsKey(needsPermissionParameter)) {
            ADD_WITH_CHECK_BODY_MAP.get(needsPermissionParameter).addHasSelfPermissionsCondition(builder, activityName, permissionField);
        } else {
            builder.beginControlFlow("if ($T.hasSelfPermissions($N, $N))", PERMISSION_UTILS, activityName, permissionField);
        }

        builder.addCode(CodeBlock.builder()
                .add("$N.$N(", targetParam, Extension.simpleString(needsMethod))
                .add(Helper.argsParametersCodeBlock(needsMethod))
                .addStatement(")")
                .build());
        builder.nextControlFlow("else");

        ExecutableElement onRationale = rpe.findOnRationaleForNeeds(needsMethod);
        boolean hasParameters = isNotEmpty(needsMethod.getParameters());
        if (hasParameters) {
            builder.addCode(CodeBlock.builder()
                    .add("$N = new $N($N,", Helper.pendingRequestFieldName(needsMethod)
                            , Helper.permissionRequestTypeName(rpe, needsMethod)
                            , targetParam)
                    .add(Helper.argsParametersCodeBlock(needsMethod))
                    .addStatement(")")
                    .build());
        }
        if (onRationale != null) {
            addShouldShowRequestPermissionRationaleCondition(builder, targetParam, permissionField, true);
            if (hasParameters) {
                builder.addStatement("$N.$N($N)", targetParam, Extension.simpleString(onRationale), Helper.pendingRequestFieldName(needsMethod));
            } else {
                builder.addStatement("$N.$N(new $N($N))", targetParam, Extension.simpleString(onRationale), Helper.permissionRequestTypeName(rpe, needsMethod), targetParam);
            }
            builder.nextControlFlow("else");
        }

        if (ADD_WITH_CHECK_BODY_MAP.containsKey(needsPermissionParameter)) {
            ADD_WITH_CHECK_BODY_MAP.get(needsPermissionParameter).addRequestPermissionsStatement(builder, targetParam, permissionField, requestCodeField);
        } else {
            addRequestPermissionsStatement(builder, targetParam, permissionField, requestCodeField);
        }
        if (onRationale != null) {
            builder.endControlFlow();
        }
        builder.endControlFlow();
    }

    public List<MethodSpec> createPermissionHandlingMethods(RuntimePermissionsElement rpe) {
        ArrayList<MethodSpec> methodSpecs = new ArrayList<>();
        if (hasNormalPermission(rpe)) {
            methodSpecs.add(createPermissionResultMethod(rpe));
        }

        if (hasSystemAlertWindowPermission(rpe) || hasWriteSettingPermission(rpe)) {
            methodSpecs.add(createOnActivityResultMethod(rpe));
        }

        return methodSpecs;
    }

    private boolean hasNormalPermission(RuntimePermissionsElement rpe) {
        List<ExecutableElement> list = rpe.getNeedsElements();
        for (ExecutableElement element : list) {
            List<String> values = Extension.permissionValue(element.getAnnotation(NeedsPermission.class));
            if (!values.contains(MANIFEST_SYSTEM_ALERT_WINDOW) && !values.contains(MANIFEST_WRITE_SETTING)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasSystemAlertWindowPermission(RuntimePermissionsElement rpe) {
        return isDefinePermission(rpe, MANIFEST_SYSTEM_ALERT_WINDOW);
    }

    private boolean hasWriteSettingPermission(RuntimePermissionsElement rpe) {
        return isDefinePermission(rpe, MANIFEST_WRITE_SETTING);
    }

    private boolean isDefinePermission(RuntimePermissionsElement rpe, String permissionName) {
        List<ExecutableElement> elements = rpe.getNeedsElements();
        for (ExecutableElement element : elements) {
            List<String> permissionValues = Extension.permissionValue(element.getAnnotation(NeedsPermission.class));
            if (permissionValues.contains(permissionName)) {
                return true;
            }
        }
        return false;
    }

    private MethodSpec createPermissionResultMethod(RuntimePermissionsElement rpe) {
        String targetParam = "target";
        String requestCodeParam = "requestCode";
        String grantResultsParam = "grantResults";
        MethodSpec.Builder builder = MethodSpec.methodBuilder("onRequestPermissionsResult")
                .addTypeVariables(rpe.getTypeVariables())
                .addModifiers(Modifier.STATIC)
                .addParameter(rpe.getTypeName(), targetParam)
                .addParameter(TypeName.INT, requestCodeParam)
                .addParameter(ArrayTypeName.of(TypeName.INT), grantResultsParam);

        builder.beginControlFlow("switch($N)", requestCodeParam);
        List<ExecutableElement> elements = rpe.getNeedsElements();
        for (ExecutableElement element : elements) {
            String needsPermissionParameter = element.getAnnotation(NeedsPermission.class).value()[0];
            if (ADD_WITH_CHECK_BODY_MAP.containsKey(needsPermissionParameter)) {
                continue;
            }
            builder.addCode("case $N:\n", Helper.requestCodeFieldName(element));
            addResultCaseBody(builder, element, rpe, targetParam, grantResultsParam);
        }
        builder.addCode("default:\n")
                .addStatement("break")
                .endControlFlow();
        return builder.build();
    }

    private MethodSpec createOnActivityResultMethod(RuntimePermissionsElement rpe) {
        String targetParam = "target";
        String requestCodeParam = "requestCode";
        String grantResultsParam = "grantResults";

        MethodSpec.Builder builder = MethodSpec.methodBuilder("onActivityResult")
                .addTypeVariables(rpe.getTypeVariables())
                .addModifiers(Modifier.STATIC)
                .returns(TypeName.VOID)
                .addParameter(rpe.getTypeName(), targetParam)
                .addParameter(TypeName.INT, requestCodeParam);

        builder.beginControlFlow("switch ($N)", requestCodeParam);
        List<ExecutableElement> elements = rpe.getNeedsElements();
        for (ExecutableElement element : elements) {
            String needsPermissionParameter = element.getAnnotation(NeedsPermission.class).value()[0];
            if (!ADD_WITH_CHECK_BODY_MAP.containsKey(needsPermissionParameter)) {
                continue;
            }
            builder.addCode("case $N:\n", Helper.requestCodeFieldName(element));
            addResultCaseBody(builder, element, rpe, targetParam, grantResultsParam);
        }
        builder.addCode("default:\n")
                .addStatement("break;")
                .endControlFlow();
        return builder.build();

    }

    private void addResultCaseBody(MethodSpec.Builder builder, ExecutableElement needsMethod, RuntimePermissionsElement rpe, String targetParam, String grantResultParam) {
        ExecutableElement onDenied = rpe.findOnDeniedForNeeds(needsMethod);
        boolean hasDenied = onDenied != null;
        String needsPermissionParameter = needsMethod.getAnnotation(NeedsPermission.class).value()[0];
        String permissionField = Helper.permissionFieldName(needsMethod);

        if (ADD_WITH_CHECK_BODY_MAP.containsKey(needsPermissionParameter)) {
            ADD_WITH_CHECK_BODY_MAP.get(needsPermissionParameter).addHasSelfPermissionsCondition(builder, getActivityName(targetParam), permissionField);
        } else {
            builder.beginControlFlow("if ($T.verifyPermissions($N))", PERMISSION_UTILS, grantResultParam);
        }

        boolean hasParameters = isNotEmpty(needsMethod.getParameters());
        if (hasParameters) {
            String pendingField = Helper.pendingRequestFieldName(needsMethod);
            builder.beginControlFlow("if ($N != null)", pendingField);
            builder.addStatement("$N.grant()", pendingField);
            builder.endControlFlow();
        } else {
            builder.addStatement("target.$N()", Extension.simpleString(needsMethod));
        }

        ExecutableElement onNeverAsk = rpe.findOnNeverAskForNeeds(needsMethod);
        boolean hasNeverAsk = onNeverAsk != null;

        if (hasDenied || hasNeverAsk) {
            builder.nextControlFlow("else");
        }

        if (hasNeverAsk) {
            addShouldShowRequestPermissionRationaleCondition(builder, targetParam, permissionField, false);
            builder.addStatement("target.$N() ", Extension.simpleString(onNeverAsk));

            if (hasDenied) {
                builder.nextControlFlow("else");
            } else {
                builder.endControlFlow();
            }
        }

        if (hasDenied) {
            builder.addStatement("$N.$N() ", targetParam, Extension.simpleString(onDenied));
            if (hasNeverAsk) {
                builder.endControlFlow();
            }
        }
        builder.endControlFlow();

        if (hasParameters) {
            builder.addStatement("$N = null", Helper.pendingRequestFieldName(needsMethod));
        }
        builder.addStatement("break");
    }


    private List<TypeSpec> createPermissionRequestClasses(RuntimePermissionsElement rpe) {
        ArrayList<TypeSpec> classes = new ArrayList<>();
        List<ExecutableElement> elements = rpe.getNeedsElements();
        for (ExecutableElement element : elements) {
            ExecutableElement onRationale = rpe.findOnRationaleForNeeds(element);
            if (onRationale != null || isNotEmpty(element.getParameters())) {
                classes.add(createPermissionRequestClass(rpe, element));
            }
        }
        return classes;
    }

    private TypeSpec createPermissionRequestClass(RuntimePermissionsElement rpe, ExecutableElement needsMethod) {

        boolean hasParameter = isNotEmpty(needsMethod.getParameters());
        String superInterfaceName = hasParameter ? SUPER_INTERFACE_GRANT_SIMPLE_NAME : SUPER_INTERFACE_PERMISSION_SIMPLE_NAME;

        TypeName targetType = rpe.getTypeName();

        TypeSpec.Builder builder = TypeSpec.classBuilder(Helper.permissionRequestTypeName(rpe, needsMethod))
                .addTypeVariables(rpe.getTypeVariables())
                .addSuperinterface(ClassName.get(ANNOTATION_PACKAGE_NAME, superInterfaceName))
                .addModifiers(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL);

        String weakFieldName = "weakTarget";
        // WeakReference<MainActivity>
        ParameterizedTypeName weakFieldType = ParameterizedTypeName.get(WEAK_REFERENCE, targetType);
        builder.addField(weakFieldType, weakFieldName, Modifier.PRIVATE, Modifier.FINAL);
        List<VariableElement> variableElements = (List<VariableElement>) needsMethod.getParameters();
        for (VariableElement element : variableElements) {
            builder.addField(Helper.typeNameOf(element), Extension.simpleString(element), Modifier.PRIVATE, Modifier.FINAL);
        }

        String targetParam = "target";
        MethodSpec.Builder constructorBuilder = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PRIVATE)
                .addParameter(targetType, targetParam)
                .addStatement("this.$L = new WeakReference<$T>($N)", weakFieldName, targetType, targetParam);
        for (VariableElement element : variableElements) {
            String fieldName = Extension.simpleString(element);
            constructorBuilder.addParameter(Helper.typeNameOf(element), fieldName)
                    .addStatement("this.$L = $N", fieldName, fieldName);
        }
        builder.addMethod(constructorBuilder.build());

        MethodSpec.Builder proceedMethod = MethodSpec.methodBuilder("proceed")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(TypeName.VOID)
                .addStatement("$T target = $N.get()", targetType, weakFieldName)
                .addStatement("if(target == null) return");

        String requestCodeField = Helper.requestCodeFieldName(needsMethod);
        String permissionName = needsMethod.getAnnotation(NeedsPermission.class).value()[0];
        if (ADD_WITH_CHECK_BODY_MAP.containsKey(permissionName)) {
            ADD_WITH_CHECK_BODY_MAP.get(permissionName).addRequestPermissionsStatement(proceedMethod, targetParam, getActivityName(targetParam), requestCodeField);
        } else {
            addRequestPermissionsStatement(proceedMethod, targetParam, Helper.permissionFieldName(needsMethod), requestCodeField);
        }
        builder.addMethod(proceedMethod.build());

        MethodSpec.Builder cancelMethod = MethodSpec.methodBuilder("cancel")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(TypeName.VOID);
        ExecutableElement onDenied = rpe.findOnDeniedForNeeds(needsMethod);
        if (onDenied != null) {
            cancelMethod
                    .addStatement("$T target = $N.get()", targetType, weakFieldName)
                    .addStatement("if (target == null) return")
                    .addStatement("target.$N()", Extension.simpleString(onDenied));
        }
        builder.addMethod(cancelMethod.build());

        if (hasParameter) {
            MethodSpec.Builder grantMethod = MethodSpec.methodBuilder("grant")
                    .addAnnotation(Override.class)
                    .addModifiers(Modifier.PUBLIC)
                    .returns(TypeName.VOID);

            grantMethod
                    .addStatement("$T target = $N.get()", targetType, weakFieldName)
                    .addStatement("if (target == null) return");

            grantMethod.addCode(
                    CodeBlock.builder()
                            .add("target.$N(", Extension.simpleString(needsMethod))
                            .add(Helper.argsParametersCodeBlock(needsMethod))
                            .addStatement(")")
                            .build());
            builder.addMethod(grantMethod.build());
        }

        return builder.build();
    }

    private boolean isNotEmpty(List list) {
        return list != null && list.size() > 0;
    }


}
