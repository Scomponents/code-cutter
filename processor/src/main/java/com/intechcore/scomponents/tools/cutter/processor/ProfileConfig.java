package com.intechcore.scomponents.tools.cutter.processor;

import com.intechcore.scomponents.tools.cutter.annotations.CutCode;
import com.intechcore.scomponents.tools.cutter.annotations.common.ParamType;

import java.lang.annotation.Annotation;
import java.util.Arrays;

class ProfileConfig implements CutCode {
    public String name;
    public String withCall;
    public String[] params;
    public String[] paramsTypes;

    @Override
    public String withCall() {
        return this.withCall;
    }

    @Override
    public ParamType[] callParamsTypes() {
        if (paramsTypes == null) {
            return null;
        }
        return Arrays.stream(this.paramsTypes).map(ParamType::parse).toArray(ParamType[]::new);
    }

    @Override
    public String[] callParams() {
        return this.params;
    }

    @Override
    public String profile() {
        return this.name;
    }

    @Override
    public Class<? extends Annotation> annotationType() {
        return CutCode.class;
    }

    @Override
    public ProfileConfig clone() {
        ProfileConfig result = new ProfileConfig();
        result.name = this.name;
        result.withCall = this.withCall;
        result.params = this.params == null ? null : this.params.clone();
        result.paramsTypes = this.paramsTypes == null ? null : this.paramsTypes.clone();

        return result;
    }

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
