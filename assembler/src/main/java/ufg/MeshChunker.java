package ufg;


import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;

import de.javagl.jgltf.model.AccessorModel;
import de.javagl.jgltf.model.MaterialModel;
import de.javagl.jgltf.model.MeshModel;
import de.javagl.jgltf.model.MeshPrimitiveModel;
import de.javagl.jgltf.model.NodeModel;
import de.javagl.jgltf.model.SkinModel;
import de.javagl.jgltf.model.io.GltfModelReader;
import de.javagl.jgltf.model.v2.GltfModelV2;
import ufg.TextureChunker.TextureChunkerResult;
import ufg.enums.BufferType;
import ufg.enums.PrimitiveType;
import ufg.enums.ResourceType;
import ufg.enums.VertexStreamElementUsage;
import ufg.io.Serializer;
import ufg.resources.BonePalette;
import ufg.resources.Buffer;
import ufg.resources.Locators;
import ufg.resources.Material;
import ufg.resources.Model;
import ufg.resources.Texture;
import ufg.structures.chunks.Chunk;
import ufg.structures.vertex.Mesh;
import ufg.structures.vertex.VertexStreamDescriptor;
import ufg.structures.vertex.VertexStreams;
import ufg.util.UFGCRC;

public class MeshChunker {
    public static class MeshChunkerResult {
        public Chunk[] modelStreamingChunks;
        public Chunk[] materialStreamingChunks;
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

    private static BonePalette createKartSkeleton() {
        BonePalette palette = new BonePalette();
        palette.name = "KART.BonePalette";
        palette.UID = UFGCRC.qStringHashUpper32(palette.name);
        // String[] bones = new String[] {
        //     "MARKER_ROOT", "MARKER_R_F_ROOT", "MARKER_R_B_ROOT", "MARKER_R_B_BONE", "MARKER_L_B_BONE", "MARKER_L_B_ROOT",
        //     "MARKER_L_F_ROOT", "MARKER_L_F_BONE", "MARKER_R_F_BONE"
        // };
        String[] bones = new String[] {
            "MARKER_ROOT", "MARKER_R_F_BONE", "MARKER_R_F_ROOT", "MARKER_L_F_BONE", "MARKER_L_F_ROOT", "MARKER_R_B_BONE",
            "MARKER_R_B_ROOT", "MARKER_L_B_BONE", "MARKER_L_B_ROOT"
        };

        for (String bone : bones)
            palette.addBone(bone);
        return palette;
    }

    private static String mapSabaBone(String name) {
        String original = name;
        name = name.toLowerCase();

        // if (name.contains("tongue03")) name = "bone_tongue02";
        // if (name.contains("tongue04")) name = "bone_tongue02";

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
        if (name.equals("l_knee_1_scale_01")) name = "Bip01 L Calf";

        if (name.equals("r_knee_1_roll_01")) name = "Bip01 R Calf";
        if (name.equals("r_knee_1_scale_01")) name = "Bip01 R Calf";

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
        if (name.equals("f_zipper_1_0")) name = "bone_zip_pull01";

        if (name.equals(original.toLowerCase())) {
            // System.out.println("Unmapped bone! " + original);
            return original;
        }
        return name;
    }
    
    private static int getBoneIndex(String name, BonePalette palette) {
        name = mapSabaBone(name);
        bones.add(name);
        return palette.addBone(name);
    }

    public static enum ImportType {
        Costume,
        KartBody,
        KartSuspension,
        KartEffect,
        KartPart
    }

    public static class MeshImportData {
        public String name;
        public String source;
        public ImportType type = ImportType.Costume;
        public boolean invisible = false;
        public String suffix;

        public MeshImportData(String name, String source, ImportType type, String suffix) {
            this.name = name;
            this.source = source;
            this.type = type;
            this.suffix = "_" + suffix;
        }
    }


