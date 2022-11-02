package executables.assembler;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;

import org.joml.Vector3f;
import org.joml.Vector4f;

import ufg.io.Serializer;
import de.javagl.jgltf.model.AccessorModel;
import de.javagl.jgltf.model.MaterialModel;
import de.javagl.jgltf.model.MeshModel;
import de.javagl.jgltf.model.MeshPrimitiveModel;
import de.javagl.jgltf.model.NodeModel;
import de.javagl.jgltf.model.SkinModel;
import de.javagl.jgltf.model.io.GltfModelReader;
import de.javagl.jgltf.model.v2.GltfModelV2;
import executables.assembler.TextureChunker.TextureChunkerResult;
import ufg.enums.BufferType;
import ufg.enums.PrimitiveType;
import ufg.enums.ResourceType;
import ufg.enums.VertexStreamElementUsage;
import ufg.resources.BonePalette;
import ufg.resources.Buffer;
import ufg.resources.Material;
import ufg.resources.Model;
import ufg.resources.Texture;
import ufg.structures.chunks.Chunk;
import ufg.structures.vertex.Mesh;
import ufg.structures.vertex.VertexStreamDescriptor;
import ufg.structures.vertex.VertexStreams;
import ufg.utilities.UFGCRC;

public class MeshChunker {
    public static class MeshChunkerResult {
        public Chunk[] modelStreamingChunks;
        public Texture[] textureStreamingChunks;
        public byte[][] texturePackData;
    }

    private static HashSet<String> bones = new HashSet<>();

    private static BonePalette createSackboySkeleton() {
        BonePalette palette = new BonePalette();

        palette.name = "SACKBOY.BonePalette";
        palette.UID = UFGCRC.qStringHashUpper32(palette.name);

        // String[] bones = new String[] {
        //     "Bip01", "Bip01 Pelvis", "Bip01 Spine", "Bip01 Spine1", "Bip01 Neck", "Bip01 Head",
        //     "bone_eye_pos_left", "bone_eye_left", "bone_eye_pos_right", "bone_eye_right", "Bone_jaw",
        //     "bone_tongue01", "bone_tongue02", "Bip01 L Clavicle", "Bip01 L UpperArm", "Bip01 L Forearm",
        //     "Bip01 L Hand", "Bip01 L Finger0", "Bip01 L Finger1", "Bip01 L Finger2", "Bip01 L Finger3",
        //     "Bip01 L Finger4", "Bip01 R Clavicle", "Bip01 R UpperArm", "Bip01 R Hand", "Bip01 R Finger0",
        //     "Bip01 R Finger1", "Bip01 R Finger2", "Bip01 R Finger3", "Bip01 R Finger4", "bone_zip_pull01",
        //     "bone_zip_pull02", "Bip01 L Thigh", "Bip01 L Calf", "Bip01 L Foot", "Bip01 L Toe0", "Bip01 R Thigh",
        //     "Bip01 R Calf", "Bip01 R Foot", "Bip01 R Toe0"
        // };

        // for (String bone : bones)
        //     palette.addBone(bone);

        return palette;

    }

