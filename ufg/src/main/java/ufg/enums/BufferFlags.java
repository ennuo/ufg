package ufg.enums;

public class BufferFlags {
    public static final int NONE = 0;
    public static final int CPU_READABLE = 1;
    public static final int CPU_WRITABLE = 2;
    public static final int FRAME_GENERATED = 4;
    public static final int SHADER_RESOURCE = 8;
    public static final int SHADER_UAV = 16;
    public static final int SHADER_STRUCTURED = 32;
    public static final int SHADER_ALLOW_RAW = 64;
}
