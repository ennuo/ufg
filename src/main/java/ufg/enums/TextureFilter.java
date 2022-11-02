package ufg.enums;

import ufg.io.ValueEnum;

public enum TextureFilter implements ValueEnum<Byte> {
    DEFAULT(0),
    LINEAR(1),
    POINT(2),
    ANISOTROPIC(3),
    CONVOLUTION(4);

    private final byte value;
    private TextureFilter(int value) {
        this.value = (byte) (value & 0xff);
    }

    public Byte getValue() { return this.value; }

    public static TextureFilter fromValue(int value) {
        for (TextureFilter type : TextureFilter.values()) {
            if (type.value == value) 
                return type;
        }
        return null;
    }
}
