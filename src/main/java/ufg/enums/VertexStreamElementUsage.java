package ufg.enums;

public enum VertexStreamElementUsage {
    POSITION(0),
    NORMAL(1),
    TANGENT(2),
    COLOR0(3),
    COLOR1(4),
    TEXCOORD0(5),
    TEXCOORD1(6),
    TEXCOORD2(7),
    TEXCOORD3(8),
    TEXCOORD4(9),
    TEXCOORD5(10),
    TEXCOORD6(11),
    TEXCOORD7(12),
    BLENDINDEX(13),
    BLENDWEIGHT(14),
    BINORMAL(15);

    private final int index;
    private VertexStreamElementUsage(int index) {
        this.index = index;
    }

    public int getIndex() { return this.index; }
}