    private static String mapSabaBone(String name) {
        String original = name;
        name = name.toLowerCase();

        if (name.contains("tongue03")) name = "bone_tongue02";
        if (name.contains("tongue04")) name = "bone_tongue02";

        if (name.equals("tongue_1_0")) name = "bone_tongue01";
        if (name.equals("tongue_1_1")) name = "bone_tongue02";
        if (name.equals("tongue_1_2")) name = "bone_tongue02";
        if (name.equals("tongue_1_3")) name = "bone_tongue02";

        if (name.equals("root")) name = "Bip01";
        if (name.equals("cog")) name = "Bip01";

        if (name.equals("l_wrist_1")) name = "Bip01 L Hand";
        if (name.equals("r_wrist_1")) name = "Bip01 R Hand";

        if (name.equals("spine00")) name = "Bip01 Pelvis";
        if (name.equals("spine01")) name = "Bip01 Spine";
        if (name.equals("spine02")) name = "Bip01 Spine1";

        if (name.equals("l_shoulder_1")) name = "Bip01 L Clavicle";
        if (name.equals("r_shoulder_1")) name = "Bip01 R Clavicle";

        if (name.equals("neck_1")) name = "Bip01 Neck";

        if (name.equals("r_arm_1")) name = "Bip01 R UpperArm";
        if (name.equals("r_arm_1_roll_01")) name = "Bip01 R UpperArm";
        if (name.equals("r_arm_1_roll_02")) name = "Bip01 R UpperArm";
        if (name.equals("l_arm_1_roll_01")) name = "Bip01 L UpperArm";
        if (name.equals("l_arm_1_roll_02")) name = "Bip01 L UpperArm";
        if (name.equals("l_arm_1")) name = "Bip01 L UpperArm";

        if (name.equals("head_1")) name = "Bip01 Head";

        if (name.equals("r_elbow_1")) name ="Bip01 R Forearm";
        if (name.equals("r_elbow_1_scale_01")) name ="Bip01 R Forearm";
        if (name.equals("l_elbow_1_scale_01")) name ="Bip01 L Forearm";
        if (name.equals("r_elbow_1_roll_01")) name ="Bip01 R Forearm";
        if (name.equals("l_elbow_1_roll_01")) name = "Bip01 L Forearm";
        if (name.equals("l_elbow_1")) name = "Bip01 L Forearm";

        if (name.equals("rear_sternum_attach_01")) name = "Bip01 Neck";
        if (name.equals("sternum")) name = "Bip01 Neck";
        if (name.equals("sternum01")) name = "Bip01 Neck"; // upper torso
        if (name.equals("sternum03")) name = "Bip01 Neck";
        if (name.equals("sternum02")) name = "Bip01 Neck";

        if (name.equals("l_leg_1")) name = "Bip01 L Thigh";
        if (name.equals("r_leg_1")) name = "Bip01 R Thigh";

        if (name.equals("head_1")) name = "Bip01 Head";
        if (name.equals("jaw_1")) name = "Bone_jaw";

        if (name.equals("l_eye_1")) name = "bone_eye_pos_left";
        if (name.equals("r_eye_1")) name = "bone_eye_pos_right";

        if (name.equals("r_hat_01")) name = "Bip01 Head";
        if (name.equals("r_hat_02")) name = "Bip01 Head";
        if (name.equals("r_hat_03")) name = "Bip01 Head";
        
        if (name.equals("l_hat_01")) name = "Bip01 Head";
        if (name.equals("l_hat_02")) name = "Bip01 Head";
        if (name.equals("l_hat_03")) name = "Bip01 Head";

        if (name.equals("l_ankle_1")) name = "Bip01 L foot";
        if (name.equals("r_ankle_1")) name = "Bip01 R foot";

        if (name.equals("r_ball_1")) name = "Bip01 R Foot";
        if (name.equals("l_ball_1")) name = "Bip01 L Foot";

        if (name.equals("r_knee_1")) name ="Bip01 R Calf";
        if (name.equals("l_knee_1")) name ="Bip01 L Calf";

        if (name.equals("l_knee_1_roll_01")) name = "Bip01 L Calf";
        if (name.equals("r_knee_1_roll_01")) name = "Bip01 R Calf";

        if (name.equals("l_knee_1_roll_02")) name = "Bip01 L Calf";
        if (name.equals("r_knee_1_roll_02")) name = "Bip01 R Calf";

        if (name.equals("l_knee_1_roll_03")) name = "Bip01 L Calf";
        if (name.equals("r_knee_1_roll_03")) name = "Bip01 R Calf";

        if (name.equals("l_leg_1_roll_01")) name = "Bip01 L Thigh";
        if (name.equals("r_leg_1_roll_01")) name = "Bip01 R Thigh";
        if (name.equals("l_leg_1_roll_02")) name = "Bip01 L Thigh";
        if (name.equals("r_leg_1_roll_02")) name = "Bip01 R Thigh";

        if (name.equals("r_leg_1")) name = "Bip01 R Thigh";
        if (name.equals("l_leg_1")) name = "Bip01 L Thigh";

        if (name.equals("l_pinkie_1_1")) name = "Bip01 L Finger4";
        if (name.equals("l_pinkie_1_meta")) name = "Bip01 L Finger4";
        if (name.equals("l_pinkie_1_2")) name = "Bip01 L Finger4";
        if (name.equals("l_pinkie_1_meta")) name = "Bip01 L Finger4";

        if (name.equals("r_pinkie_1_1")) name = "Bip01 R Finger4";
        if (name.equals("r_pinkie_1_meta")) name = "Bip01 R Finger4";
        if (name.equals("r_pinkie_1_2")) name = "Bip01 R Finger4";
        if (name.equals("r_pinkie_1_meta")) name = "Bip01 R Finger4";

        if (name.equals("l_ring_1_1")) name = "Bip01 L Finger3";
        if (name.equals("l_ring_1_meta")) name = "Bip01 L Finger3";
        if (name.equals("l_ring_1_2")) name = "Bip01 L Finger3";
        if (name.equals("l_ring_1_meta")) name = "Bip01 L Finger3";

        if (name.equals("r_ring_1_1")) name = "Bip01 R Finger3";
        if (name.equals("r_ring_1_meta")) name = "Bip01 R Finger3";
        if (name.equals("r_ring_1_2")) name = "Bip01 R Finger3";
        if (name.equals("r_ring_1_meta")) name = "Bip01 R Finger3";

        if (name.equals("l_middle_1_1")) name = "Bip01 L Finger2";
        if (name.equals("l_middle_1_meta")) name = "Bip01 L Finger2";
        if (name.equals("l_middle_1_2")) name = "Bip01 L Finger2";
        if (name.equals("l_middle_1_meta")) name = "Bip01 L Finger2";

        if (name.equals("r_middle_1_1")) name = "Bip01 R Finger2";
        if (name.equals("r_middle_1_meta")) name = "Bip01 R Finger2";
        if (name.equals("r_middle_1_2")) name = "Bip01 R Finger2";
        if (name.equals("r_middle_1_meta")) name = "Bip01 R Finger2";

        if (name.equals("l_index_1_1")) name = "Bip01 L Finger1";
        if (name.equals("l_index_1_meta")) name = "Bip01 L Finger1";
        if (name.equals("l_index_1_2")) name = "Bip01 L Finger1";
        if (name.equals("l_index_1_meta")) name = "Bip01 L Finger1";

        if (name.equals("r_index_1_1")) name = "Bip01 R Finger1";
        if (name.equals("r_index_1_meta")) name = "Bip01 R Finger1";
        if (name.equals("r_index_1_2")) name = "Bip01 R Finger1";
        if (name.equals("r_index_1_meta")) name = "Bip01 R Finger1";

        if (name.equals("l_thumb_1_1")) name = "Bip01 L Finger0";
        if (name.equals("l_thumb_1_meta")) name = "Bip01 L Finger0";
        if (name.equals("l_thumb_1_2")) name = "Bip01 L Finger0";
        if (name.equals("l_thumb_1_meta")) name = "Bip01 L Finger0";

        if (name.equals("r_thumb_1_1")) name = "Bip01 R Finger0";
        if (name.equals("r_thumb_1_meta")) name = "Bip01 R Finger0";
        if (name.equals("r_thumb_1_2")) name = "Bip01 R Finger0";
        if (name.equals("r_thumb_1_meta")) name = "Bip01 R Finger0";

        if (name.equals("l_ear_1")) name = "Bip01 Head";
        if (name.equals("l_ear_2")) name = "Bip01 Head";
        if (name.equals("l_ear_3")) name = "Bip01 Head";

        if (name.equals("r_ear_1")) name = "Bip01 Head";
        if (name.equals("r_ear_2")) name = "Bip01 Head";
        if (name.equals("r_ear_3")) name = "Bip01 Head";

        if (name.equals(original.toLowerCase())) return original;
        return name;
    }
    
