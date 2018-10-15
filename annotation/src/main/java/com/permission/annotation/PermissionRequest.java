package com.permission.annotation;

/**
 * Interface used by {@link OnRationale} methods to allow for continuation
 * or cancellation of a permission request.
 */
public interface PermissionRequest {
    void proceed();

    void cancel();
}
