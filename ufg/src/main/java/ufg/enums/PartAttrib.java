package ufg.enums;

import ufg.io.ValueEnum;

public enum PartAttrib implements ValueEnum<Integer> {
    INT(0x0),
    UID(0x1),
    FLOAT(0x2),
    VEC2(0x3),
    VEC3(0x4),
    VEC4(0x5),
    MATRIX(0x6),
    STRING(0x7);

    private final int value;
    private PartAttrib(int value) {
        this.value = value & 0xFF;
    }

    public Integer getValue() { return this.value; }

    public static PartAttrib fromValue(int value) {
        for (PartAttrib type : PartAttrib.values()) {
            if (type.value == value) 
                return type;
        }
        return null;
    }
}
