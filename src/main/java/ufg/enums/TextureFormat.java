package ufg.enums;

import ufg.io.ValueEnum;

public enum TextureFormat implements ValueEnum<Byte> {
    A8R8G8B8(0),
    DXT1(1),
    DXT3(2),
    DXT5(3),
    R5G6B5(4),
    A1R5G5B5(5),
    X8(6),
    X16(7),
    CXT1(8),
    DXN(9),
    BC6H_UF16(10),
    BC6H_SF16(11),
    BC7_UNORM(12),
    BC7_UNORM_SRGB(13),
    R32F(14),
    X16FY16FZ16FW16F(15),
    D24S8(16),
    D24FS8(17),
    SHADOW(18),
    DEPTHCOPY(19),
    A2R10G10B10(20),
    A2R10G10B10F(21),
    A16B16G16R16(22);

    private final byte value;
    private TextureFormat(int value) {
        this.value = (byte) (value & 0xff);
    }

    public Byte getValue() { return this.value; }

    public static TextureFormat fromValue(int value) {
        for (TextureFormat type : TextureFormat.values()) {
            if (type.value == value) 
                return type;
        }
        return null;
    }
}
