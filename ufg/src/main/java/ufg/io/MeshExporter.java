package ufg.io;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;

import org.joml.Vector4f;

import de.javagl.jgltf.impl.v2.Accessor;
import de.javagl.jgltf.impl.v2.Asset;
import de.javagl.jgltf.impl.v2.Buffer;
import de.javagl.jgltf.impl.v2.BufferView;
import de.javagl.jgltf.impl.v2.GlTF;
import de.javagl.jgltf.impl.v2.Image;
import de.javagl.jgltf.impl.v2.Material;
import de.javagl.jgltf.impl.v2.MaterialNormalTextureInfo;
import de.javagl.jgltf.impl.v2.MaterialPbrMetallicRoughness;
import de.javagl.jgltf.impl.v2.Mesh;
import de.javagl.jgltf.impl.v2.MeshPrimitive;
import de.javagl.jgltf.impl.v2.Node;
import de.javagl.jgltf.impl.v2.Scene;
import de.javagl.jgltf.impl.v2.Texture;
import de.javagl.jgltf.impl.v2.TextureInfo;
import de.javagl.jgltf.model.io.v2.GltfAssetV2;
import de.javagl.jgltf.model.io.v2.GltfAssetWriterV2;
import ufg.enums.VertexStreamElementUsage;
import ufg.io.streams.MemoryInputStream;
import ufg.io.streams.MemoryOutputStream;
import ufg.resources.Locators;
import ufg.resources.Material.MaterialParameter;
import ufg.resources.Model;
import ufg.resources.TexturePack;
import ufg.structures.chunks.ChunkFileIndexEntry;
import ufg.structures.chunks.ResourceData;
import ufg.structures.vertex.VertexStreamDescriptor;
import ufg.structures.vertex.VertexStreams;
import ufg.util.Bytes;

public class MeshExporter {

    private static class ExportContext {
        GlTF glTf = new GlTF();
        byte[] buffer = null;

        HashMap<Integer, ResourceData> modelStreamingResources; // .perm.bin, .perm.idx (small enough we can ignore the streaming part)
        HashMap<Integer, ResourceData> texturePackResources; // .perm.bin, .temp.bin
        HashMap<Integer, ChunkFileIndexEntry> textureStreamingResources; // .perm.bin, .perm.idx

        HashMap<Integer, Integer> materials = new HashMap<>();
        HashMap<Integer, Integer> textures = new HashMap<>();


        int numAccessors = 0;
        int numTextures = 0;
        int numViews = 0;

        HashMap<String, Integer> views = new HashMap<>();

        private ExportContext(HashMap<Integer, ResourceData> modelStreamingResources, HashMap<Integer, ResourceData> texturePackResources, HashMap<Integer, ChunkFileIndexEntry> textureStreamingResources) {
            this.modelStreamingResources = modelStreamingResources;
            this.texturePackResources = texturePackResources;
            this.textureStreamingResources = textureStreamingResources;
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

            int index = numViews++;
            this.views.put(name, index);

            return index;
        }

        public Integer addTexture(int textureUID) {
            if (this.textures.containsKey(textureUID))
                return this.textures.get(textureUID);
            
            ufg.resources.Texture texture = null;
            if (this.texturePackResources.containsKey(textureUID))
                texture = ((ufg.resources.Texture) this.texturePackResources.get(textureUID));
            else if (this.textureStreamingResources.containsKey(textureUID))
                texture = this.textureStreamingResources.get(textureUID).loadData(ufg.resources.Texture.class);

            if (texture == null) return null;

            // in MNR, imageDataPosition for image starts after ResourceData
            // in KARTING, imageDataPosition starts at beginning of chunk

            byte[] stream = null;

            if (this.texturePackResources.containsKey(texture.alphaStateSampler))
                stream = ((TexturePack) this.texturePackResources.get(texture.alphaStateSampler)).stream;
            
            else if (this.textureStreamingResources.containsKey(texture.alphaStateSampler))
                stream = this.textureStreamingResources.get(texture.alphaStateSampler).getTexturePackData();
            
            if (stream == null) return null;

            byte[] textureData = texture.toPNG(stream);
            if (textureData == null) return null;

            Image image = new Image();
            image.setBufferView(this.addBufferView(texture.name, buffer.length, textureData.length));
            image.setMimeType("image/png");
            image.setName(texture.name);

            this.buffer = Bytes.combine(this.buffer, textureData);

            int textureIndex = this.numTextures++;
            this.glTf.addImages(image);

            Texture glTexture = new Texture();

            glTexture.setSource(textureIndex);
            glTexture.setName(texture.name);

            this.glTf.addTextures(glTexture);

            this.textures.put(texture.UID, textureIndex);

            return textureIndex;
        }

