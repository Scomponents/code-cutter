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

import com.intechcore.scomponents.tools.cutter.annotations.CutCode;
import com.intechcore.scomponents.tools.cutter.annotations.common.ParamType;

import java.lang.annotation.Annotation;
import java.util.Arrays;

/**
 * The {@code ProfileConfig} class represents a named profile for {@link CutCode} annotations.
 * It allows defining reusable configurations for code cutting, which can then be referenced
 * by the {@link CutCode#profile()} attribute. This class implements {@link CutCode}
 * to allow for easy merging of profile-defined attributes with annotation-defined attributes.
 */
class ProfileConfig implements CutCode {
    /**
     * The name of this profile.
     */
    public String name;
    /**
     * The method call defined by this profile.
     */
    public String withCall;
    /**
     * The parameters for the method call defined by this profile.
     */
    public String[] params;
    /**
     * The types of parameters for the method call defined by this profile.
     */
    public String[] paramsTypes;

    /**
     * {@inheritDoc}
     */
    @Override
    public String withCall() {
        return this.withCall;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ParamType[] callParamsTypes() {
        if (paramsTypes == null) {
            return null;
        }
        return Arrays.stream(this.paramsTypes).map(ParamType::parse).toArray(ParamType[]::new);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] callParams() {
        return this.params;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String profile() {
        return this.name;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<? extends Annotation> annotationType() {
        return CutCode.class;
    }

    /**
     * Creates a shallow clone of this {@code ProfileConfig} instance.
     *
     * @return A cloned {@code ProfileConfig} instance.
     */
    @Override
    public ProfileConfig clone() {
        ProfileConfig result = new ProfileConfig();
        result.name = this.name;
        result.withCall = this.withCall;
        result.params = this.params == null ? null : this.params.clone();
        result.paramsTypes = this.paramsTypes == null ? null : this.paramsTypes.clone();

        return result;
    }

    /**
     * Merges the attributes of this profile with another {@link CutCode} annotation.
     * Attributes from the {@code source} annotation will override attributes from this profile
     * if they are explicitly defined in the source (i.e., not null or empty).
     *
     * @param source The {@link CutCode} annotation to merge with.
     * @return A new {@link CutCode} instance representing the merged configuration.
     */
    public CutCode mergeWith(CutCode source) {
        ProfileConfig result = this.clone();

        if (source.withCall() != null && !source.withCall().isEmpty()) {
            result.withCall = source.withCall();
        }

        if (source.callParams() != null && source.callParams().length > 0) {
            result.params = source.callParams();
        }

        if (source.callParamsTypes() != null && source.callParamsTypes().length > 0) {
            result.paramsTypes = Arrays.stream(source.callParamsTypes())
                    .map(p -> p.name())
                    .toArray(String[]::new);
        }

        return result;
    }
}
