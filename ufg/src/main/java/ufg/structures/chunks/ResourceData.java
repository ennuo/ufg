package ufg.structures.chunks;

import ufg.io.Serializable;
import ufg.io.Serializer;

public class ResourceData implements Serializable {
    public static final int BASE_ALLOCATION_SIZE = 0x40;

    public int UID;
    public int typeUID;
    public String name;

    @SuppressWarnings("unchecked")
    @Override public ResourceData serialize(Serializer serializer, Serializable structure) {
        ResourceData data = (structure == null) ? new ResourceData() : (ResourceData) structure;

        serializer.pad(0xC);
        data.UID = serializer.i32(data.UID);
        serializer.pad(0x8);
        data.typeUID = serializer.i32(data.typeUID);
        data.name = serializer.str(data.name, 0x24);
        
        return data;
    }


    @Override public int getAllocatedSize() { return ResourceData.BASE_ALLOCATION_SIZE; }
}
