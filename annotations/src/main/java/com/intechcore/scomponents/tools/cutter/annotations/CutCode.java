package com.intechcore.scomponents.tools.cutter.annotations;

import com.intechcore.scomponents.tools.cutter.annotations.common.CutCodes;
import com.intechcore.scomponents.tools.cutter.annotations.common.ParamType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.SOURCE)
@Repeatable(CutCodes.class)
public @interface CutCode {
    String withCall() default "";
    ParamType[] callParamsTypes() default {};
    String[] callParams() default {};
    String profile() default  "";
}


