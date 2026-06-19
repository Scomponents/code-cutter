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

package com.intechcore.scomponents.tools.cutter.annotations.common;

/**
 * The {@code BoolForce} enum provides options to explicitly force a boolean value
 * (true or false), or to leave it unspecified (NONE) to defer to a default or
 * global configuration.
 */
public enum BoolForce {
    /**
     * Indicates that no explicit boolean force is applied, and the decision
     * should be based on default behavior or other configurations.
     * This is the default value when no explicit override is specified.
     */
    NONE,
    /**
     * Forces the boolean value to be {@code false}.
     * This will override any global or default configuration.
     */
    FORCE_FALSE,
    /**
     * Forces the boolean value to be {@code true}.
     * This will override any global or default configuration.
     */
    FORCE_TRUE
}