    public static MeshChunkerResult getMeshChunkData(MeshImportData config) {
        bones = new HashSet<>();
        String meshName = config.name.toUpperCase();

        MeshChunkerResult result = new MeshChunkerResult();

        BonePalette skeleton;
        if (config.type == ImportType.Costume)
            skeleton = MeshChunker.createSackboySkeleton();
        else
            skeleton = MeshChunker.createKartSkeleton();

        ArrayList<Chunk> chunks = new ArrayList<>();
        ArrayList<Chunk> materialChunks = new ArrayList<>();

        // String suffix = config.type == ImportType.KartEffect ? "" : "_A";
        String suffix = config.suffix;

        GltfModelV2 gltf;
        try { gltf = (GltfModelV2) new GltfModelReader().read(Path.of(config.source).toUri()); }
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
        boolean isSkinned = glSkin != null;

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

        ArrayList<byte[]> texturePackData = new ArrayList<>();
        ArrayList<Texture> textureStreamingChunks = new ArrayList<>();

        Vector3f min = new Vector3f(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY);
        Vector3f max = new Vector3f(Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY);

        String vertexStreamType = "VertexDecl.KartWorld";
        if (config.type == ImportType.KartBody || config.type == ImportType.KartPart) vertexStreamType = "VertexDecl.KartWorld1UV";
        else if (isSkinned) vertexStreamType = "VertexDecl.SkinnedCol";


        boolean overrideSkin = false;
        if (config.type == ImportType.KartSuspension && !isSkinned) {
            vertexStreamType = "VertexDecl.SkinnedCol";
            overrideSkin = true;
            isSkinned = true;
        }

        VertexStreamDescriptor decl = VertexStreams.get(vertexStreamType);
        Buffer[] buffers = decl.create(meshName + suffix, totalVertCount);

        Buffer indexBuffer = new Buffer();
        indexBuffer.name = meshName + suffix + ".IndexBuffer";
        indexBuffer.UID = UFGCRC.qStringHashUpper32(indexBuffer.name);
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

            FloatBuffer weightBuffer = null;
            ByteBuffer blendIndexBuffer = null;
            if (isSkinned && !overrideSkin) {
                weightBuffer = attributes.get("WEIGHTS_0").getAccessorData().createByteBuffer().asFloatBuffer();
                blendIndexBuffer = attributes.get("JOINTS_0").getAccessorData().createByteBuffer();
            }
    
    
            for (int i = baseVert; i < baseVert + vertexCount; ++i) {
    
                if (isSkinned && !overrideSkin) {
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
                } else if (overrideSkin) {
                    blendIndices[i] = new Vector4f(0, 0, 0, 0);
                    blendWeights[i] = new Vector4f(1.0f, 0.0f, 0.0f, 0.0f);
                }

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

            boolean hasDiffuse = glMaterial.getValues().get("baseColorTexture") != null;
            boolean hasNormals = glMaterial.getValues().get("normalTexture") != null;
            boolean hasSpecular = glMaterial.getValues().get("metallicRoughnessTexture") != null;

            Material material;
            String materialName = String.format("%s%s_%d.%s", meshName, suffix, p, glMaterial.getName().toLowerCase());
            if (!config.invisible) 
            {
                String diffuse = hasDiffuse ? String.format("%s_%d_D", meshName, p) : null;
                String normal = hasNormals ? String.format("%s_%d_N", meshName, p) : null;
                String specular = hasSpecular ? String.format("%s_%d_s", meshName, p): null;

                if (config.type == ImportType.Costume) material = Material.createVinylShader(materialName, diffuse, normal, specular);
                else if (config.type == ImportType.KartPart) material = Material.createKartPartShader(materialName, diffuse, normal, specular);
                else if (isSkinned) material = Material.createSkinnedKartShader(materialName, diffuse, normal, specular);
                else material = Material.createKartShader(materialName, diffuse, normal, specular);

            } else material = Material.createSimpleShader(materialName);


            if (hasDiffuse) {
                int diffuseTextureIndex = (int) glMaterial.getValues().get("baseColorTexture");

                {
                    TextureChunkerResult diffuseTexture = TextureChunker.convertTextureData(String.format("%s_%d_D", meshName, p), gltf.getTextureModels().get(diffuseTextureIndex).getImageModel().getImageData(), "256");
                    texturePackData.add(diffuseTexture.data);
                    textureStreamingChunks.add(diffuseTexture.texture);
                }
            }

            if (hasNormals) {
                int normalTextureIndex = (int) glMaterial.getValues().get("normalTexture");
                TextureChunkerResult normalTexture = TextureChunker.convertTextureData(String.format("%s_%d_N", meshName, p), gltf.getTextureModels().get(normalTextureIndex).getImageModel().getImageData(), "128");

                texturePackData.add(normalTexture.data);
                textureStreamingChunks.add(normalTexture.texture);
            }

            if (hasSpecular) {
                int specularTextureIndex = (int) glMaterial.getValues().get("metallicRoughnessTexture");
                TextureChunkerResult specularTexture = TextureChunker.convertTextureData(String.format("%s_%d_S", meshName, p), gltf.getTextureModels().get(specularTextureIndex).getImageModel().getImageData(), "128");

                texturePackData.add(specularTexture.data);
                textureStreamingChunks.add(specularTexture.texture);
            }

            Serializer serializer = new Serializer(material.getAllocatedSize());
            serializer.struct(material, Material.class);
            materialChunks.add(new Chunk(ResourceType.MATERIAL.getValue(), serializer.getBuffer()));

            materialUIDs[p] = material.UID;
        }

        decl.set(buffers, VertexStreamElementUsage.POSITION, positions);
        decl.set(buffers, VertexStreamElementUsage.NORMAL, normals);
        decl.set(buffers, VertexStreamElementUsage.TEXCOORD0, uvs);
        decl.set(buffers, VertexStreamElementUsage.TANGENT, tangents);
        decl.set(buffers, VertexStreamElementUsage.COLOR0, colors);

        if (isSkinned) {
            decl.set(buffers, VertexStreamElementUsage.BLENDINDEX, blendIndices);
            decl.set(buffers, VertexStreamElementUsage.BLENDWEIGHT, blendWeights);
        }

        if (isSkinned) {
            skeleton.name = meshName + suffix + ".BonePalette";
            skeleton.UID = UFGCRC.qStringHashUpper32(skeleton.name);
            Serializer serializer = new Serializer(skeleton.getAllocatedSize());
            serializer.struct(skeleton, BonePalette.class);
            chunks.add(new Chunk(ResourceType.BONE_PALETTE.getValue(), serializer.getBuffer()));
        }

        {
            Serializer serializer = new Serializer(indexBuffer.getAllocatedSize());
            serializer.struct(indexBuffer, Buffer.class);
            chunks.add(new Chunk(ResourceType.BUFFER.getValue(), serializer.getBuffer()));
        }

        for (int i = 0; i < decl.getMaxStreams(); ++i) {
            Serializer serializer = new Serializer(buffers[i].getAllocatedSize());
            serializer.struct(buffers[i], Buffer.class);
            chunks.add(new Chunk(ResourceType.BUFFER.getValue(), serializer.getBuffer()));
        }

        {
            Model model = new Model();

            HashMap<String, Matrix4f> locators = new HashMap<>();
            for (NodeModel node : gltf.getNodeModels()) {
                if (node.getName().toUpperCase().startsWith("MARKER")) {
                    Matrix4f locMatrix = new Matrix4f();
                    float[] nodeMatrix = node.getMatrix();
                    if (nodeMatrix != null) locMatrix.set(nodeMatrix);
                    else {

                        Vector3f translation = new Vector3f();
                        if (node.getTranslation() != null)
                            translation.set(node.getTranslation());
                        Vector4f rotation = new Vector4f(0.0f, 0.0f, 0.0f, 1.0f);
                        if (node.getRotation() != null)
                            rotation.set(node.getRotation());
                        Vector3f scale = new Vector3f(1.0f, 1.0f, 1.0f);
                        if (node.getScale() != null)
                            scale.set(node.getScale());

                        locMatrix.identity()
                            .translationRotateScale(
                                translation, 
                                new Quaternionf(rotation.x, rotation.y, rotation.z, rotation.w), 
                                scale
                            );
                    }

                    locators.put(node.getName().split("[.]")[0], locMatrix);
                }
            }

            if (locators.size() != 0) {
                Locators loc = new Locators();
                loc.locators = locators;
                loc.name = meshName + suffix + ".Locators";
                loc.UID = UFGCRC.qStringHashUpper32(loc.name);
                Serializer serializer = new Serializer(loc.getAllocatedSize());
                serializer.struct(loc, Locators.class);
                chunks.add(new Chunk(ResourceType.LOCATORS.getValue(), serializer.getBuffer()));
                model.locatorsUID = loc.UID;
            }

            model.minAABB = min;
            model.maxAABB = max;
            if (isSkinned)
                model.bonePaletteUID = skeleton.UID;

            Mesh[] meshes = new Mesh[primitiveCount];
            for (int i = 0; i < meshes.length; ++i) {
                Mesh mesh = new Mesh();

                mesh.materialUID = materialUIDs[i];
                mesh.indexBufferUID = indexBuffer.UID;
                mesh.indexStart = primitiveStarts[i];
                mesh.numPrimitives = primitiveCounts[i];
                mesh.vertexDeclUID = decl.getNameUID();
                for (int j = 0; j < decl.getMaxStreams(); ++j)
                    mesh.vertexBufferUIDs[j] = buffers[j].UID;
    
                mesh.primitiveType = PrimitiveType.TRIANGLE_LIST;

                meshes[i] = mesh;
            }
            
            model.meshes = meshes;

            model.name = meshName + suffix;
            model.UID = UFGCRC.qStringHashUpper32(model.name);
            model.numPrims = totalIndexCount / 0x3;
            
            Serializer serializer = new Serializer(model.getAllocatedSize());
            serializer.struct(model, Model.class);
            chunks.add(new Chunk(ResourceType.MODEL_DATA.getValue(), serializer.getBuffer()));
        }

        result.modelStreamingChunks = chunks.toArray(Chunk[]::new);
        result.texturePackData = texturePackData.toArray(byte[][]::new);
        result.textureStreamingChunks = textureStreamingChunks.toArray(Texture[]::new);
        result.materialStreamingChunks = materialChunks.toArray(Chunk[]::new);

        return result;
    }
}
