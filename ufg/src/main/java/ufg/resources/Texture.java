package ufg.resources;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;

import javax.imageio.ImageIO;

import ufg.enums.AlphaState;
import ufg.enums.MipmapBias;
import ufg.enums.TextureFilter;
import ufg.enums.TextureFlags;
import ufg.enums.TextureFormat;
import ufg.enums.TextureType;
import ufg.io.Serializable;
import ufg.io.Serializer;
import ufg.structures.chunks.ResourceData;
import ufg.util.DDS;
import ufg.util.ExecutionContext;
import ufg.util.UFGCRC;

public class Texture extends ResourceData {
    public static final int BASE_ALLOCATION_SIZE = 0x100;

    public int flags = TextureFlags.NONE;
    public TextureFormat format = TextureFormat.DXT5;
    public TextureType type = TextureType.T_2D;
    public byte aniso;
    public MipmapBias mipMapBiasPreset = MipmapBias.UNSPECIFIED;
    public int mipMipBias = 0xBF91EB85;
    public short width;
    public short height;
    public byte numMipMaps = 1;
    public TextureFilter filter = TextureFilter.DEFAULT;
    public short depth = 1;
    public AlphaState alphaState = AlphaState.NONE;

    public int alphaStateSampler;
    // TextureUser
    public int imageDataByteSize;
    // public int lastUsedFrameNum;
    public int imageDataPosition;
    // qVRAMemoryPool*
    // qMemoryPool*
    // qResourceFileHandle* textureDataHandle

    public Texture() { this.typeUID = UFGCRC.qStringHash32("Illusion.Texture"); }

    public byte[] toPNG(byte[] texturePack) { return this.toPNG(texturePack, false); }
    public byte[] toPNG(byte[] texturePack, boolean asDDS) {
        if (texturePack == null) return null;
        
        byte[] header = DDS.getDDSHeader(this);
        byte[] rawData = 
            Arrays.copyOfRange(texturePack, this.imageDataPosition, this.imageDataPosition + this.imageDataByteSize);
        
        byte[] dds = new byte[rawData.length + header.length];
        System.arraycopy(header, 0, dds, 0, header.length);
        System.arraycopy(rawData, 0, dds, header.length, rawData.length);

        if (asDDS) return dds;

        BufferedImage image = DDS.toBufferedImage(dds);
        if (image == null) return null;

        try (ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", stream);
            return stream.toByteArray();
        } catch (Exception ex) { return null; }
    }

    @SuppressWarnings("unchecked")
    @Override public Texture serialize(Serializer serializer, Serializable structure) {
        Texture texture = (structure == null) ? new Texture() : (Texture) structure;

        super.serialize(serializer, texture);

        texture.flags = serializer.i32(texture.flags);

        texture.format = serializer.enum8(texture.format);
        texture.type = serializer.enum8(texture.type);
        texture.aniso = serializer.i8(texture.aniso);
        
        texture.mipMapBiasPreset = serializer.enum8(texture.mipMapBiasPreset);
        texture.mipMipBias = serializer.i32(texture.mipMipBias);

        texture.width = serializer.i16(texture.width);
        texture.height = serializer.i16(texture.height);

        texture.numMipMaps = serializer.i8(texture.numMipMaps);
        texture.filter = serializer.enum8(texture.filter);
        texture.depth = serializer.i16(texture.depth);
        
        texture.alphaState = serializer.enum32(texture.alphaState);

        serializer.pad(0xC);
        texture.alphaStateSampler = serializer.i32(texture.alphaStateSampler);
        if (!ExecutionContext.isModNation())
            serializer.pad(0x8);

        texture.imageDataPosition = serializer.i32(texture.imageDataPosition);
        texture.imageDataByteSize = serializer.i32(texture.imageDataByteSize);

        // This is a mess down here, I don't know what actually maps to what
        // and what's just alignment, it doesn't really affect much in terms
        // of practical use, but even still.
        // Blagh!
        if (ExecutionContext.isModNation()) {
            serializer.i32(0x50);
            serializer.pad(0xC);
            serializer.i32(0x60);
            serializer.pad(0x8);
            serializer.i32(-1);
            serializer.pad(0x50);
        } else {
            serializer.i32(0x58);
            serializer.pad(0x14);
            serializer.i32(0x60);
            serializer.pad(0x5f);
        }

        serializer.str("BA0", 3);
        serializer.pad(0xa);

        return texture;
    }

    public int getSamplerAddressFlags() {
        return flags & 0xf;
    }

    public void setSamplerAddressFlags(int flags) {
        this.flags &= 0xfffffff0;
        this.flags |= flags;
    }

    @Override public int getAllocatedSize() {
        int size = Texture.BASE_ALLOCATION_SIZE;
        return size;
    }
}
