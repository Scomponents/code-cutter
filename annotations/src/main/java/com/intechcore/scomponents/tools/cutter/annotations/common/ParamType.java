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

import com.intechcore.scomponents.tools.cutter.annotations.CutCode;

/**
 * The {@code ParamType} enum defines how parameters specified in a {@link CutCode}
 * annotation should be interpreted during code generation.
 */
public enum ParamType {
    /**
     * Indicates that the parameter string should be treated as a literal value.
     */
    LITERAL,
    /**
     * Indicates that the parameter string should be treated as the name of a
     * variable or a fully qualified expression.
     */
    VARIABLE;

    /**
     * Parses a string representation into a {@code ParamType} enum constant.
     * If the string does not match any existing enum constant, it defaults to {@link #LITERAL}.
     *
     * @param source The string to parse.
     * @return The corresponding {@code ParamType}, or {@link #LITERAL} if no match is found.
     */
    public static ParamType parse(String source) {
        try {
            return ParamType.valueOf(source);
        } catch (Exception ignored) {
        }

        return ParamType.LITERAL;
    }
}
