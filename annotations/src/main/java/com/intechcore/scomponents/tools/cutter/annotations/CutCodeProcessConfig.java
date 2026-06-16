package com.intechcore.scomponents.tools.cutter.annotations;

import com.intechcore.scomponents.tools.cutter.annotations.common.BoolForce;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.SOURCE)
public @interface CutCodeProcessConfig {
    BoolForce logProcessing() default BoolForce.NONE;
    BoolForce returnThisIfFound() default BoolForce.NONE;
}
