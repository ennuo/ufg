package executables.assembler;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;

import executables.assembler.MeshChunker.MeshChunkerResult;
import executables.assembler.TextureChunker.TextureChunkerResult;
import ufg.io.streams.MemoryOutputStream;
import ufg.io.streams.MemoryInputStream.SeekMode;
import ufg.util.Bytes;
import ufg.util.FileIO;
import ufg.enums.PartAttrib;
import ufg.enums.ResourceType;
import ufg.resources.ChunkFileIndex;
import ufg.resources.PartDB;
import ufg.resources.Texture;
import ufg.resources.PartDB.Part;
import ufg.resources.PartDB.PartValue;
import ufg.resources.PartDB.SlotDB;
import ufg.structures.SHA1;
import ufg.structures.XmlDb;
import ufg.structures.XmlDb.XmlDbNode;
import ufg.structures.chunks.Chunk;
import ufg.structures.chunks.ChunkFileIndexEntry;
import ufg.utilities.UFGCRC;

public class Assembler {
    private static class Package {
        private String name;
        private String slot;

        private Chunk[] modelStreamingChunks;
        private ArrayList<Texture> textureStreamingChunks = new ArrayList<>();
        private ArrayList<byte[]> texturePackData = new ArrayList<>();

    }

    private final PartDB partDatabase;
    private final SlotDB slotDatabase;
    private final XmlDb xmlDatabase;

    private final ArrayList<Package> packages = new ArrayList<>();

    public Assembler(PartDB partDB, SlotDB slotDB, XmlDb xmlDB) {
        this.partDatabase = partDB;
        this.slotDatabase = slotDB;
        this.xmlDatabase = xmlDB;
    }

    public void addCostumePackage(String packageName, String slot, int category, String[] selections, String iconSourcePath, String glbSourcePath) {
        Package pkg = new Package();

        pkg.name = packageName;
        pkg.slot = slot;

        MeshChunkerResult result =  MeshChunker.getMeshChunkData(packageName, glbSourcePath);

        pkg.modelStreamingChunks = result.modelStreamingChunks;
        pkg.textureStreamingChunks.addAll(Arrays.asList(result.textureStreamingChunks));
        pkg.texturePackData.addAll(Arrays.asList(result.texturePackData));

        TextureChunkerResult icon = TextureChunker.convertTextureData(packageName.toUpperCase() + "_ICON", ByteBuffer.wrap(FileIO.read(iconSourcePath)));
        pkg.textureStreamingChunks.add(icon.texture);
        pkg.texturePackData.add(icon.data);

        // Add to part tables

        Part packagePart = new Part();
        packagePart.name = packageName;
        packagePart.set("UICategory", new PartValue(PartAttrib.UID, category));
        if (packagePart.name.contains("Shirt"))
            packagePart.set(-1279932325, new PartValue(PartAttrib.UID, -1));
        if (selections != null)
            for (int i = 0; i < selections.length; ++i)
                packagePart.set("HiddenSelectionSet" + (i), new PartValue(PartAttrib.UID, UFGCRC.qStringHashUpper32(selections[i].toUpperCase())));
        this.partDatabase.parts.add(packagePart);

        // Add to associated slot

        int index = -1;
        for (int i = 0; i < this.slotDatabase.entries.length; ++i) {
            if (this.slotDatabase.entries[i].name.equals(slot)) {
                index = i;
                break;
            }
        }

        if (index == -1) throw new RuntimeException("Slot not found!");

        this.slotDatabase.entries[index].uids.add(UFGCRC.qStringHashUpper32(packageName));
        
        this.packages.add(pkg);
    }

