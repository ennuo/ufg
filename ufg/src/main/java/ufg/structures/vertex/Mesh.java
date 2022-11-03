package ufg.structures.vertex;

import ufg.enums.PrimitiveType;
import ufg.util.ExecutionContext;
import ufg.io.Serializable;
import ufg.io.Serializer;

public class Mesh implements Serializable {
    public static final int BASE_ALLOCATION_SIZE = 0xa0;

    public int materialUID;
    public int vertexDeclUID;
    public int indexBufferUID;
    public int[] vertexBufferUIDs = new int[4];
    public PrimitiveType primitiveType = PrimitiveType.TRIANGLE_LIST;
    public int indexStart;
    public int numPrimitives;


    @SuppressWarnings("unchecked")
    @Override public Mesh serialize(Serializer serializer, Serializable structure) {
        Mesh mesh = (structure == null) ? new Mesh() : (Mesh) structure;

        serializer.pad(0xC);
        mesh.materialUID = serializer.i32(mesh.materialUID);

        serializer.pad(0xC);
        mesh.vertexDeclUID = serializer.i32(mesh.vertexDeclUID);

        serializer.pad(0xC);
        mesh.indexBufferUID = serializer.i32(mesh.indexBufferUID);

        for (int i = 0; i < 4; ++i) {
            serializer.pad(0xC);
            mesh.vertexBufferUIDs[i] = serializer.i32(mesh.vertexBufferUIDs[i]);
        }

        mesh.primitiveType = serializer.enum32(mesh.primitiveType);
        mesh.indexStart = serializer.i32(mesh.indexStart);
        mesh.numPrimitives = serializer.i32(mesh.numPrimitives);
        
        // Modnation Racers seems to keep a cached index here?
        // Don't feel like adding it right now though.

        // There's probably some technical alignment reasons to this
        // but I'll deal with it later
        serializer.pad(ExecutionContext.IS_MODNATION_RACERS ? 0x14 : 0x24);
        
        return mesh;
    }

    @Override public int getAllocatedSize() { return Mesh.BASE_ALLOCATION_SIZE; }
}
