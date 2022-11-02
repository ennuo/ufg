package ufg.enums;

import ufg.io.ValueEnum;
import ufg.utilities.UFGCRC;

public enum RasterState implements ValueEnum<Integer> {
    NONE("None"),
    NORMAL("Normal"),
    DISABLE_WRITE("DisableWrite"),
    INVERT_DISABLE_WRITE("InvertDisableWrite"),
    DOUBLE_SIDED("DoubleSided"),
    DOUBLE_SIDED_ALPHA("DoubleSidedAlpha"),
    INVERT_CULLING("InvertCulling"),
    NO_COLOR("NoColor"),
    ONLY_ALPHA("OnlyAlpha"),
    ONLY_ALPHA_CHECK_Z("OnlyAlphaCheckZ"),
    ONLY_COLOR("OnlyColour"),
    ONLY_RED("OnlyRed"),
    ONLY_GREEN("OnlyGreen"),
    ONLY_BLUE("OnlyBlue"),
    NO_COLOR_INVERT("NoColorInvert"),
    NO_COLOR_DOUBLE_SIDED("NoColorDoubleSided");

    private final int value;
    private RasterState(String value) {
        this.value = UFGCRC.qStringHash32("Illusion.RasterState." + value);
    }

    public Integer getValue() { return this.value; }

    public static RasterState fromValue(byte value) {
        for (RasterState state : RasterState.values()) {
            if (state.value == value) 
                return state;
        }
        return null;
    }
}
