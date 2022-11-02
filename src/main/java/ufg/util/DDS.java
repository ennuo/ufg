package ufg.util;

import ufg.external.DDSReader;
import ufg.io.streams.MemoryOutputStream;
import ufg.resources.Texture;

import java.awt.image.BufferedImage;

public class DDS {
    public static int DDS_HEADER_FLAGS_TEXTURE = 0x00001007;
    public static int DDS_HEADER_FLAGS_MIPMAP = 0x00020000;
    public static int DDS_SURFACE_FLAGS_TEXTURE = 0x00001000;
    public static int DDS_SURFACE_FLAGS_MIPMAP = 0x00400008;

    public static int DDS_FOURCC = 0x4;
    public static int DDS_RGB = 0x40;
    public static int DDS_RGBA = 0x41;
    public static int DDS_LUMINANCE = 0x00020000;
    public static int DDS_LUMINANCEA = 0x00020001;
    
    public static int[] DDSPF_DXT1 = { 0x20, DDS_FOURCC, 0x31545844, 0, 0, 0, 0, 0 };
    public static int[] DDSPF_DXT3 = { 0x20, DDS_FOURCC, 0x33545844, 0, 0, 0, 0, 0 };
    public static int[] DDSPF_DXT5 = { 0x20, DDS_FOURCC, 0x35545844, 0, 0, 0, 0, 0 };
    public static int[] DDSPF_A8R8G8B8 = { 0x20, DDS_RGBA, 0, 32, 0x00ff0000, 0x0000ff00, 0x000000ff, 0xff000000 };
    public static int[] DDSPF_R5G6B5 = { 0x20, DDS_RGB, 0, 16, 0x0000f800, 0x000007e0, 0x0000001f, 0x00000000 };
    public static int[] DDSPF_A4R4G4B4 = { 0x20, DDS_RGBA, 0, 16, 0x00000f00, 0x000000f0, 0x0000000f, 0x0000f000 };
    public static int[] DDSPF_A16B16G16R16F = { 0x20, DDS_FOURCC, 113, 0, 0, 0, 0, 0 };
    public static int[] DDSPF_A8L8 = { 0x20, DDS_LUMINANCEA, 0, 16, 0xff, 0, 0, 0xff00 };
    public static int[] DDSPF_L8 = { 0x20, DDS_LUMINANCE, 0, 8, 0xff, 0, 0, 0 };
    public static int[] DDSPF_B8 = { 0x20, DDS_LUMINANCE, 0, 8, 0, 0, 0x000000ff, 0 };
    public static int[] DDSPF_A1R5G5B5 = { 0x20, DDS_RGBA, 0, 16, 0x00007c00, 0x000003e0, 0x0000001f, 0x00008000 };

    /**
     * Generates a DDS header.
     * @param texture Texture descriptor
     * @return Generated DDS header
     */
    public static byte[] getDDSHeader(Texture texture) {
        // For details on the DDS header structure, see:
        // https://docs.microsoft.com/en-us/windows/win32/direct3ddds/dds-header
        
        MemoryOutputStream header = new MemoryOutputStream(0x80);
        header.setLittleEndian(true);

        header.str("DDS ", 4);
        header.u32(0x7C); // dwSize
        header.u32(DDS.DDS_HEADER_FLAGS_TEXTURE | ((texture.numMipMaps != 0) ? DDS.DDS_HEADER_FLAGS_MIPMAP : 0));
        header.u32(texture.height);
        header.u32(texture.width);
        header.u32(0); // dwPitchOrLinearSize
        header.u32(0); // dwDepth
        header.u32(texture.numMipMaps & 0xff);
        for (int i = 0; i < 11; ++i)
            header.u32(0); // dwReserved[11]
        
        // DDS_PIXELFORMAT
        int[] pixelFormat = null;
        switch (texture.format) {
            case A1R5G5B5: pixelFormat = DDS.DDSPF_A1R5G5B5; break;
            case R5G6B5: pixelFormat = DDS.DDSPF_R5G6B5; break;
            case A8R8G8B8: pixelFormat = DDS.DDSPF_A8R8G8B8; break;
            case DXT1: pixelFormat = DDS.DDSPF_DXT1; break;
            case DXT3: pixelFormat = DDS.DDSPF_DXT3; break;
            case DXT5: pixelFormat = DDS.DDSPF_DXT5; break;
            default: throw new RuntimeException("Unknown or unimplemented DDS Type!");
        }
        for (int value : pixelFormat)
            header.u32(value);
        
        int surfaceFlags = DDS.DDS_SURFACE_FLAGS_TEXTURE;
        if (texture.numMipMaps != 0) surfaceFlags |= DDS.DDS_SURFACE_FLAGS_MIPMAP;
        header.u32(surfaceFlags);
        
        header.u32(0);
        
        for (int i = 0; i < 3; ++i)
            header.u32(0); // dwReserved
        
        return header.getBuffer();
    }

    /**
     * Reads a DDS file into a BufferedImage
     * @param DDS DDS to read
     * @return BufferedImage instance
     */
    public static BufferedImage toBufferedImage(byte[] DDS) {
        try {
            int[] pixels = DDSReader.read(DDS, DDSReader.ARGB, 0);
            int width = DDSReader.getWidth(DDS), height = DDSReader.getHeight(DDS);

            BufferedImage image = new BufferedImage(width, height, 2);

            image.setRGB(0, 0, width, height, pixels, 0, width);
            return image;
        } catch (Exception ex) { return null; }
    }
}
