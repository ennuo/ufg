package ufg.enums;

import ufg.io.ValueEnum;
import ufg.resources.BonePalette;
import ufg.resources.Buffer;
import ufg.resources.ChunkFileIndex;
import ufg.resources.CreationMetadata;
import ufg.resources.Localization;
import ufg.resources.Locators;
import ufg.resources.Material;
import ufg.resources.Model;
import ufg.resources.MorphTargets;
import ufg.resources.StickerImage;
import ufg.resources.Texture;
import ufg.resources.TexturePack;
import ufg.structures.chunks.ResourceData;

public enum ResourceType implements ValueEnum<Integer> {
    ATTRIBUTE(0xc41460b),
    PC_CHUNK_FILE_INDEX(0xD2F74070, ChunkFileIndex.class),
    CHUNK_FILE_INDEX(0x7040f7d2, ChunkFileIndex.class),
    BUFFER(0x7A971479, Buffer.class), // Illusion.Buffer.ChunkV1
    MATERIAL(0xF5F8516F, Material.class),
    TERMINATOR(0xDEADB0FF),
    XML_FILE(0x24d0c3a0),
    LOCATORS(0x15506061, Locators.class),
    BONE_PALETTE(0x982456DB, BonePalette.class),
    COMPRESSED(0x680DED35),
    MORPH_TARGETS(0x02CD0C47, MorphTargets.class), // Illusion.MorphTargets.ChunkV1
    MODEL_DATA(0x6DF963B3, Model.class), // Illusion.Model.ChunkV1
    UI_LOCALIZATION(0x90CE6B7A, Localization.class),
    PARTDB_PART(0x8D43D0B4),
    TEXTURE_PACK(0x5E73CDD7, TexturePack.class),
    TEXTURE_DESCRIPTOR(0xCDBFA090, Texture.class), // Illusion.Texture.ChunkV1
    CREATION_METADATA(0x62CC092C, CreationMetadata.class),
    PART_DEFINITION(0x8509d307), // PartDefinitionV17Chunk
    STICKER_SAVED_IMAGES(0x1B6BED8B), // StickerSavedImagesV1ChunkUID
    STICKER_DEFINITION(0x000D23BD), // StickerDefinitionV5Chunk
    TRACK_DEFINITION(0x580579B6), // TrackDefinitionV7
    STICKER_IMAGE(0xAF4ACC8C, StickerImage.class); // StickerImageV2ChunkUID

    // LOCATORS 0x15506061


    // COSTUME CONSISTS OF
    // 5x PART_DEFINITION
    // 1x STICKER_SAVED_IMAGES
    // 1x STICKER_DEFINITION (0x001d504 in length)
    // Nx STICKER_IMAGE



    private final int value;
    private final Class<? extends ResourceData> clazz;

    private ResourceType(int value) {
        this.value = value;
        this.clazz = null;
    }

    private ResourceType(int value, Class<? extends ResourceData> clazz) {
        this.value = value;
        this.clazz = clazz;
    }

    public Class<? extends ResourceData> getSerializable() { return this.clazz; }
    public Integer getValue() { return this.value; }

    public static ResourceType fromValue(int value) {
        for (ResourceType type : ResourceType.values()) {
            if (type.value == value) 
                return type;
        }
        return null;
    }

    public static boolean isLatinLetter(char c) {
        return (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z');
    }


    public static void main(String[] args) {


        
        // System.out.println(Bytes.toHex(UFGCRC.qStringHashUpper32("LBP_MUS_Linear05")));
    }
}