    public void addSkinPackage(String packageName, String iconSourcePath, String diffuseIconPath, String normalIconPath, String specularIconPath) {
        Package pkg = new Package();

        pkg.name = packageName;
        pkg.slot = "Slot_Char_Pattern";


        pkg.modelStreamingChunks = new Chunk[0];

        TextureChunkerResult texture = TextureChunker.convertTextureData(packageName.toUpperCase() + "_D", ByteBuffer.wrap(FileIO.read(diffuseIconPath)));
        pkg.textureStreamingChunks.add(texture.texture);
        pkg.texturePackData.add(texture.data);

        texture = TextureChunker.convertTextureData(packageName.toUpperCase() + "_N", ByteBuffer.wrap(FileIO.read(normalIconPath)));
        pkg.textureStreamingChunks.add(texture.texture);
        pkg.texturePackData.add(texture.data);

        texture = TextureChunker.convertTextureData(packageName.toUpperCase() + "_S", ByteBuffer.wrap(FileIO.read(specularIconPath)));
        pkg.textureStreamingChunks.add(texture.texture);
        pkg.texturePackData.add(texture.data);

        texture = TextureChunker.convertTextureData(packageName.toUpperCase() + "_ICON", ByteBuffer.wrap(FileIO.read(iconSourcePath)));
        pkg.textureStreamingChunks.add(texture.texture);
        pkg.texturePackData.add(texture.data);

        // Add to part tables

        Part packagePart = new Part();
        packagePart.name = packageName;
        packagePart.set("UICategory", new PartValue(PartAttrib.UID, 1000219946));
        packagePart.set("StateBlock", new PartValue(PartAttrib.STRING, "Cloth"));
        this.partDatabase.parts.add(packagePart);

        // Add to associated slot

        int index = -1;
        for (int i = 0; i < this.slotDatabase.entries.length; ++i) {
            if (this.slotDatabase.entries[i].name.equals(pkg.slot)) {
                index = i;
                break;
            }
        }

        if (index == -1) throw new RuntimeException("Slot not found!");

        this.slotDatabase.entries[index].uids.add(UFGCRC.qStringHashUpper32(packageName));
        
        this.packages.add(pkg);
    }