    private static int getBoneIndex(String name, BonePalette palette) {
        name = mapSabaBone(name);
        bones.add(name);
        return palette.addBone(name);
    }

    public static MeshChunkerResult getMeshChunkData(String meshName, String glbSourcePath) {
        bones = new HashSet<>();
        meshName = meshName.toUpperCase();

        MeshChunkerResult result = new MeshChunkerResult();

        BonePalette skeleton = MeshChunker.createSackboySkeleton();

        ArrayList<Chunk> chunks = new ArrayList<>();

        GltfModelV2 gltf;
        try { gltf = (GltfModelV2) new GltfModelReader().read(Path.of(glbSourcePath).toUri()); }
        catch (IOException ex) { return null; }

        // Right now we're only supporting single model meshes.
        MeshModel glMesh = null;
        SkinModel glSkin = null;
        for (NodeModel node : gltf.getNodeModels()) {
            if (node.getMeshModels().size() != 0) {
                glMesh = node.getMeshModels().get(0);
                glSkin = node.getSkinModel();
                break;
            }
        }

        if (glMesh == null) return null;

        int totalVertCount = glMesh.getMeshPrimitiveModels()
                    .stream()
                    .mapToInt(model -> model.getAttributes().get("POSITION").getCount())
                    .reduce(0, (total, current) -> total + current);

        int totalIndexCount = glMesh.getMeshPrimitiveModels()
                    .stream()
                    .mapToInt(model -> model.getIndices().getCount())
                    .reduce(0, (total, current) -> total + current);

        Vector4f[] positions = new Vector4f[totalVertCount];
        Vector4f[] normals = new Vector4f[totalVertCount];
        Vector4f[] uvs = new Vector4f[totalVertCount];
        Vector4f[] tangents = new Vector4f[totalVertCount];
        Vector4f[] colors = new Vector4f[totalVertCount];

        Vector4f[] blendIndices = new Vector4f[totalVertCount];
        Vector4f[] blendWeights = new Vector4f[totalVertCount];

        int baseVert = 0;
        int baseIndex = 0;
        int primitiveCount = glMesh.getMeshPrimitiveModels().size();

        int[] primitiveStarts = new int[primitiveCount];
        int[] primitiveCounts = new int[primitiveCount];
        int[] materialUIDs = new int[primitiveCount];
        
        Vector3f min = new Vector3f(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY);
        Vector3f max = new Vector3f(Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY);

        VertexStreamDescriptor decl = VertexStreams.get("VertexDecl.SkinnedCol");
        Buffer[] buffers = decl.create(meshName + "_A", totalVertCount);

        Buffer indexBuffer = new Buffer();
        indexBuffer.name = meshName + "_A.IndexBuffer";
        indexBuffer.UID = UFGCRC.qStringHash32(indexBuffer.name);
        indexBuffer.elementByteSize = 2;
        indexBuffer.numElements = totalIndexCount;
        indexBuffer.type = BufferType.INDEX;
        indexBuffer.data = new byte[indexBuffer.numElements * 0x2];

        for (int p = 0; p < primitiveCount; ++p) {
            MeshPrimitiveModel primitive = glMesh.getMeshPrimitiveModels().get(p);

            MaterialModel glMaterial = primitive.getMaterialModel();
            Map<String, AccessorModel> attributes = primitive.getAttributes();

            int vertexCount = attributes.get("POSITION").getCount();
    
            FloatBuffer positionBuffer = attributes.get("POSITION").getAccessorData().createByteBuffer().asFloatBuffer();
            FloatBuffer normalBuffer = attributes.get("NORMAL").getAccessorData().createByteBuffer().asFloatBuffer();
            FloatBuffer uvBuffer = attributes.get("TEXCOORD_0").getAccessorData().createByteBuffer().asFloatBuffer();
            FloatBuffer tangentBuffer = attributes.get("TANGENT").getAccessorData().createByteBuffer().asFloatBuffer();
            FloatBuffer weightBuffer = attributes.get("WEIGHTS_0").getAccessorData().createByteBuffer().asFloatBuffer();
            ByteBuffer blendIndexBuffer = attributes.get("JOINTS_0").getAccessorData().createByteBuffer();
    
    
            for (int i = baseVert; i < baseVert + vertexCount; ++i) {
    
                blendIndices[i] = new Vector4f(
                    getBoneIndex(glSkin.getJoints().get(blendIndexBuffer.get()).getName(), skeleton),
                    getBoneIndex(glSkin.getJoints().get(blendIndexBuffer.get()).getName(), skeleton),
                    getBoneIndex(glSkin.getJoints().get(blendIndexBuffer.get()).getName(), skeleton),
                    getBoneIndex(glSkin.getJoints().get(blendIndexBuffer.get()).getName(), skeleton)
                );
    
                blendWeights[i] = new Vector4f(
                    weightBuffer.get(),
                    weightBuffer.get(),
                    weightBuffer.get(),
                    weightBuffer.get()
                );
    
                colors[i] = new Vector4f(1.0f, 1.0f, 1.0f, 1.0f);
    
                float x = positionBuffer.get();
                float y = positionBuffer.get();
                float z = positionBuffer.get();
    
                if (x >= max.x) max.x = x;
                if (x <= min.x) min.x = x;
    
                if (y >= max.y) max.y = y;
                if (y <= min.y) min.y = y;
    
                if (z >= max.z) max.z = z;
                if (z <= min.z) min.z = z;
    
    
                positions[i] = new Vector4f(x, y, z, 0.0f);
    
    
                normals[i] = new Vector4f(
                    normalBuffer.get(),
                    normalBuffer.get(),
                    normalBuffer.get(),
                    0.0f
                );
    
                uvs[i] = new Vector4f(
                    uvBuffer.get(),
                    uvBuffer.get(),
                    0.0f,
                    0.0f
                );
    
                tangents[i] = new Vector4f(
                    tangentBuffer.get(),
                    tangentBuffer.get(),
                    tangentBuffer.get(),
                    -0.00787401574f
                );
            }

            ShortBuffer indexStream = primitive.getIndices().getAccessorData().createByteBuffer().asShortBuffer();
            int indexBufferSize = primitive.getIndices().getCount() * 0x2;

            primitiveStarts[p] = baseIndex / 0x2;
            for (int i = 0; i < indexBufferSize; i += 2) {
                short s = (short) (baseVert + (int) (indexStream.get() & 0xffff));
                indexBuffer.data[baseIndex + i + 0] = (byte) (s >>> 8);
                indexBuffer.data[baseIndex + i + 1] = (byte) (s & 0xff);
            }
            primitiveCounts[p] = indexBufferSize / 0x2 / 0x3;

            baseIndex += indexBufferSize;
            baseVert += vertexCount;

            Material material = Material.createVinylShader(
                meshName + "_A." + glMaterial.getName().toLowerCase(),
                meshName + "_D.TGA",
                meshName + "_N.TGA",
                meshName + "_S.TGA"
            );

            int normalTextureIndex = (int) glMaterial.getValues().get("normalTexture");
            int diffuseTextureIndex = (int) glMaterial.getValues().get("baseColorTexture");
            int specularTextureIndex = (int) glMaterial.getValues().get("metallicRoughnessTexture");

            TextureChunkerResult normalTexture = TextureChunker.convertTextureData(meshName + "_N.TGA", gltf.getTextureModels().get(normalTextureIndex).getImageModel().getImageData());
            TextureChunkerResult diffuseTexture = TextureChunker.convertTextureData(meshName + "_D.TGA", gltf.getTextureModels().get(diffuseTextureIndex).getImageModel().getImageData());
            TextureChunkerResult specularTexture = TextureChunker.convertTextureData(meshName + "_S.TGA", gltf.getTextureModels().get(specularTextureIndex).getImageModel().getImageData());


            result.texturePackData = new byte[][] { diffuseTexture.data, normalTexture.data, specularTexture.data };
            result.textureStreamingChunks = new Texture[] { diffuseTexture.texture, normalTexture.texture, specularTexture.texture };

            Serializer serializer = new Serializer(material.getAllocatedSize());
            serializer.struct(material, Material.class);
            chunks.add(new Chunk(ResourceType.MATERIAL.getValue(), serializer.getBuffer()));

            materialUIDs[p] = material.UID;
        }

        decl.set(buffers, VertexStreamElementUsage.POSITION, positions);
        decl.set(buffers, VertexStreamElementUsage.NORMAL, normals);
        decl.set(buffers, VertexStreamElementUsage.TEXCOORD0, uvs);
        decl.set(buffers, VertexStreamElementUsage.TANGENT, tangents);
        decl.set(buffers, VertexStreamElementUsage.COLOR0, colors);

        decl.set(buffers, VertexStreamElementUsage.BLENDINDEX, blendIndices);
        decl.set(buffers, VertexStreamElementUsage.BLENDWEIGHT, blendWeights);

        

        for (int i = 0; i < 3; ++i) {
            Serializer serializer = new Serializer(buffers[i].getAllocatedSize());
            serializer.struct(buffers[i], Buffer.class);
            chunks.add(new Chunk(ResourceType.BUFFER.getValue(), serializer.getBuffer()));
        }

        {
            Serializer serializer = new Serializer(indexBuffer.getAllocatedSize());
            serializer.struct(indexBuffer, Buffer.class);
            chunks.add(new Chunk(ResourceType.BUFFER.getValue(), serializer.getBuffer()));
        }

        {
            skeleton.name = meshName + "_A.BonePalette";
            skeleton.UID = UFGCRC.qStringHash32(skeleton.name);
            Serializer serializer = new Serializer(skeleton.getAllocatedSize());
            serializer.struct(skeleton, BonePalette.class);
            chunks.add(new Chunk(ResourceType.BONE_PALETTE.getValue(), serializer.getBuffer()));
        }

        {
            Model model = new Model();

            model.minAABB = min;
            model.maxAABB = max;
            model.bonePaletteUID = skeleton.UID;

            Mesh[] meshes = new Mesh[primitiveCount];
            for (int i = 0; i < meshes.length; ++i) {
                Mesh mesh = new Mesh();

                mesh.materialUID = materialUIDs[i];
                mesh.indexBufferUID = indexBuffer.UID;
                mesh.indexStart = primitiveStarts[i];
                mesh.numPrimitives = primitiveCounts[i];
                mesh.vertexDeclUID = decl.getNameUID();
                mesh.vertexBufferUIDs[0] = buffers[0].UID;
                mesh.vertexBufferUIDs[1] = buffers[1].UID;
                mesh.vertexBufferUIDs[2] = buffers[2].UID;
    
                mesh.primitiveType = PrimitiveType.TRIANGLE_LIST;

                meshes[i] = mesh;
            }
            
            model.meshes = meshes;

            model.name = meshName + "_A";
            model.UID = UFGCRC.qStringHash32(model.name);
            model.numPrims = totalIndexCount / 0x3;
            
            Serializer serializer = new Serializer(model.getAllocatedSize());
            serializer.struct(model, Model.class);
            chunks.add(new Chunk(ResourceType.MODEL_DATA.getValue(), serializer.getBuffer()));
        }

        result.modelStreamingChunks = chunks.toArray(Chunk[]::new);

        System.out.println(bones);

        return result;
    }
}
