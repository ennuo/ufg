package ufg.io;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;

import org.joml.Vector4f;

import ufg.util.ExecutionContext;
import ufg.io.streams.MemoryInputStream;
import ufg.io.streams.MemoryOutputStream;
import ufg.util.FileIO;
import de.javagl.jgltf.impl.v2.Accessor;
import de.javagl.jgltf.impl.v2.Asset;
import de.javagl.jgltf.impl.v2.Buffer;
import de.javagl.jgltf.impl.v2.BufferView;
import de.javagl.jgltf.impl.v2.GlTF;
import de.javagl.jgltf.impl.v2.Mesh;
import de.javagl.jgltf.impl.v2.MeshPrimitive;
import de.javagl.jgltf.impl.v2.Node;
import de.javagl.jgltf.impl.v2.Scene;
import de.javagl.jgltf.model.io.v2.GltfAssetV2;
import de.javagl.jgltf.model.io.v2.GltfAssetWriterV2;
import ufg.resources.ChunkFileIndex;
import ufg.resources.Model;
import ufg.structures.chunks.Chunk;
import ufg.structures.chunks.ChunkFileIndexEntry;
import ufg.structures.chunks.ResourceData;
import ufg.structures.vertex.VertexStreamDescriptor;
import ufg.structures.vertex.VertexStreams;
import ufg.enums.ResourceType;
import ufg.enums.VertexStreamElementUsage;

public class MeshExporter {

    private static class ExportContext {
        GlTF glTf = new GlTF();
        byte[] buffer = null;
        HashMap<Integer, ResourceData> resources;
        int numAccessors = 0;

        HashMap<String, Integer> views = new HashMap<>();

        private ExportContext(HashMap<Integer, ResourceData> resources) {
            this.resources = resources;
        }
        
        private int addAccessor(String viewKey, int componentType, String type, int offset, int count) {
            Accessor accessor = new Accessor();
            
            accessor.setBufferView(this.views.get(viewKey));
            accessor.setByteOffset(offset);
            accessor.setComponentType(componentType);
            accessor.setType(type);
            accessor.setCount(count);

            this.glTf.addAccessors(accessor);

            return this.numAccessors++;

        }

        private int addBufferView(String name, int offset, int length) {
            BufferView view = new BufferView();

            view.setBuffer(0);
            view.setByteOffset(offset);
            view.setByteLength(length);

            this.glTf.addBufferViews(view);

            int index = this.views.size();
            this.views.put(name, index);

            return index;
        }

