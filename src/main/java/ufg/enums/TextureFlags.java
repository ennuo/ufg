package ufg.enums;

public class TextureFlags {
    public static final int NONE = 0;
    public static final int CLAMPU = 1;
    public static final int CLAMPV = 2;
    public static final int MIRRORU = 4;
    public static final int MIRRORV = 8;
    public static final int LINEAR = 256;
    public static final int LE = 512;
    public static final int CPU_WRITABLE = 1024;
    public static final int TARGET = 2048;
    public static final int PS3_TARGET_TILED = 4096;
    public static final int IN_INVENTORY = 8192;
    public static final int CREATED_AT_RUNTIME = 16384;
    public static final int MOVING = 0xffff8000;
    public static final int NO_EXPAND_AS_16 = 0x10000;
    public static final int MOVIE_TEXTURE = 0x20000;
    public static final int CPU_READABLE = 0x40000;
    public static final int MIPS_SHADER_SRC = 0x80000;
    public static final int PRESENT_BUFFER = 0x100000;
    public static final int ALIASED_TEXTURE = 0x200000;
    public static final int PC_MAIN_MEM_COPY = 0x400000;
    public static final int PC_UNORDERED_ACCESS = 0x800000;
    public static final int REBIND_DATAHANDLE = 0x1000000;
    public static final int XB1_USE_ESRAM = 0x2000000;
    public static final int MSAA_2X = 0x4000000;
    public static final int MSAA_4x = 0x8000000;
    public static final int SAMPLER_ADDRESS_MASK = 15;
    public static final int RUNTIME_MASK = 0x100a000;
}
