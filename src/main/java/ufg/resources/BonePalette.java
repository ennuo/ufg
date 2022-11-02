package ufg.resources;

import ufg.util.ExecutionContext;
import ufg.io.Serializable;
import ufg.io.Serializer;
import ufg.structures.chunks.ResourceData;
import ufg.utilities.UFGCRC;

public class BonePalette extends ResourceData {
    public static final int BASE_ALLOCATION_SIZE = 0x40 + 0xc0;

    private int numPalettes = 1;
    private int numBoneIndices;

    private byte[] boneIndices = new byte[0xa0];
    private String[] boneNames = new String[0xa0];
    private int[] boneUIDs = new int[0xa0];
    private int[] boneFullUIDs = new int[0xa0];

    public BonePalette() {
        this.typeUID = 0x50A819E3;
        for (int i = 0; i < 0xa0; ++i)
            this.boneIndices[i] = -1;
    }

    public int addBone(String name) {
        int index = this.getBoneIndex(name);
        if (index != -1) return index;
        
        index = this.numBoneIndices++;

        this.boneIndices[index] = (byte) index;
        this.boneNames[index] = name;
        this.boneUIDs[index] = UFGCRC.qStringHashUpper32(name);
        this.boneFullUIDs[index] = UFGCRC.qStringHashUpper32(name); 

        return index;
    }

    public int getBoneCount() { return this.numBoneIndices; }

    public int getBoneIndex(String name) {
        int uid = UFGCRC.qStringHashUpper32(name);
        for (int i = 0; i < this.boneUIDs.length; ++i) {
            if (this.boneUIDs[i] == uid)
                return i;
        }
        return -1;
    }

    @SuppressWarnings("unchecked")
    @Override public BonePalette serialize(Serializer serializer, Serializable structure) {
        BonePalette palette = (structure == null) ? new BonePalette() : (BonePalette) structure;

        super.serialize(serializer, palette);

        palette.numPalettes = serializer.i32(palette.numPalettes);
        palette.numBoneIndices = serializer.i32(palette.numBoneIndices);
        
        int boneNameOffset = serializer.getOffset();
        boneNameOffset += serializer.i32(0xb8);

        int boneUIDOffset = 0, boneFullUIDOffset = 0;
        if (!ExecutionContext.IS_MODNATION_RACERS) {
            boneUIDOffset = serializer.getOffset();
            boneUIDOffset += serializer.i32(0xb4 + (palette.numBoneIndices * 0x40));
    
            boneFullUIDOffset = serializer.getOffset();
            boneFullUIDOffset += serializer.i32(0xb0  + (palette.numBoneIndices * 0x40) + (0x4 * palette.numBoneIndices));
        }

        serializer.align(0x10);

        for (int i = 0; i < 0xA0; ++i)
            boneIndices[i] = serializer.i8(boneIndices[i]);

        serializer.seek(boneNameOffset);
        for (int i = 0; i < palette.numBoneIndices; ++i)
            palette.boneNames[i] = serializer.str(palette.boneNames[i], 0x40);

        if (!ExecutionContext.IS_MODNATION_RACERS) {
            serializer.seek(boneUIDOffset);
            for (int i = 0; i < palette.numBoneIndices; ++i)
                palette.boneUIDs[i] = serializer.i32(palette.boneUIDs[i]);
    
            serializer.seek(boneFullUIDOffset);
            for (int i = 0; i < palette.numBoneIndices; ++i)
                palette.boneFullUIDs[i] = serializer.i32(palette.boneFullUIDs[i]);
        }

        return palette;
    }

    @Override public int getAllocatedSize() {
        int size = BonePalette.BASE_ALLOCATION_SIZE;
        size += (this.boneNames.length * 0x40);
        size += (this.boneUIDs.length * 0x4);
        size += (this.boneFullUIDs.length * 0x4);
        return size;
    }
}
