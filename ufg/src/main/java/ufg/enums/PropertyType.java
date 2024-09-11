package ufg.enums;

import ufg.io.ValueEnum;
import ufg.util.UFGCRC;

public enum PropertyType implements ValueEnum<Integer> {
    INT8("int8", 1),
    INT16("int16", 2),
    INT32("int32", 4),
    INT64("int64", 8),
    INT128("int128", 16),
    UINT8("uint8", 1),
    UINT16("uint16", 2),
    UINT32("uint32", 4),
    UINT64("uint64", 8),
    BOOL("bool", 1),
    FLOAT("float", 4),
    DOUBLE("double", 8),
    STRING("string", 0),
    MATRIX44("qMatrix44", 0x40),
    VECTOR2("qVector2", 0x8),
    VECTOR3("qVector3", 0xC),
    VECTOR4("qVector4", 0x10);

    private final int value;
    private final int size;
    private PropertyType(String value, int size) {
        this.value = UFGCRC.qStringHash32(value);
        this.size = size;
    }

    public Integer getValue() { return this.value; }
    public int getSize() { return this.size; }

    public static PropertyType fromValue(int value) {
        for (PropertyType type : PropertyType.values()) {
            if (type.value == value) 
                return type;
        }
        return null;
    }
}
