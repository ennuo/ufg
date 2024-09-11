package ufg.resources;

import ufg.io.Serializable;
import ufg.io.Serializer;
import ufg.io.streams.MemoryInputStream;
import ufg.structures.chunks.ResourceData;
import ufg.util.UFGCRC;

public class CollisionMeshData extends ResourceData {
    public int bundleGuid;
    public int geometryType;
    public int numVertices, numIndices;
    public int numParts;



    @SuppressWarnings("unchecked")
    @Override public CollisionMeshData serialize(Serializer serializer, Serializable structure) {
        CollisionMeshData data = (structure == null) ? new CollisionMeshData() : (CollisionMeshData) structure;

        super.serialize(serializer, data);

        data.bundleGuid = serializer.i32(data.bundleGuid);
        data.geometryType = serializer.i32(data.geometryType);
        data.numVertices = serializer.i32(data.numVertices);
        data.numIndices = serializer.i32(data.numIndices);
        data.numParts = serializer.i32(data.numParts);







        return data;
    }

    @Override public int getAllocatedSize() { return 0x500; }

    public static void main(String[] args) {
        System.out.println(UFGCRC.qStringHashUpper32("ufg_drfunk_to"));
        Serializer serializer = new Serializer(new MemoryInputStream("C:/Users/Aidan/Desktop/index"));
        CollisionMeshData data = serializer.struct(null, CollisionMeshData.class);
        System.out.println(data.numVertices);
    }
}
