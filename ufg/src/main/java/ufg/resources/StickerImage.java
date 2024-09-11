package ufg.resources;

import ufg.enums.TextureFormat;
import ufg.io.Serializable;
import ufg.io.Serializer;
import ufg.structures.chunks.ResourceData;

public class StickerImage extends ResourceData {
    public static final int BASE_ALLOCATION_SIZE = 0x4044;
    public static final int TEXTURE_DATA_SIZE = 0x4000;

    public TextureFormat format = TextureFormat.DXT5;
    public byte[] imageData = new byte[TEXTURE_DATA_SIZE];

    @SuppressWarnings("unchecked")
    @Override public StickerImage serialize(Serializer serializer, Serializable structure) {
        StickerImage image = (structure == null) ? new StickerImage() : (StickerImage) structure;

        super.serialize(serializer, image);


        if (serializer.isWriting())
            serializer.getOutput().i32(image.format.getValue() & 0xff);
        else
            image.format = TextureFormat.fromValue(serializer.getInput().i32());

        image.imageData = serializer.bytes(image.imageData, TEXTURE_DATA_SIZE);

        return image;
    }

    @Override public int getAllocatedSize() { return StickerImage.BASE_ALLOCATION_SIZE; }
}
