package com.permission;

/**
 * create date:  2018/8/13
 * creator:  luoyonghui
 * functional description:
 */
public interface UnBinder {

    void unbind();

    UnBinder EMPTY = new UnBinder() {
        @Override
        public void unbind() {
        }
    };
}