        private boolean addModel(int modelUID) {
            Model model = (Model) resources.get(modelUID);
            if (model == null || model.meshes.length == 0 || model.numPrims == 0) return false;


            // First pass to get vertex data
            ufg.structures.vertex.Mesh primary = model.meshes[0];
            VertexStreamDescriptor decl = VertexStreams.get(primary.vertexDeclUID);
            ufg.resources.Buffer[] buffers = new ufg.resources.Buffer[] {
                (ufg.resources.Buffer) this.resources.get(primary.vertexBufferUIDs[0]),
                (ufg.resources.Buffer) this.resources.get(primary.vertexBufferUIDs[1]),
                (ufg.resources.Buffer) this.resources.get(primary.vertexBufferUIDs[2]),
                (ufg.resources.Buffer) this.resources.get(primary.vertexBufferUIDs[3])
            };


            boolean hasColor = decl.hasElement(VertexStreamElementUsage.COLOR0);
            boolean hasUV1 = decl.hasElement(VertexStreamElementUsage.TEXCOORD1);

            int vertCount = buffers[0].numElements;
            int indexCount = model.numPrims * 0x3;

            MemoryOutputStream stream = new MemoryOutputStream(
                (vertCount * 0xC) + // Positions
                (vertCount * 0xC) + // Normals
                (vertCount * 0x10) + // Tangents
                (vertCount * (hasUV1 ? 0x10 : 0x8)) + // UV0/UV1
                (vertCount * (hasColor ? 0x10 : 0)) + // Color
                (indexCount * 0x2) // Indices
            );

            stream.setLittleEndian(true);

            this.addBufferView("POSITIONS", stream.getOffset(), 0xC * vertCount);
            int POSITIONS = this.addAccessor("POSITIONS", 5126, "VEC3", 0, vertCount);
            Vector4f[] vertices = decl.get(buffers, VertexStreamElementUsage.POSITION);
            for (Vector4f vertex : vertices) {
                stream.f32(vertex.x);
                stream.f32(vertex.y);
                stream.f32(vertex.z);
            }

            this.addBufferView("NORMALS", stream.getOffset(), 0xC * vertCount);
            int NORMALS = this.addAccessor("NORMALS", 5126, "VEC3", 0, vertCount);
            Vector4f[] normals = decl.get(buffers, VertexStreamElementUsage.NORMAL);
            for (Vector4f vertex : normals) {
                stream.f32(vertex.x);
                stream.f32(vertex.y);
                stream.f32(vertex.z);
            }
            
            this.addBufferView("TANGENTS", stream.getOffset(), 0x10 * vertCount);
            int TANGENTS = this.addAccessor("TEXCOORD0", 5126, "VEC4", 0, vertCount);
            Vector4f[] tangents = decl.get(buffers, VertexStreamElementUsage.TANGENT);
            for (Vector4f vertex : tangents) {
                stream.f32(vertex.x);
                stream.f32(vertex.y);
                stream.f32(vertex.z);
                stream.f32(1.0f);
            }

            this.addBufferView("TEXCOORD0", stream.getOffset(), 0x8 * vertCount);
            int UV0 = this.addAccessor("TEXCOORD0", 5126, "VEC2", 0, vertCount);
            Vector4f[] attributes = decl.get(buffers, VertexStreamElementUsage.TEXCOORD0);
            for (Vector4f vertex : attributes) {
                stream.f32(vertex.x);
                stream.f32(vertex.y);
            }

            int UV1 = -1;
            if (hasUV1) {
                this.addBufferView("TEXCOORD1", stream.getOffset(), 0x8 * vertCount);
                UV1 = this.addAccessor("TEXCOORD1", 5126, "VEC2", 0, vertCount);
                Vector4f[] attribs = decl.get(buffers, VertexStreamElementUsage.TEXCOORD1);
                for (Vector4f vertex : attribs) {
                    stream.f32(vertex.x);
                    stream.f32(vertex.y);
                }
            }

            int COLOR = -1;
            if (hasColor) {
                this.addBufferView("COLOR", stream.getOffset(), 0x10 * vertCount);
                COLOR = this.addAccessor("COLOR", 5126, "VEC4", 0, vertCount);
                Vector4f[] colors = decl.get(buffers, VertexStreamElementUsage.COLOR0);
                for (Vector4f vertex : colors)
                    stream.v4(vertex);
            }

            this.addBufferView("INDICES", stream.getOffset(), 0x2 * indexCount);
            // Accessor indices = this.addAccessor("INDICES", 5123, "SCALAR", 0, vertCount)
            MemoryInputStream indexStream = new MemoryInputStream(((ufg.resources.Buffer) resources.get(primary.indexBufferUID)).data);
            for (int i = 0; i < indexCount; ++i)
                stream.i16(indexStream.i16());
            
            this.buffer = stream.getBuffer();

            Buffer buffer = new Buffer();
            buffer.setByteLength(this.buffer.length);
            this.glTf.addBuffers(buffer);

            Asset asset = new Asset();
            asset.setGenerator("UFG/IO v2.0");
            asset.setVersion("2.0");
            this.glTf.setAsset(asset);

            Mesh glMesh = new Mesh();
            glMesh.setName(model.name);
            
            for (ufg.structures.vertex.Mesh mesh : model.meshes) {
                MeshPrimitive primitive = new MeshPrimitive();
                primitive.addAttributes("POSITION", POSITIONS);
                primitive.addAttributes("NORMAL", NORMALS);
                primitive.addAttributes("TANGENT", TANGENTS);
                primitive.addAttributes("TEXCOORD_0", UV0);
                if (hasUV1)
                    primitive.addAttributes("TEXCOORD_1", UV1);
                if (hasColor)
                    primitive.addAttributes("COLOR_0", COLOR);
                primitive.setIndices(this.addAccessor("INDICES", 5123, "SCALAR", mesh.indexStart * 0x2, mesh.numPrimitives * 0x3));
                primitive.setMode(4);
                glMesh.addPrimitives(primitive);
            }

            this.glTf.addMeshes(glMesh);

            Node root = new Node();
            root.setName("mesh");
            root.setMesh(0);
            this.glTf.addNodes(root);

            Scene scene = new Scene();
            scene.setName("Scene");
            scene.addNodes(0);
            this.glTf.addScenes(scene);

            this.glTf.setScene(0);

            return true;
        }
    }

    public static byte[] getGLB(int modelUID, HashMap<Integer, ResourceData> resources) {

        ExportContext context = new ExportContext(resources);
        if (!context.addModel(modelUID))
            return null;

        ByteBuffer buffer = ByteBuffer.wrap(context.buffer);
        GltfAssetV2 asset = new GltfAssetV2(context.glTf, buffer);
        GltfAssetWriterV2 writer = new GltfAssetWriterV2();

        try (ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
            writer.writeBinary(asset, stream);
            return stream.toByteArray();
        } catch (IOException ex) { return null; }
    }

    public static void main(String[] args) {
        ChunkFileIndex index = Chunk.loadChunk("C:/Users/Aidan/Desktop/CHARMODELPACKSTREAMING.IDX").loadResource(ChunkFileIndex.class);
        Chunk[] chunks = Chunk.loadChunks("C:/Users/Aidan/Desktop/CHARMODELPACKSTREAMING.BIN");

        ExecutionContext.IS_MODNATION_RACERS = true;
        
        HashMap<Integer, ResourceData> resources = new HashMap<>();
        for (Chunk chunk : chunks) {
            ResourceType type = ResourceType.fromValue(chunk.UID);
            if (type == null) continue;
            ResourceData resource = chunk.loadResource(type.getSerializable());
            resources.put(resource.UID, resource);
        }

        for (ChunkFileIndexEntry entry : index.entries) {
            ResourceData data = resources.get(entry.filenameUID);
            if (!(data instanceof Model)) continue;

            byte[] glb = getGLB(entry.filenameUID, resources);
            if (glb == null) continue;
            FileIO.write(glb, "C:/Users/Aidan/Desktop/CHARACTERS/" + data.name + ".GLB");
        }
    }
}
