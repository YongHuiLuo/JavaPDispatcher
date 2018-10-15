package com.permission.annotation;

public interface GrantedRequest extends PermissionRequest {
    void grant();
}
