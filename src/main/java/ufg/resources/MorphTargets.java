package ufg.resources;

import ufg.io.Serializable;
import ufg.io.Serializer;
import ufg.structures.chunks.ResourceData;

public class MorphTargets extends ResourceData {
    public static final int BASE_ALLOCATION_SIZE = 
        ResourceData.BASE_ALLOCATION_SIZE + 0x20;

    public int targetBufferUID;
    public int[] targetUIDs;
    public String[] targetNames;

    @SuppressWarnings("unchecked")
    @Override public MorphTargets serialize(Serializer serializer, Serializable structure) {
        MorphTargets targets = (structure == null) ? new MorphTargets() : (MorphTargets) structure;

        super.serialize(serializer, targets);

        int targetCount = serializer.i32(targets.targetUIDs != null ? targets.targetUIDs.length : 0);
        serializer.pad(0xC);
        targets.targetBufferUID = serializer.i32(targets.targetBufferUID);

        int targetUIDsOffset = serializer.getOffset();
        targetUIDsOffset += serializer.i32(0xC);

        int targetNamesOffset = serializer.getOffset();
        targetNamesOffset += serializer.i32(0x8 + (0x4 * targetCount));

        serializer.seek(targetUIDsOffset);
        if (!serializer.isWriting()) targets.targetUIDs = new int[targetCount];
        for (int i = 0; i < targetCount; ++i)
            targets.targetUIDs[i] = serializer.i32(targets.targetUIDs[i]);
        
        serializer.seek(targetNamesOffset);
        if (!serializer.isWriting()) targets.targetNames = new String[targetCount];
        for (int i = 0; i < targetCount; ++i)
            targets.targetNames[i] = serializer.str(targets.targetNames[i], 0x40);

        return targets;
    }
    
    @Override public int getAllocatedSize() { 
        int size = MorphTargets.BASE_ALLOCATION_SIZE;
        size += (this.targetUIDs.length * 0x44);
        return size;
    }
}
