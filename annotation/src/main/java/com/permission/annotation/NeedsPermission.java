package com.permission.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Register some methods which permissions are needed.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.CLASS)
@MethodBase
public @interface NeedsPermission{
    String[] value();

    int maxSdkVersion() default 0;

    int position() default 0; // 如果有多个方法需要同一个权限，通过 position 区分是哪个函数
}