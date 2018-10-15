package com.permission.processor.impl;

import com.permission.processor.impl.java.JavaActivityProcessorUnit;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.processing.Messager;

/**
 * create date:  2018/8/1
 * creator:  luoyonghui
 * functional description:
 */
public class ProcessorUnits {
    public static List javaProcessorUnits(Messager messager) {
        List list = new ArrayList();
        list.add(new JavaActivityProcessorUnit(messager));
        return list;
    }
}
