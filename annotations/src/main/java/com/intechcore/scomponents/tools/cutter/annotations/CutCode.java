/*
 * Copyright (c) 2026-present, Intechcore GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.intechcore.scomponents.tools.cutter.annotations;

import com.intechcore.scomponents.tools.cutter.annotations.common.CutCodes;
import com.intechcore.scomponents.tools.cutter.annotations.common.ParamType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The {@code @CutCode} annotation is used to mark methods that should be processed by the CutCodeProcessor.
 * This annotation allows for the replacement of method calls with alternative code
 * specified by the annotation's attributes. It is repeatable, meaning multiple
 * {@code @CutCode} annotations can be applied to a single method using the {@link CutCodes} container.
 *
 * The processing happens at compile time, effectively "cutting" out the original method
 * body and replacing it with generated code that invokes the specified {@code withCall}.
 *
 * @see CutCodes
 * @see CutCodeProcessConfig
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.SOURCE)
@Repeatable(CutCodes.class)
public @interface CutCode {
    /**
     * Specifies the fully qualified method call (e.g., "com.example.MyClass.myMethod")
     * that will replace the annotated method's body.
     * If empty, no call replacement will occur unless a profile specifies one.
     *
     * @return The method call string.
     */
    String withCall() default "";

    /**
     * Specifies the types of parameters for the {@link #withCall()} method.
     * This is used to determine how the {@link #callParams()} are interpreted
     * (e.g., as literals or as variable names).
     *
     * @return An array of {@link ParamType} indicating the type of each parameter.
     */
    ParamType[] callParamsTypes() default {};

    /**
     * Specifies the parameters to be passed to the {@link #withCall()} method.
     * The interpretation of these parameters depends on {@link #callParamsTypes()}.
     *
     * @return An array of strings representing the parameters.
     */
    String[] callParams() default {};

    /**
     * Specifies a profile name to load additional configuration for the cut code.
     * Profiles can define default {@code withCall}, {@code callParams}, and {@code callParamsTypes}.
     * If both a profile and direct annotation attributes are provided, the direct
     * attributes will override the profile's settings for the respective fields.
     *
     * @return The name of the profile to use.
     */
    String profile() default  "";
}


