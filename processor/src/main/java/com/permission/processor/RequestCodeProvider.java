package com.permission.processor;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * create date:  2018/8/1
 * creator:  luoyonghui
 * functional description:
 */
public final class RequestCodeProvider {
    private static final AtomicInteger currentCode = new AtomicInteger(0);

    public int nextRequestCode() {
        return currentCode.getAndIncrement();
    }
}