    public void save(String name, String path) {
        File root = new File(path);
        
        ArrayList<Chunk> textureChunks = new ArrayList<>();

        ArrayList<SHA1> hashes = new ArrayList<>();
        ArrayList<Integer> offsets = new ArrayList<>();
        // ArrayList<Integer> uids = new ArrayList<>();

        ArrayList<byte[]> modelChunkFiles = new ArrayList<>();
        ChunkFileIndex modelPackIndex = new ChunkFileIndex();
        // ChunkFileIndex texturePackIndex = new ChunkFileIndex();

        int textureCount = this.packages.stream()
                                        .mapToInt(pkg -> pkg.textureStreamingChunks.size())
                                        .reduce(0, (total, current) -> total + current);

        MemoryOutputStream stream = new MemoryOutputStream((4_767_119 * textureCount) + (0x80 * textureCount * 2));
        stream.i32(0x5E73CDD7);

        int modelOffset = 0;

        for (Package pkg : this.packages) {

            if (pkg.modelStreamingChunks.length != 0) {
                byte[] chunkFile = Chunk.saveChunks(pkg.modelStreamingChunks);
                modelPackIndex.entries.add(new ChunkFileIndexEntry(UFGCRC.qStringHashUpper32(pkg.name + "_A"), modelOffset, chunkFile.length));
                modelOffset += chunkFile.length;
                modelChunkFiles.add(chunkFile);
            }

            for (int i = 0; i < pkg.texturePackData.size(); ++i) {
                Texture texture = pkg.textureStreamingChunks.get(i);
                byte[] imageData = pkg.texturePackData.get(i);

                // int textureDataUID = UFGCRC.qStringHash32(texture.name + ".TEXTUREDATA");

                SHA1 sha1 = SHA1.fromBuffer(imageData);

                int offset;
                int hash_index = hashes.indexOf(sha1);
                if (hash_index == -1) {
                    hashes.add(sha1);
                    if ((stream.getOffset()) % 0x80 != 0)
                        stream.pad((0x80 - ((stream.getOffset()) % 0x80)));
                    offset = stream.getOffset();
                    offsets.add(offset);
                    stream.bytes(imageData);
                    if ((stream.getOffset()) % 0x80 != 0)
                        stream.pad((0x80 - ((stream.getOffset()) % 0x80)));

                    // offset = 0x80;
                    // textureDataUID = UFGCRC.qStringHash32(texture.name + ".TextureData");

                    // offsets.add(offset);
                    // uids.add(textureDataUID);

                    // int textureDataStartOffset = stream.getOffset();

                    // stream.i32(0x5E73CDD7);
                    // int dataOffset = 0x0;
                    // if ((stream.getOffset() + 0xC) % 0x80 != 0)
                    //     dataOffset = (0x80 - ((stream.getOffset() + 0xC) % 0x80));

                    // int dataOffset = 0x70;

                    // int dataSize = imageData.length;
                    // dataSize += dataOffset;
                    // int endPadding = 0;
                    // if (dataSize % 0x80 != 0) {
                    //     endPadding = (0x80 - (dataSize % 0x80));
                    //     dataSize += endPadding;
                    // }

                    // texturePackIndex.entries.add(new ChunkFileIndexEntry(textureDataUID, textureDataStartOffset, dataSize + 0x10));

                    // stream.i32(dataSize);
                    // stream.i32(dataSize);
                    // stream.i32(dataOffset);
                    // stream.pad(dataOffset);
                    // stream.bytes(imageData);
                    // stream.pad(endPadding);

                } else {
                    // textureDataUID = uids.get(hash_index);
                    offset = offsets.get(hash_index);
                }
                
                texture.imageDataPosition = offset;
                texture.alphaStateSampler = UFGCRC.qStringHashUpper32(String.format("Illusion:Texture:%sTexturePack.TEMP.BIN", name));

                // Append texture to stream

                // texturePackIndex.entries.add(new ChunkFileIndexEntry(texture.UID, stream.getOffset(), 0x110));
                
                // stream.i32(ResourceType.TEXTURE_DESCRIPTOR.getValue());
                // stream.i32(0x100); stream.i32(0x100);
                // stream.i32(0);

                // Serializer serializer = new Serializer(stream, null);
                // serializer.struct(texture, Texture.class);

                textureChunks.add(Chunk.toChunk(ResourceType.TEXTURE_DESCRIPTOR.getValue(), texture));
            }
        }

        int end = stream.getOffset();
        stream.seek(0x4, SeekMode.Begin);
        stream.i32(end - 0x10);
        stream.i32(end - 0x10);
        stream.seek(end, SeekMode.Begin);

        stream.shrink();
        byte[] texturePack = stream.getBuffer();

        FileIO.write(texturePack, new File(root, String.format("MODS/%s/%sTEXTUREPACK.TEMP.BIN", name.toUpperCase(), name.toUpperCase())).getAbsolutePath());
        FileIO.write(Chunk.saveChunks(textureChunks.toArray(Chunk[]::new)), new File(root, String.format("MODS/%s/%sTEXTUREPACK.PERM.BIN", name.toUpperCase(), name.toUpperCase())).getAbsolutePath());
        
        // FileIO.write(texturePack, new File(root, String.format("MODS/%s/%sTEXTUREPACK.PERM.BIN", name.toUpperCase(), name.toUpperCase())).getAbsolutePath());

        // texturePackIndex.entries.sort((a, b) -> a.filenameUID - b.filenameUID);
        // texturePackIndex.name = name + "TexturePack.Perm";
        // texturePackIndex.UID = UFGCRC.qStringHash32(texturePackIndex.name.toUpperCase());

        // FileIO.write(
        //     Chunk.saveChunks(new Chunk[] { Chunk.toChunk(ResourceType.CHUNK_FILE_INDEX.getValue(), texturePackIndex) }), 
        //     new File(root, String.format("MODS/%s/%sTEXTUREPACK.PERM.IDX", name.toUpperCase(), name.toUpperCase())).getAbsolutePath()
        // );

        FileIO.write(
            Chunk.saveChunks(new Chunk[] { 
                Chunk.toChunk(0x8D43D0B4, this.partDatabase),
                Chunk.toChunk(0x01BC0C5C, this.slotDatabase)
            }), 
            new File(root, "PARTDB/PARTS.BIN").getAbsolutePath()
        );

        modelPackIndex.entries.sort((a, b) -> Long.compare(a.filenameUID & 0xffffffffl, b.filenameUID & 0xffffffffl));

        modelPackIndex.name = name + "ModelPackStreaming";
        modelPackIndex.UID = UFGCRC.qStringHashUpper32(modelPackIndex.name);

        if (modelPackIndex.entries.size() != 0) {
            FileIO.write(
                Chunk.saveChunks(new Chunk[] { Chunk.toChunk(ResourceType.CHUNK_FILE_INDEX.getValue(), modelPackIndex) }), 
                new File(root, "MODS/" + name.toUpperCase() + "/" + name.toUpperCase() + "MODELPACKSTREAMING.IDX").getAbsolutePath()
            );
    
            FileIO.write(
                Bytes.combine(modelChunkFiles.toArray(byte[][]::new)), 
                new File(root, "MODS/" + name.toUpperCase() + "/" + name.toUpperCase() + "MODELPACKSTREAMING.BIN").getAbsolutePath()
            );
        }

        String texturePath = "Mods\\" + name + "\\" + name + "TexturePack.perm.bin";
        // String texturePackPath = String.format("Data\\Mods\\%s\\TexturePacks.xml", name);
        XmlDbNode texturePackXML = this.xmlDatabase.getNode("Data\\Create\\RoboToy\\Textures\\ResLo\\TexturePacks.xml");
        if (texturePackXML.strings.indexOf("$(PatchDir)\\Data\\" + texturePath) == -1)
            texturePackXML.strings.add("$(PatchDir)\\Data\\" + texturePath);
        
        // FileIO.write(
        //     String.format("<?xml version=\"1.0\"?>\n<!-- Generated by pipeline.exe -->\n<TexturePack>\n\t<TexturePackStream>%s</TexturePackStream>\n</TexturePack>", texturePath).getBytes(),
        //     new File(root, String.format("MODS/%s/TEXTUREPACKS.XML", name.toUpperCase())).getAbsolutePath()
        // );

        if (modelPackIndex.entries.size() != 0) {
            String modelPath = "Mods\\" + name + "\\" + name + "ModelPackStreaming.idx";
            // String modelPackPath = String.format("Data\\Mods\\%s\\ModelPacks.xml", name);
            XmlDbNode modelPackXML = this.xmlDatabase.getNode("Data\\Create\\RoboToy\\ModelPacks.xml");

            if (modelPackXML.strings.indexOf("$(PatchDir)\\Data\\" + modelPath) == -1)
                modelPackXML.strings.add("$(PatchDir)\\Data\\" + modelPath);

            // FileIO.write(
            //     String.format("<?xml version=\"1.0\"?>\n<!-- Generated by pipeline.exe -->\n<ModelPack>\n\t<ModelPackStream>%s</ModelPackStream>\n</ModelPack>", modelPath).getBytes(),
            //     new File(root, String.format("MODS/%s/MODELPACKS.XML", name.toUpperCase())).getAbsolutePath()
            // );
        }

        FileIO.write(this.xmlDatabase.serialize(), new File(root, "XMLDB.DAT").getAbsolutePath());

        this.packages.clear();
    }


