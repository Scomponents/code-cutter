package com.intechcore.scomponents.tools.cutter.example;

import com.intechcore.scomponents.tools.cutter.annotations.CutCode;

public class PrimitiveTypesTests {

    public void run() {
        System.out.println("getDouble -> " + this.getDouble());
        System.out.println("getFloat -> " + this.getFloat());
        System.out.println("getChar -> " + this.getChar());
        System.out.println("getInt -> " + this.getInt());
        System.out.println("getShort -> " + this.getShort());
        System.out.println("getByte -> " + this.getByte());
        System.out.println("getBoolean -> " + this.getBoolean());
        System.out.println("getLong -> " + this.getLong());

        System.out.println("getBoxedString -> " + this.getBoxedString());
        System.out.println("getBoxedDouble -> " + this.getBoxedDouble());
        System.out.println("getBoxedFloat -> " + this.getBoxedFloat());
        System.out.println("getBoxedChar -> " + this.getBoxedChar());
        System.out.println("getBoxedInt -> " + this.getBoxedInt());
        System.out.println("getBoxedShort -> " + this.getBoxedShort());
        System.out.println("getBoxedByte -> " + this.getBoxedByte());
        System.out.println("getBoxedBoolean -> " + this.getBoxedBoolean());
        System.out.println("getBoxedLong -> " + this.getBoxedLong());
    }

    @CutCode(withCall = "System.out.println", callParams = {"getBoxedString - replaced 1"})
    @CutCode(profile = "profile1")
    private String getBoxedString() {
        return "String";
    }

    @CutCode(withCall = "System.out.println", callParams = {"getDouble - replaced 1"})
    @CutCode(profile = "profile1")
    private double getDouble() {
        return 15D;
    }

    @CutCode(withCall = "System.out.println", callParams = {"getFloat - replaced 1"})
    @CutCode(profile = "profile1")
    private float getFloat() {
        return 30f;
    }

    @CutCode(withCall = "System.out.println", callParams = {"getChar - replaced 1"})
    @CutCode(profile = "profile1")
    private char getChar() {
        return 'A';
    }

    @CutCode(withCall = "System.out.println", callParams = {"getInt - replaced 1"})
    @CutCode(profile = "profile1")
    private int getInt() {
        return 42;
    }

    @CutCode(withCall = "System.out.println", callParams = {"getShort - replaced 1"})
    @CutCode(profile = "profile1")
    private short getShort() {
        return 32;
    }

    @CutCode(withCall = "System.out.println", callParams = {"getByte - replaced 1"})
    @CutCode(profile = "profile1")
    private byte getByte() {
        return 127;
    }

    @CutCode(withCall = "System.out.println", callParams = {"getBoolean - replaced 1"})
    @CutCode(profile = "profile1")
    private boolean getBoolean() {
        return true;
    }

    @CutCode(withCall = "System.out.println", callParams = {"getLong - replaced 1"})
    @CutCode(profile = "profile1")
    private long getLong() {
        return Long.MAX_VALUE;
    }


    @CutCode(withCall = "System.out.println", callParams = {"getBoxedDouble - replaced 1"})
    @CutCode(profile = "profile1")
    private Double getBoxedDouble() {
        return 16D;
    }

    @CutCode(withCall = "System.out.println", callParams = {"getBoxedFloat - replaced 1"})
    @CutCode(profile = "profile1")
    private Float getBoxedFloat() {
        return 32f;
    }

    @CutCode(withCall = "System.out.println", callParams = {"getBoxedChar - replaced 1"})
    @CutCode(profile = "profile1")
    private Character getBoxedChar() {
        return 'F';
    }

    @CutCode(withCall = "System.out.println", callParams = {"getBoxedInt - replaced 1"})
    @CutCode(profile = "profile1")
    private Integer getBoxedInt() {
        return 44;
    }

    @CutCode(withCall = "System.out.println", callParams = {"getBoxedShort - replaced 1"})
    @CutCode(profile = "profile1")
    private Short getBoxedShort() {
        return 36;
    }

    @CutCode(withCall = "System.out.println", callParams = {"getBoxedByte - replaced 1"})
    @CutCode(profile = "profile1")
    private Byte getBoxedByte() {
        return 120;
    }

    @CutCode(withCall = "System.out.println", callParams = {"getBoxedBoolean - replaced 1"})
    @CutCode(profile = "profile1")
    private Boolean getBoxedBoolean() {
        return true;
    }

    @CutCode(withCall = "System.out.println", callParams = {"getBoxedLong - replaced 1"})
    @CutCode(profile = "profile1")
    private Long getBoxedLong() {
        return Long.MAX_VALUE - 10;
    }
}
