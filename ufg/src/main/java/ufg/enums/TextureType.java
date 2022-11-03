package ufg.enums;

import ufg.io.ValueEnum;

public enum TextureType implements ValueEnum<Byte> {
    T_2D(0),
    T_3D(1),
    T_CUBE(2),
    T_2D_ARRAY(3);

    private final byte value;
    private TextureType(int value) {
        this.value = (byte) (value & 0xff);
    }

    public Byte getValue() { return this.value; }

    public static TextureType fromValue(int value) {
        for (TextureType type : TextureType.values()) {
            if (type.value == value) 
                return type;
        }
        return null;
    }
}
