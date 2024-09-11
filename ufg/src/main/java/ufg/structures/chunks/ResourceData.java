package ufg.structures.chunks;

import ufg.io.Serializable;
import ufg.io.Serializer;
import ufg.util.Bytes;
import ufg.util.UFGCRC;

public class ResourceData implements Serializable {
    public static final int BASE_ALLOCATION_SIZE = 0x40;

    public int UID;
    public int typeUID;
    public String name;

    @SuppressWarnings("unchecked")
    @Override public ResourceData serialize(Serializer serializer, Serializable structure) {
        ResourceData data = (structure == null) ? new ResourceData() : (ResourceData) structure;

        if (serializer.isWriting()) {
            if (data.name != null && data.name.length() > 0x23)
                data.name = data.name.substring(0, 0x23);
        }

        serializer.pad(0xC); // qBaseNode:: mParent, mChild[0], mChild[1]
        data.UID = serializer.i32(data.UID);
        serializer.pad(0x8); // qList::mNext, qList::mPrev
        data.typeUID = serializer.i32(data.typeUID);
        data.name = serializer.str(data.name, 0x24);
        
        return data;
    }


    @Override public int getAllocatedSize() { return ResourceData.BASE_ALLOCATION_SIZE; }
}
