package ufg.enums;

import ufg.io.ValueEnum;

public enum BufferType implements ValueEnum<Byte> {
    VERTEX(0),
    INDEX(1),
    INSTANCE(2);

    private final byte value;
    private BufferType(int value) {
        this.value = (byte) (value & 0xFF);
    }

    public Byte getValue() { return this.value; }

    public static BufferType fromValue(byte value) {
        for (BufferType type : BufferType.values()) {
            if (type.value == value) 
                return type;
        }
        return null;
    }
}
