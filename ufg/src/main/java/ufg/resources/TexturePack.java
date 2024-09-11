package ufg.resources;

import ufg.io.Serializable;
import ufg.io.Serializer;
import ufg.structures.chunks.ResourceData;
import ufg.util.ExecutionContext;

public class TexturePack extends ResourceData {
    public static final int BASE_ALLOCATION_SIZE = ResourceData.BASE_ALLOCATION_SIZE;
    public byte[] stream;
    
    @SuppressWarnings("unchecked")
    @Override public TexturePack serialize(Serializer serializer, Serializable structure) {
        TexturePack pack = (structure == null) ? new TexturePack() : (TexturePack) structure;

        if (ExecutionContext.isModNation())
            super.serialize(serializer, pack);

        if (serializer.isWriting()) serializer.getOutput().bytes(pack.stream);
        else pack.stream = serializer.getInput().bytes(serializer.getLength() - serializer.getOffset());

        return pack;
    }

    @Override public int getAllocatedSize() {
        int size = TexturePack.BASE_ALLOCATION_SIZE;
        return size + stream.length;
    }
}
