package ufg.structures.vertex;

import ufg.io.Serializable;
import ufg.io.Serializer;
import ufg.io.streams.MemoryInputStream;

public class SoftbodyData implements Serializable {
    public static int BASE_ALLOCATION_SIZE = 0x20 + 0x40;

    public short[] verletVertIndices;
    public short[] renderVertIndices;
    public int[] springData;
    public int[] renderVertToVerletVertMapper;

    @SuppressWarnings("unchecked")
    @Override public SoftbodyData serialize(Serializer serializer, Serializable structure) {
        SoftbodyData data = (structure == null) ? new SoftbodyData() : (SoftbodyData) structure;

        if (serializer.isWriting()) {
            



            return data;
        }

        MemoryInputStream stream = serializer.getInput();
        int verletOffset = stream.getOffset() + stream.i32();
        int renderOffset = stream.getOffset() + stream.i32();
        int springOffset = stream.getOffset() + stream.i32();
        int vertMap = stream.getOffset() + stream.i32();

        int numSoftVertIndices = stream.i32();
        int numRenderVertIndices = stream.i32();
        int numSpringData = stream.i32();
        int numMappedVerts = stream.i32();

        data.verletVertIndices = new short[numSoftVertIndices];
        data.renderVertIndices = new short[numRenderVertIndices];
        data.springData = new int[numSpringData];
        data.renderVertToVerletVertMapper = new int[numMappedVerts];

        stream.seek(verletOffset);
        for (int i = 0; i < numSoftVertIndices; ++i)
            data.verletVertIndices[i] = stream.i16();
        stream.seek(renderOffset);
        for (int i = 0; i < numRenderVertIndices; ++i)
            data.renderVertIndices[i] = stream.i16();
        stream.seek(springOffset);
        for (int i = 0; i < numSpringData; ++i)
            data.springData[i] = stream.i32();
        stream.seek(vertMap);
        for (int i = 0; i < numMappedVerts; ++i)
            data.renderVertToVerletVertMapper[i] = stream.i16();

        return data;
    }

    @Override public int getAllocatedSize() {
        int size = SoftbodyData.BASE_ALLOCATION_SIZE;
        if (this.verletVertIndices != null)
            size += (this.verletVertIndices.length * 0x2);
        if (this.renderVertIndices != null)
            size += (this.renderVertIndices.length * 0x2);
        if (this.springData != null)
            size += (this.springData.length * 0x4);
        if (this.renderVertToVerletVertMapper != null)
            size += (this.renderVertToVerletVertMapper.length * 0x2);
        return size;
    }
}
