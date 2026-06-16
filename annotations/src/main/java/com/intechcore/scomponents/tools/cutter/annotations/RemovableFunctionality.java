package com.intechcore.scomponents.tools.cutter.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.SOURCE)
public @interface RemovableFunctionality {
    boolean sendNotification() default true;

    String notificationText() default "This feature is disabled because this is a demo version of the application.";
}

