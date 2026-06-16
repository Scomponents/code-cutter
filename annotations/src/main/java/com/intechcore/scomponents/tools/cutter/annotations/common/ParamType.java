package com.intechcore.scomponents.tools.cutter.annotations.common;

public enum ParamType {
    LITERAL,
    VARIABLE;

    public static ParamType parse(String source) {
        try {
            return ParamType.valueOf(source);
        } catch (Exception ignored) {
        }

        return ParamType.LITERAL;
    }
}
