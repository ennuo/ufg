package ufg.enums;

import ufg.io.ValueEnum;

public enum MipmapBias implements ValueEnum<Byte> {
    UNSPECIFIED(0),
    SLIDER0(10),
    SLIDER1(11),
    SLIDER2(12),
    SLIDER3(13),
    SLIDER4(14),
    CUSTOM(15);

    private final byte value;
    private MipmapBias(int value) {
        this.value = (byte) (value & 0xff);
    }

    public Byte getValue() { return this.value; }

    public static MipmapBias fromValue(int value) {
        for (MipmapBias type : MipmapBias.values()) {
            if (type.value == value) 
                return type;
        }
        return null;
    }
}
