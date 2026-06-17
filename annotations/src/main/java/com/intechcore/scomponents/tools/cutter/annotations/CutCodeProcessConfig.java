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

import com.intechcore.scomponents.tools.cutter.annotations.common.BoolForce;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The {@code @CutCodeProcessConfig} annotation provides configuration options for how
 * the {@link CutCode} annotations are processed. It can be applied to a method
 * to override global processing settings for that specific method.
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.SOURCE)
public @interface CutCodeProcessConfig {
    /**
     * Controls whether logging for the code cutting process is enabled for the annotated method.
     * {@link BoolForce#NONE} means the global configuration determines logging.
     * {@link BoolForce#FORCE_TRUE} forces logging on for this method.
     * {@link BoolForce#FORCE_FALSE} forces logging off for this method.
     *
     * @return A {@link BoolForce} value indicating the logging preference.
     */
    BoolForce logProcessing() default BoolForce.NONE;

    /**
     * Controls whether the processor should attempt to return 'this' if a
     * {@link CutCode} annotation is found and the method's return type is compatible.
     * {@link BoolForce#NONE} means the global configuration determines this behavior.
     * {@link BoolForce#FORCE_TRUE} forces returning 'this'.
     * {@link BoolForce#FORCE_FALSE} prevents returning 'this'.
     *
     * @return A {@link BoolForce} value indicating the 'return this' preference.
     */
    BoolForce returnThisIfFound() default BoolForce.NONE;
}
