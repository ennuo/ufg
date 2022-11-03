package ufg.enums;

import ufg.io.ValueEnum;

public enum BuildType implements ValueEnum<Byte> {
    BUILT_WITH_CHUNK_SUBDIVISION(0),
    BUILT_WITHOUT_CHUNK_SUBDIVISION(1),
    BUILD_NOT_SET(2);

    private final byte value;
    private BuildType(int value) {
        this.value = (byte) (value & 0xFF);
    }

    public Byte getValue() { return this.value; }

    public static BuildType fromValue(byte value) {
        for (BuildType type : BuildType.values()) {
            if (type.value == value) 
                return type;
        }
        return null;
    }
}
