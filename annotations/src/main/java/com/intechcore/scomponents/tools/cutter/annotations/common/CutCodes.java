package com.intechcore.scomponents.tools.cutter.annotations.common;

import com.intechcore.scomponents.tools.cutter.annotations.CutCode;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.SOURCE)
public @interface CutCodes {
    CutCode[] value();
}