    public static void main(String[] args) {
        Assembler assembler = new Assembler(
            Chunk.loadChunk("E:/KART/PART/PARTS.BIN").loadResource(PartDB.class),
            Chunk.loadChunk("E:/KART/PART/SLOTS.BIN").loadResource(SlotDB.class),
            Chunk.loadChunk("E:/KART/PART/XMLDB.BIN").loadResource(XmlDb.class)
        );

        assembler.addCostumePackage(
            "vex_hd",
            "Slot_Char_Hands",
            -147077727,
            new String[] { "_GLOVEL", "_GLOVER" },
            "E:/KART/PACKAGES/VEX/ICONS/THUMB_VEX_HANDS_A.PNG",
            "E:/KART/PACKAGES/VEX/VEX_HD.GLB"
        );

        assembler.addCostumePackage(
            "vex_ft",
            "Slot_Char_Shoes",
            -1267069371,
            new String[] { "_SOCKS" },
            "E:/KART/PACKAGES/VEX/ICONS/THUMB_VEX_FEET_A.PNG",
            "E:/KART/PACKAGES/VEX/VEX_FT.GLB"
        );

        assembler.addCostumePackage(
            "vex_nk",
            "Slot_Char_HeadgearAccess",
            -1133499747,
            new String[] { "_SOCKS" },
            "E:/KART/PACKAGES/VEX/ICONS/THUMB_VEX_NECK_A.PNG",
            "E:/KART/PACKAGES/VEX/VEX_NK.GLB"
        );

        assembler.addCostumePackage(
            "vex_he",
            "Slot_Char_Headgear",
            65959567,
            new String[] { "_SCALP", "_BROW" },
            "E:/KART/PACKAGES/VEX/ICONS/THUMB_VEX_HEAD_A.PNG",
            "E:/KART/PACKAGES/VEX/VEX_HE.GLB"
        );

        assembler.addCostumePackage(
            "vex_lg",
            "Slot_Char_Pants",
            193205195,
            new String[] { "_SHORTS", "_PANTS", "_LEGS" },
            "E:/KART/PACKAGES/VEX/ICONS/THUMB_VEX_LEGS_A.PNG",
            "E:/KART/PACKAGES/VEX/VEX_LG.GLB"
        );

        assembler.addCostumePackage(
            "vex_to",
            "Slot_Char_Shirt",
            193860055,
            new String[] { "_TORSO", "_TORSO1", "_SLEEVER", "_SLEEVEL", "_ARML", "_ARMR" },
            "E:/KART/PACKAGES/VEX/ICONS/THUMB_VEX_TORSO_A.PNG",
            "E:/KART/PACKAGES/VEX/VEX_TO.GLB"
        );

        // assembler.save("Vex", "E:/zeon/rpcs3/dev_hdd0/game/NPUA80848/USRDIR/DATA");

        assembler.addSkinPackage(
            "easterbunny_base",
            "E:/KART/PACKAGES/EASTERBUNNY/ICONS/THUMB_EASTERBUNNY_SKIN_A.png",
            "E:/KART/PACKAGES/EASTERBUNNY/TEXTURES/T_EASTERBUNNY_SKIN_A.PNG",
            "E:/KART/PACKAGES/EASTERBUNNY/TEXTURES/T_EASTERBUNNY_SKIN_N.PNG",
            "E:/KART/PACKAGES/EASTERBUNNY/TEXTURES/T_EASTERBUNNY_SKIN_S.PNG"
        );

        assembler.addCostumePackage(
            "easterbunny_he",
            "Slot_Char_Headgear",
            65959567,
            null,
            "E:/KART/PACKAGES/EASTERBUNNY/ICONS/Thumb_EASTERBUNNY_HEAD_A.PNG",
            "E:/KART/PACKAGES/EASTERBUNNY/EASTERBUNNY_HE.GLB"
        );

        assembler.addCostumePackage(
            "easterbunny_to",
            "Slot_Char_Shirt",
            193860055,
            new String[] { "_TORSO", "_TORSO1", "_SLEEVER", "_SLEEVEL", "_ARML", "_ARMR" },
            "E:/KART/PACKAGES/EASTERBUNNY/ICONS/Thumb_EASTERBUNNY_TORSO_A.PNG",
            "E:/KART/PACKAGES/EASTERBUNNY/EASTERBUNNY_TO.GLB"
        );

        assembler.addCostumePackage(
            "easterbunny_lg",
            "Slot_Char_Pants",
            193205195,
            new String[] { "_SHORTS", "_PANTS", "_LEGS" },
            "E:/KART/PACKAGES/EASTERBUNNY/ICONS/THUMB_EASTERBUNNY_LEGS_A.PNG",
            "E:/KART/PACKAGES/EASTERBUNNY/EASTERBUNNY_LG.GLB"
        );

        assembler.addCostumePackage(
            "easterbunny_nk",
            "Slot_Char_HeadgearAccess",
            -1133499747,
            new String[] { "_SOCKS" },
            "E:/KART/PACKAGES/EASTERBUNNY/ICONS/THUMB_EASTERBUNNY_NECK_A.PNG",
            "E:/KART/PACKAGES/EASTERBUNNY/EASTERBUNNY_NK.GLB"
        );

        assembler.addCostumePackage(
            "easterbunny_fe",
            "Slot_Char_Shoes",
            -1267069371,
            new String[] { "_SOCKS" },
            "E:/KART/PACKAGES/EASTERBUNNY/ICONS/THUMB_EASTERBUNNY_FEET_A.PNG",
            "E:/KART/PACKAGES/EASTERBUNNY/EASTERBUNNY_FE.GLB"
        );

        assembler.addCostumePackage(
            "easterbunny_hd",
            "Slot_Char_Hands",
            -147077727,
            new String[] { "_GLOVEL", "_GLOVER" },
            "E:/KART/PACKAGES/EASTERBUNNY/ICONS/THUMB_EASTERBUNNY_HANDS_A.PNG",
            "E:/KART/PACKAGES/EASTERBUNNY/EASTERBUNNY_HD.GLB"
        );

        assembler.addCostumePackage(
            "easterbunny_ft",
            "Slot_Char_Mouth",
            -1267069371,
            null,
            "E:/KART/PACKAGES/EASTERBUNNY/ICONS/THUMB_EASTERBUNNY_MOUTH_A.PNG",
            "E:/KART/PACKAGES/EASTERBUNNY/EASTERBUNNY_FT.GLB"
        );

        assembler.addCostumePackage(
            "easterbunny_hr",
            "Slot_Char_Hair",
            -1771885287,
            null,
            "E:/KART/PACKAGES/EASTERBUNNY/ICONS/THUMB_EASTERBUNNY_HAIR_A.PNG",
            "E:/KART/PACKAGES/EASTERBUNNY/EASTERBUNNY_HR.GLB"
        );


        assembler.addCostumePackage(
            "rivet_ft",
            "Slot_Char_Mouth",
            -1267069371,
            null,
            "E:/KART/PACKAGES/RIVET/ICONS/THUMB_LOMBAX_MOUTH_A.PNG",
            "E:/KART/PACKAGES/RIVET/RIVET_FT.GLB"
        );

        assembler.addCostumePackage(
            "rivet_hd",
            "Slot_Char_Hands",
            -147077727,
            new String[] { "_GLOVEL", "_GLOVER" },
            "E:/KART/PACKAGES/RIVET/ICONS/THUMB_LOMBAX_HANDS_A.PNG",
            "E:/KART/PACKAGES/RIVET/RIVET_HD.GLB"
        );

        assembler.addCostumePackage(
            "rivet_fe",
            "Slot_Char_Shoes",
            -1267069371,
            new String[] { "_SOCKS" },
            "E:/KART/PACKAGES/RIVET/ICONS/THUMB_LOMBAX_FEET_A.PNG",
            "E:/KART/PACKAGES/RIVET/RIVET_FE.GLB"
        );

        assembler.addCostumePackage(
            "rivet_nk",
            "Slot_Char_HeadgearAccess",
            -1133499747,
            new String[] { "_SOCKS" },
            "E:/KART/PACKAGES/RIVET/ICONS/THUMB_LOMBAX_NECK_A.PNG",
            "E:/KART/PACKAGES/RIVET/RIVET_NK.GLB"
        );

        assembler.addCostumePackage(
            "rivet_lg",
            "Slot_Char_Pants",
            193205195,
            new String[] { "_SHORTS", "_PANTS", "_LEGS" },
            "E:/KART/PACKAGES/RIVET/ICONS/THUMB_LOMBAX_LEGS_A.PNG",
            "E:/KART/PACKAGES/RIVET/RIVET_LG.GLB"
        );

        assembler.addCostumePackage(
            "rivet_to",
            "Slot_Char_Shirt",
            193860055,
            new String[] { "_TORSO", "_TORSO1", "_SLEEVER", "_SLEEVEL", "_ARML", "_ARMR" },
            "E:/KART/PACKAGES/RIVET/ICONS/THUMB_LOMBAX_TORSO_A.PNG",
            "E:/KART/PACKAGES/RIVET/RIVET_TO.GLB"
        );

        assembler.addCostumePackage(
            "rivet_he",
            "Slot_Char_Headgear",
            65959567,
            null,
            "E:/KART/PACKAGES/RIVET/ICONS/THUMB_LOMBAX_HEAD_A.PNG",
            "E:/KART/PACKAGES/RIVET/RIVET_HE.GLB"
        );

        assembler.addSkinPackage(
            "rivet_base",
            "E:/KART/PACKAGES/RIVET/ICONS/THUMB_LOMBAX_SKIN_A.png",
            "E:/KART/PACKAGES/RIVET/TEXTURES/T_LOMBAX_SKIN_A.PNG",
            "E:/KART/PACKAGES/RIVET/TEXTURES/T_LOMBAX_SKIN_N.PNG",
            "E:/KART/PACKAGES/RIVET/TEXTURES/T_LOMBAX_SKIN_S.PNG"
        );




        assembler.save("SabaAssetPack", "E:/zeon/rpcs3/dev_hdd0/game/NPUA80848/USRDIR/DATA");
        
    }
}
