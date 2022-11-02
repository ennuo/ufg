package ufg.enums;

import ufg.io.ValueEnum;

public enum PrimitiveType implements ValueEnum<Integer> {
    UNKNOWN(0),
    POINT_LIST(1),
    LINE_LIST(2),
    TRIANGLE_LIST(3),
    TRIANGLE_STRIP(4);

    private final int value;
    private PrimitiveType(int value) {
        this.value = value;
    }

    public Integer getValue() { return this.value; }

    public static PrimitiveType fromValue(int value) {
        for (PrimitiveType type : PrimitiveType.values()) {
            if (type.value == value) 
                return type;
        }
        return null;
    }
}
