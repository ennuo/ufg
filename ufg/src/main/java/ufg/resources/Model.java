package ufg.resources;

import org.joml.Vector3f;

import ufg.io.Serializable;
import ufg.io.Serializer;
import ufg.structures.chunks.ResourceData;
import ufg.structures.vertex.Mesh;
import ufg.structures.vertex.SoftbodyData;
import ufg.util.ExecutionContext;
import ufg.util.UFGCRC;

public class Model extends ResourceData {
    public static final int BASE_ALLOCATION_SIZE = 
        ResourceData.BASE_ALLOCATION_SIZE + 0xB0;

    public Vector3f minAABB;
    public int numPrims;
    public Vector3f maxAABB;
    public int materialTableUID;
    public int bonePaletteUID;
    public int targetsUID;
    public int locatorsUID;
    public Mesh[] meshes;

    public int hasSoftbodyData;
    public SoftbodyData softbodyData = new SoftbodyData();

    public Model() { this.typeUID = UFGCRC.qStringHash32("Illusion.Model"); }

    @SuppressWarnings("unchecked")
    @Override public Model serialize(Serializer serializer, Serializable structure) {
        Model model = (structure == null) ? new Model() : (Model) structure;
        
        super.serialize(serializer, model);

        model.minAABB = serializer.v3(model.minAABB);
        model.numPrims = serializer.i32(model.numPrims);
        model.maxAABB = serializer.v3(model.maxAABB);
        serializer.pad(4);

        if (ExecutionContext.isModNation()) {
            serializer.pad(0x18);
            model.bonePaletteUID = serializer.i32(model.bonePaletteUID);
        } else {
            serializer.pad(0x8);
            model.materialTableUID = serializer.i32(model.materialTableUID);
            serializer.pad(0x4);
    
            serializer.pad(0x8);
            model.bonePaletteUID = serializer.i32(model.bonePaletteUID);
            serializer.pad(0x4);
    
            serializer.pad(0x8);
            model.targetsUID = serializer.i32(model.targetsUID);
            serializer.pad(0x4);

            if (ExecutionContext.isKarting())
            {
                serializer.pad(0x8);
                model.locatorsUID = serializer.i32(model.locatorsUID);
                serializer.pad(0x4);
            }
    
            serializer.pad(0xC); // some extra data?
        }

        int meshTableOffset = serializer.getOffset();
        meshTableOffset += serializer.i32(0x44);

        int numMeshes = serializer.i32(model.meshes != null ? model.meshes.length :  0);

        int modelUserOffset = serializer.getOffset();
        modelUserOffset += serializer.i32(0x2c);
        serializer.pad(0x8);

        int memoryPoolOffset = serializer.getOffset();
        memoryPoolOffset += serializer.i32(0x10);

        serializer.seek(memoryPoolOffset + 3);
        serializer.str("BA0", 3);

        serializer.seek(meshTableOffset);

        int MESH_INSTANCE_SIZE = ExecutionContext.isKarting() ? 0xa0 : 0x90;

        int[] meshOffsets = new int[numMeshes];
        if (serializer.isWriting()) {
            int meshTableSize = numMeshes * 4;
            if (meshTableSize % 16 != 0)
                meshTableSize += (16 - (meshTableSize % 16));
            int meshStart = serializer.getOffset() + meshTableSize;
            for (int i = 0; i < numMeshes; ++i) {
                meshOffsets[i] = meshStart + (MESH_INSTANCE_SIZE * i);
                serializer.i32(meshOffsets[i] - serializer.getOffset());
            }

        } else for (int i = 0; i < numMeshes; ++i)
            meshOffsets[i] = serializer.getOffset() + serializer.getInput().i32();

        if (!serializer.isWriting()) model.meshes = new Mesh[numMeshes];
        for (int i = 0; i < numMeshes; ++i) {
            serializer.seek(meshOffsets[i]);
            model.meshes[i] = serializer.struct(model.meshes[i], Mesh.class);
        }

        // this is softbodies? 0xb4 refers to softbody offset, number before it is hasSoftbodyInfo
        if (ExecutionContext.isKarting()) {
            int endOffset = serializer.getOffset();
            serializer.seek(modelUserOffset + 0x8);
            
            model.hasSoftbodyData = serializer.i32(model.hasSoftbodyData);
            int softbodyOffset = serializer.getOffset();
            softbodyOffset += serializer.i32(endOffset - softbodyOffset);
            serializer.seek(softbodyOffset);
            if (hasSoftbodyData > 0)
                model.softbodyData = serializer.struct(model.softbodyData, SoftbodyData.class);
        }

        // short* mVerletVertIndices
        // short* mRenderVertIndices
        // int* mSpringData
        // short* mRenderVertToVerletVertMapper
        // int mNumSoftVertIndices
        // int mNumRenderVertIndices
        // int mNumSpringData
        // int mNumRenderVertToVerletVertRapper

        return model;
    }

    @Override public int getAllocatedSize() {
        int size = Model.BASE_ALLOCATION_SIZE;

        int meshTableSize = this.meshes.length * 0x4;
        if (meshTableSize % 16 != 0)
            meshTableSize += (16 - (meshTableSize % 16));
        
        size += (meshTableSize + (this.meshes.length * Mesh.BASE_ALLOCATION_SIZE));

        if (this.softbodyData != null && this.hasSoftbodyData > 0)
            size += this.softbodyData.getAllocatedSize();

        return size;
    }
}
