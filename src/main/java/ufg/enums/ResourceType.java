package ufg.enums;

import ufg.io.ValueEnum;
import ufg.resources.BonePalette;
import ufg.resources.Buffer;
import ufg.resources.ChunkFileIndex;
import ufg.resources.Material;
import ufg.resources.Model;
import ufg.resources.MorphTargets;
import ufg.resources.Texture;
import ufg.structures.chunks.ResourceData;

public enum ResourceType implements ValueEnum<Integer> {
    ATTRIBUTE(0xc41460b),
    CHUNK_FILE_INDEX(0x7040f7d2, ChunkFileIndex.class),
    BUFFER(0x7A971479, Buffer.class), // Illusion.Buffer.ChunkV1
    MATERIAL(0xF5F8516F, Material.class),
    TERMINATOR(0xDEADB0FF),
    XML_FILE(0x24d0c3a0),
    BONE_PALETTE(0x982456DB, BonePalette.class),
    COMPRESSED(0x680DED35),
    MORPH_TARGETS(0x02CD0C47, MorphTargets.class), // Illusion.MorphTargets.ChunkV1
    MODEL_DATA(0x6DF963B3, Model.class), // Illusion.Model.ChunkV1
    UI_LOCALIZATION(0x90CE6B7A),
    PARTDB_PART(0x8D43D0B4),
    TEXTURE_DATA(0x5E73CDD7),
    TEXTURE_DESCRIPTOR(0xCDBFA090, Texture.class); // Illusion.Texture.ChunkV1

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
}
