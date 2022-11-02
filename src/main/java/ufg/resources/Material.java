package ufg.resources;

import java.util.ArrayList;

import ufg.io.Serializable;
import ufg.io.Serializer;
import ufg.structures.chunks.ResourceData;
import ufg.utilities.UFGCRC;

public class Material extends ResourceData {
    public static final int BASE_ALLOCATION_SIZE = 
        ResourceData.BASE_ALLOCATION_SIZE + 0x30;

    public static class MaterialParameter implements Serializable {
        public static final int BASE_ALLOCATION_SIZE = 0x20;

        public int paramName, paramType;
        public int valueUID, valueType;

        public MaterialParameter() {};

        public MaterialParameter(String name, int paramType, String value, int valueType) {
            this.paramName = UFGCRC.qStringHash32(name);
            this.paramType = paramType;

            this.valueUID = UFGCRC.qStringHash32(value);
            this.valueType = valueType;
        }

        public MaterialParameter(int name, int paramType, int value, int valueType) {
            this.paramName = name;
            this.paramType = paramType;
            this.valueUID = value;
            this.valueType = valueType;
        }
        
        @SuppressWarnings("unchecked")
        @Override public MaterialParameter serialize(Serializer serializer, Serializable structure) {
            MaterialParameter param = structure == null ? new MaterialParameter() : (MaterialParameter) structure;

            param.paramName = serializer.i32(param.paramName);
            param.paramType = serializer.i32(param.paramType);

            serializer.pad(0x10);

            param.valueUID = serializer.i32(param.valueUID);
            param.valueType = serializer.i32(param.valueType);

            return param;
        }

        @Override public int getAllocatedSize() { return MaterialParameter.BASE_ALLOCATION_SIZE; }
    }

    public ArrayList<MaterialParameter> parameters = new ArrayList<>();

    public Material() { this.typeUID = UFGCRC.qStringHash32("Illusion.Material"); }

    public static Material createVinylShader(String name, String diffuse, String normal, String specular) {
        Material material = new Material();

        material.name = name;
        material.UID = UFGCRC.qStringHashUpper32(name);

        material.addResourceProperty("iShader", "iShader", "Illusion.Shader:Vynl");
        material.addProperty("iAlphaState", "Illusion.AlphaState", null);

        material.addStateBlock("sbVinylLook", 0xB38E68B4); // StateBlock.Cloth_VinylLook
        material.addStateBlock("sbColourTint", 0xDD4A8F1C); // StateBlock.DefaultWhiteTint_ColourTint

        material.addProperty("iRasterState", "Illusion.RasterState", "DoubleSided");

        material.addResourceProperty("texDiffuse", "iTexture", "Illusion.Texture:" + ((diffuse == null) ? "DefaultWhite" : diffuse));
        material.addResourceProperty("texSpecular", "iTexture", "Illusion.Texture:" + ((specular == null) ? "DefaultSpecular" : specular));
        material.addResourceProperty("texNormal", "iTexture", "Illusion.Texture:" + ((normal == null) ? "DefaultNormalMap" : normal));

        material.addResourceProperty("texReflection", "iTexture", "Illusion.Texture:GEN_Windowreflect01_D");
        material.addResourceProperty("texBlendMask", "iTexture", "Illusion.Texture:DefaultBlackAlphaBlack");

        return material;
    }

    public MaterialParameter addProperty(String name, String type, String value) {
        MaterialParameter parameter = new MaterialParameter();
        parameter.paramName = UFGCRC.qStringHash32(name);
        parameter.paramType = UFGCRC.qStringHash32(name);


        parameter.valueType = UFGCRC.qStringHash32(type);
        if (value == null) parameter.valueUID = -1;
        else parameter.valueUID = UFGCRC.qStringHash32(type + "." + value);

        this.parameters.add(parameter);

        return parameter;
    }

    public MaterialParameter addStateBlock(String name, int value) {
        MaterialParameter parameter = new MaterialParameter();
        parameter.paramName = UFGCRC.qStringHash32(name);
        parameter.paramType = UFGCRC.qStringHash32(name);


        parameter.valueType = UFGCRC.qStringHash32("Illusion.StateBlock");
        parameter.valueUID = value;

        this.parameters.add(parameter);

        return parameter;
    }

    public MaterialParameter addResourceProperty(String name, String type, String value) {
        MaterialParameter parameter = new MaterialParameter();
        parameter.paramName = UFGCRC.qStringHash32(name);
        parameter.paramType = UFGCRC.qStringHash32(type);

        String[] handle = value.split(":");
        parameter.valueType = UFGCRC.qStringHash32(handle[0]);
        parameter.valueUID = UFGCRC.qStringHashUpper32(handle[1]);

        this.parameters.add(parameter);

        return parameter;
    }

    @SuppressWarnings("unchecked")
    @Override public Material serialize(Serializer serializer, Serializable structure) {
        Material material = (structure == null) ? new Material() : (Material) structure;

        super.serialize(serializer, material);

        serializer.pad(0x10); // mStateBlockMask, BitFlags128

        int numParams = serializer.i32(material.parameters != null ? material.parameters.size() : 0);
        int materialUserOffset = serializer.getOffset();
        materialUserOffset += serializer.i32(0xC + (numParams * 0x20));
        serializer.pad(0x8);

        if (!serializer.isWriting())
            material.parameters = new ArrayList<>(numParams);

        if (serializer.isWriting()) {
            for (MaterialParameter parameter : material.parameters)
                serializer.struct(parameter, MaterialParameter.class);
        } else for (int i = 0; i < numParams; ++i)
            material.parameters.add(serializer.struct(null, MaterialParameter.class));
        
        serializer.seek(materialUserOffset + 0x2);
        serializer.i32(0x001742b2);
        serializer.pad(0xA);

        return material;
    }

    @Override public int getAllocatedSize() {
        int size = Material.BASE_ALLOCATION_SIZE;
        return size + (this.parameters.size() * 0x20);
    }
}
