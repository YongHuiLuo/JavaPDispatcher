package com.permission.processor.util;

import com.permission.processor.ProcessorUnit;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import javax.lang.model.element.Element;
import javax.lang.model.type.TypeMirror;


/**
 * create date:  2018/8/3
 * creator:  luoyonghui
 * functional description:
 */
public class Validators {

    public static ProcessorUnit findAndValidateProcessorUnit(List unit1s, Element element) {
        TypeMirror typeMirror = element.asType();
        Iterator iterator = unit1s.iterator();
        ProcessorUnit unit;
        do {
            if (!iterator.hasNext()) {
                throw new NoSuchElementException("Collection contains no element matching the predicate");
            }
            unit = (ProcessorUnit) iterator.next();
        } while (!Extension.isSubtypeOf(typeMirror, unit.getTargetType()));
        return unit;
    }
}