        public Integer addMaterial(ufg.resources.Material material) {
            if (this.materials.containsKey(material.UID)) 
                return this.materials.get(material.UID);

            Material glMaterial = new Material();
            glMaterial.setName(material.name);

            // TODO: Actually check raster state
            glMaterial.setDoubleSided(true);

            MaterialPbrMetallicRoughness pbr = new MaterialPbrMetallicRoughness();
            glMaterial.setPbrMetallicRoughness(pbr);

            MaterialParameter diffuse = material.getProperty("texDiffuse");
            if (diffuse != null) {
                Integer index = this.addTexture(diffuse.valueUID);
                if (index != null) {
                    TextureInfo info = new TextureInfo();
                    info.setIndex(index);
                    pbr.setBaseColorTexture(info);
                }
            }

            MaterialParameter normal = material.getProperty("texNormal");
            if (normal != null) {
                Integer index = this.addTexture(normal.valueUID);
                if (index != null) {
                    MaterialNormalTextureInfo info = new MaterialNormalTextureInfo();
                    info.setIndex(index);
                    glMaterial.setNormalTexture(info);
                }
            }

            int index = this.materials.size();
            this.materials.put(material.UID, index);

            this.glTf.addMaterials(glMaterial);

            return index;
        }

        private boolean addModel(int modelUID) {
            Model model = (Model) this.modelStreamingResources.get(modelUID);
            if (model == null || model.meshes.length == 0 || model.numPrims == 0) return false;


            // TODO: Maybe find a better way of keeping track of stream duplicates?

            int[] vertCounts = new int[model.meshes.length];
            boolean[] hasSecondaryUVs = new boolean[model.meshes.length];
            boolean [] hasColors = new boolean[model.meshes.length];

            HashMap<Integer, Integer> streamMappings = new HashMap<>();
            HashMap<Integer, Integer> indexMappings = new HashMap<>();

            for (int i = 0; i < model.meshes.length; ++i) {
                int baseStreamOffset = this.buffer != null ? this.buffer.length : 0;

                ufg.structures.vertex.Mesh primary = model.meshes[i];

                int bufferHashCode = 1;
                for (int b = 0; b < 3; ++b)
                    bufferHashCode = 31 * bufferHashCode + primary.vertexBufferUIDs[b];
                
                if (streamMappings.containsKey(bufferHashCode)) {
                    int streamIndex = streamMappings.get(bufferHashCode);

                    vertCounts[i] = vertCounts[streamIndex];
                    hasSecondaryUVs[i] = hasSecondaryUVs[streamIndex];
                    hasColors[i] = hasColors[streamIndex];

                    this.views.put("POSITIONS_" + i, this.views.get("POSITIONS_" + streamIndex));
                    this.views.put("NORMALS_" + i, this.views.get("NORMALS_" + streamIndex));
                    this.views.put("TANGENTS_" + i, this.views.get("TANGENTS_" + streamIndex));
                    this.views.put("TEXCOORD0_" + i, this.views.get("TEXCOORD0_" + streamIndex));

                    if (hasSecondaryUVs[streamIndex])
                        this.views.put("TEXCOORD1_" + i, this.views.get("TEXCOORD1_" + streamIndex));
                    if (hasColors[streamIndex])
                        this.views.put("COLOR_" + i, this.views.get("COLOR_" + streamIndex));

                    if (model.meshes[streamIndex].indexBufferUID != model.meshes[i].indexBufferUID)
                        throw new RuntimeException("Index buffer mismatch for vertex stream!");
                    
                    this.views.put("INDICES_" + i, this.views.get("INDICES_" + streamIndex));
                    
                    continue;
                } else streamMappings.put(bufferHashCode, i);

                VertexStreamDescriptor decl = VertexStreams.get(primary.vertexDeclUID);
                ufg.resources.Buffer[] buffers = new ufg.resources.Buffer[] {
                    (ufg.resources.Buffer) this.modelStreamingResources.get(primary.vertexBufferUIDs[0]),
                    (ufg.resources.Buffer) this.modelStreamingResources.get(primary.vertexBufferUIDs[1]),
                    (ufg.resources.Buffer) this.modelStreamingResources.get(primary.vertexBufferUIDs[2]),
                    (ufg.resources.Buffer) this.modelStreamingResources.get(primary.vertexBufferUIDs[3])
                };


                boolean hasColor = decl.hasElement(VertexStreamElementUsage.COLOR0);
                boolean hasUV1 = decl.hasElement(VertexStreamElementUsage.TEXCOORD1);

                int vertCount = buffers[0].numElements;
                int indexCount = model.numPrims * 0x3;

                vertCounts[i] = vertCount;
                hasSecondaryUVs[i] = hasUV1;
                hasColors[i] = hasColor;

                MemoryOutputStream stream = new MemoryOutputStream(
                    (vertCount * 0xC) + // Positions
                    (vertCount * 0xC) + // Normals
                    (vertCount * 0x10) + // Tangents
                    (vertCount * (hasUV1 ? 0x10 : 0x8)) + // UV0/UV1
                    (vertCount * (hasColor ? 0x10 : 0)) + // Color
                    (indexCount * 0x2) // Indices
                );

                stream.setLittleEndian(true);

                this.addBufferView("POSITIONS_" + i, baseStreamOffset + stream.getOffset(), 0xC * vertCount);
                // int POSITIONS = this.addAccessor("POSITIONS", 5126, "VEC3", 0, vertCount);
                Vector4f[] vertices = decl.get(buffers, VertexStreamElementUsage.POSITION);
                for (Vector4f vertex : vertices) {
                    stream.f32(vertex.x);
                    stream.f32(vertex.y);
                    stream.f32(vertex.z);
                }

                this.addBufferView("NORMALS_" + i, baseStreamOffset + stream.getOffset(), 0xC * vertCount);
                // int NORMALS = this.addAccessor("NORMALS", 5126, "VEC3", 0, vertCount);
                Vector4f[] normals = decl.get(buffers, VertexStreamElementUsage.NORMAL);
                for (Vector4f vertex : normals) {
                    stream.f32(vertex.x);
                    stream.f32(vertex.y);
                    stream.f32(vertex.z);
                }
                
                this.addBufferView("TANGENTS_" + i, baseStreamOffset + stream.getOffset(), 0x10 * vertCount);
                // int TANGENTS = this.addAccessor("TEXCOORD0", 5126, "VEC4", 0, vertCount);
                Vector4f[] tangents = decl.get(buffers, VertexStreamElementUsage.TANGENT);
                for (Vector4f vertex : tangents) {
                    stream.f32(vertex.x);
                    stream.f32(vertex.y);
                    stream.f32(vertex.z);
                    stream.f32(1.0f);
                }

                this.addBufferView("TEXCOORD0_" + i, baseStreamOffset + stream.getOffset(), 0x8 * vertCount);
                // int UV0 = this.addAccessor("TEXCOORD0", 5126, "VEC2", 0, vertCount);
                Vector4f[] attributes = decl.get(buffers, VertexStreamElementUsage.TEXCOORD0);
                for (Vector4f vertex : attributes) {
                    stream.f32(vertex.x);
                    stream.f32(vertex.y);
                }

                if (hasUV1) {
                    this.addBufferView("TEXCOORD1_" + i, baseStreamOffset + stream.getOffset(), 0x8 * vertCount);
                    // UV1 = this.addAccessor("TEXCOORD1", 5126, "VEC2", 0, vertCount);
                    Vector4f[] attribs = decl.get(buffers, VertexStreamElementUsage.TEXCOORD1);
                    for (Vector4f vertex : attribs) {
                        stream.f32(vertex.x);
                        stream.f32(vertex.y);
                    }
                }

                if (hasColor) {
                    this.addBufferView("COLOR_" + i, baseStreamOffset + stream.getOffset(), 0x10 * vertCount);
                    // COLOR = this.addAccessor("COLOR", 5126, "VEC4", 0, vertCount);
                    Vector4f[] colors = decl.get(buffers, VertexStreamElementUsage.COLOR0);
                    for (Vector4f vertex : colors)
                        stream.v4(vertex);
                }

                if (indexMappings.containsKey(primary.indexBufferUID)) {
                    this.views.put("INDICES_" + i, indexMappings.get(primary.indexBufferUID));
                } else {
                    indexMappings.put(primary.indexBufferUID, this.addBufferView("INDICES_" + i, baseStreamOffset + stream.getOffset(), 0x2 * indexCount));
                    MemoryInputStream indexStream = new MemoryInputStream(((ufg.resources.Buffer) this.modelStreamingResources.get(primary.indexBufferUID)).data);
                    for (int j = 0; j < indexCount; ++j)
                        stream.u16(indexStream.u16());
                }

                if (this.buffer != null)
                    this.buffer = Bytes.combine(this.buffer, stream.getBuffer());
                else
                    this.buffer = stream.getBuffer();
            }

            Asset asset = new Asset();
            asset.setGenerator("UFG/IO v2.0");
            asset.setVersion("2.0");
            this.glTf.setAsset(asset);

            Mesh glMesh = new Mesh();
            glMesh.setName(model.name + ".MESH");

            int index = 0;
            for (ufg.structures.vertex.Mesh mesh : model.meshes) {
                int vertCount = vertCounts[index];

                MeshPrimitive primitive = new MeshPrimitive();
                primitive.addAttributes("POSITION", this.addAccessor("POSITIONS_" + index, 5126, "VEC3", 0, vertCount));
                primitive.addAttributes("NORMAL", this.addAccessor("NORMALS_" + index, 5126, "VEC3", 0, vertCount));
                primitive.addAttributes("TANGENT", this.addAccessor("TANGENTS_" + index, 5126, "VEC4", 0, vertCount));
                primitive.addAttributes("TEXCOORD_0", this.addAccessor("TEXCOORD0_" + index, 5126, "VEC2", 0, vertCount));
                if (hasSecondaryUVs[index])
                    primitive.addAttributes("TEXCOORD_1", this.addAccessor("TEXCOORD1_" + index, 5126, "VEC2", 0, vertCount));
                if (hasColors[index])
                    primitive.addAttributes("COLOR_0", this.addAccessor("COLOR_" + index, 5126, "VEC4", 0, vertCount));
                primitive.setIndices(this.addAccessor("INDICES_" + index, 5123, "SCALAR", mesh.indexStart * 0x2, mesh.numPrimitives * 0x3));

                primitive.setMaterial(this.addMaterial((ufg.resources.Material) this.modelStreamingResources.get(mesh.materialUID)));
                glMesh.addPrimitives(primitive);

                index++;
            }

            this.glTf.addMeshes(glMesh);

            int sceneNodeIndex = 0;

            Node root = new Node();
            root.setName(model.name);
            root.setMesh(0);
            this.glTf.addNodes(root);

            Scene scene = new Scene();
            scene.setName("Scene");
            scene.addNodes(sceneNodeIndex++);
            this.glTf.addScenes(scene);
            this.glTf.setScene(0);

            if (model.locatorsUID != 0) {
                Locators loc = (Locators)this.modelStreamingResources.get(model.locatorsUID);
                for (String key : loc.locators.keySet()) {
                    Node node = new Node();
                    node.setName(key);
                    node.setMatrix(loc.locators.get(key).get(new float[16]));
                    scene.addNodes(sceneNodeIndex++);
                    this.glTf.addNodes(node);
                }
            }

            Buffer buffer = new Buffer();
            buffer.setByteLength(this.buffer.length);
            this.glTf.addBuffers(buffer);

            return true;
        }
    }

    public static byte[] getGLB(int modelUID, HashMap<Integer, ResourceData> modelStreaming, HashMap<Integer, ResourceData> texturePackResources, HashMap<Integer, ChunkFileIndexEntry> texturePackStreaming) {

        ExportContext context = new ExportContext(modelStreaming, texturePackResources, texturePackStreaming);
        try {
            if (!context.addModel(modelUID))
                return null;
        } catch (Exception ex) { return null; }

        ByteBuffer buffer = ByteBuffer.wrap(context.buffer);
        GltfAssetV2 asset = new GltfAssetV2(context.glTf, buffer);
        GltfAssetWriterV2 writer = new GltfAssetWriterV2();

        try (ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
            writer.writeBinary(asset, stream);
            return stream.toByteArray();
        } catch (IOException ex) { return null; }
    }
}
