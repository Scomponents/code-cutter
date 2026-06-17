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

package com.intechcore.scomponents.tools.cutter.processor;

import com.intechcore.scomponents.tools.cutter.annotations.common.BoolForce;
import com.intechcore.scomponents.tools.cutter.annotations.CutCodeProcessConfig;

/**
 * The {@code ProcessingConfig} class holds configuration settings for the {@link CutCodeProcessor}.
 * It manages global processing flags such as whether to log processing details and
 * how to handle 'this' returns. These settings can be overridden locally by
 * {@link com.intechcore.scomponents.tools.cutter.annotations.CutCodeProcessConfig} annotations.
 */
class ProcessingConfig {
    /**
     * Global setting for whether to log processing details.
     */
    public boolean logProcessing;
    /**
     * Global setting for whether to return 'this' if a cut code is found.
     */
    public boolean returnThisIfFound;

    private CutCodeProcessConfig localConfig;

    /**
     * Constructs a new {@code ProcessingConfig} with default values.
     * By default, logging is enabled and returning 'this' is enabled.
     */
    public ProcessingConfig() {
        this.logProcessing = true;
        this.returnThisIfFound = true;
    }

    /**
     * Sets the local processing configuration, typically from a
     * {@link com.intechcore.scomponents.tools.cutter.annotations.CutCodeProcessConfig} annotation.
     * This local configuration can override global settings.
     *
     * @param localConfig The local {@link CutCodeProcessConfig} to apply.
     */
    public void setLocalProcessingConfig(CutCodeProcessConfig localConfig) {
        this.localConfig = localConfig;
    }

    /**
     * Determines whether processing should be logged, considering both global
     * settings and any local overrides from {@link CutCodeProcessConfig}.
     *
     * @return {@code true} if logging is enabled, {@code false} otherwise.
     */
    public boolean logProcessing() {
        return this.getLocalizedValue(
                this.logProcessing,
                this.localConfig == null ? null : this.localConfig.logProcessing()
        );
    }

    /**
     * Determines whether to return 'this' if a cut code is found, considering
     * both global settings and any local overrides from {@link CutCodeProcessConfig}.
     *
     * @return {@code true} if returning 'this' is enabled, {@code false} otherwise.
     */
    public boolean returnThisIfFound() {
        return this.getLocalizedValue(
                this.returnThisIfFound,
                this.localConfig == null ? null : this.localConfig.returnThisIfFound()
        );
    }

    /**
     * Resolves the effective boolean value by applying a local {@link com.intechcore.scomponents.tools.cutter.annotations.common.BoolForce}
     * override to a target boolean value.
     *
     * @param target The global or default boolean value.
     * @param local  The local {@link com.intechcore.scomponents.tools.cutter.annotations.common.BoolForce} override.
     * @return The resolved boolean value.
     */
    private boolean getLocalizedValue(boolean target, BoolForce local) {
        if (local == null || local == BoolForce.NONE) {
            return target;
        }
        return local == BoolForce.FORCE_TRUE;
    }
}
