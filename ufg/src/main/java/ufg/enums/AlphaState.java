package ufg.enums;

import ufg.io.ValueEnum;
import ufg.util.UFGCRC;

public enum AlphaState implements ValueEnum<Integer> {
    NONE("None"),
    MODULATED("Modulated"),
    MODULATED_PUNCH_THROUGH("ModulatedPunchThru"),
    MODULATED_RGB_SOLID_ALPHA("ModulatedRGBSolidAlpha"),
    ADDITIVE("Additive"),
    PREMULTIPLIED_ADDITIVE("PremultipliedAdditive"),
    PUNCH_THROUGH("PunchThru"),
    MASK("Mask"),
    OVERLAY("Overlay"),
    MIN("Min"),
    MAX("Max"),
    SUBTRACT("Subtract"),
    REVERSE_SUBTRACT("ReverseSubtract"),
    COPY("Copy");

    private final int value;
    private AlphaState(String value) {
        this.value = UFGCRC.qStringHash32("Illusion.AlphaState." + value);
    }

    public static void main(String[] args) {
        System.out.println(MODULATED.value);
    }

    public Integer getValue() { return this.value; }

    public static AlphaState fromValue(int value) {
        for (AlphaState state : AlphaState.values()) {
            if (state.value == value) 
                return state;
        }
        return null;
    }
}
