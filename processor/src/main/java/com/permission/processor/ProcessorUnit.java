package com.permission.processor;

import javax.lang.model.type.TypeMirror;

/**
 * create date:  2018/8/1
 * creator:  luoyonghui
 * functional description:
 */
public interface ProcessorUnit<T> {
    TypeMirror getTargetType();

    T createFile(RuntimePermissionsElement rpe, RequestCodeProvider requestCodeProvider);
}
