package com.intechcore.scomponents.tools.cutter.example;

public interface IFakeInterface {
    String getString();

    IInternalFake getInternalFake();

    interface IInternalFake {
        String getInternalString();

        IInternalFake.IDeepInternalState getDeepState();

        interface IDeepInternalState {
            int getInt();

            byte getByte();

            boolean getBoolean();

            Boolean getBoxedBoolean();
        }
    }

    static IFakeInterface create() {
        return new IFakeInterface() {
            @Override
            public String getString() {
                return "Fake String";
            }

            @Override
            public IInternalFake getInternalFake() {
                return new IInternalFake() {
                    @Override
                    public String getInternalString() {
                        return "Internal String";
                    }

                    @Override
                    public IDeepInternalState getDeepState() {
                        return new IDeepInternalState() {
                            @Override
                            public int getInt() {
                                return 42;
                            }

                            @Override
                            public byte getByte() {
                                return 127;
                            }

                            @Override
                            public boolean getBoolean() {
                                return true;
                            }

                            @Override
                            public Boolean getBoxedBoolean() {
                                return Boolean.TRUE;
                            }
                        };
                    }
                };
            }
        };
    }

    static void checkFakeInterfaceValues(IFakeInterface value) {
        final String PREFIX = IFakeInterface.class.getSimpleName();
        if (value == null) {
            System.out.println(PREFIX + "<NULL>");
            return;
        }
        System.out.println(PREFIX + ".getString() = " + value.getString());
        final String PREFIX_INTERNAL = PREFIX + ".internalFake";
        IInternalFake internalFake = value.getInternalFake();
        if (internalFake == null) {
            System.out.println(PREFIX_INTERNAL + " = <NULL>");
            return;
        }
        System.out.println(PREFIX_INTERNAL + ".getInternalString() = " + internalFake.getInternalString());
        final String PREFIX_DEEP_INTERNAL = PREFIX_INTERNAL + ".deepState";
        IInternalFake.IDeepInternalState deep = internalFake.getDeepState();
        if (deep == null) {
            System.out.println(PREFIX_DEEP_INTERNAL + " = <NULL>");
            return;
        }
        System.out.println(PREFIX_DEEP_INTERNAL + ".getBoolean() = " + deep.getBoolean());
        System.out.println(PREFIX_DEEP_INTERNAL + ".getBoxedBoolean() = " + deep.getBoxedBoolean());
        System.out.println(PREFIX_DEEP_INTERNAL + ".getByte() = " + deep.getByte());
        System.out.println(PREFIX_DEEP_INTERNAL + ".getInt() = " + deep.getInt());
    }
}
