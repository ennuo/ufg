package ufg;


import java.io.File;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;

import com.google.gson.GsonBuilder;

import ufg.MeshChunker.ImportType;
import ufg.MeshChunker.MeshChunkerResult;
import ufg.MeshChunker.MeshImportData;
import ufg.TextureChunker.TextureChunkerResult;
import ufg.enums.PartAttrib;
import ufg.enums.ResourceType;
import ufg.io.Serializer;
import ufg.io.streams.MemoryInputStream.SeekMode;
import ufg.io.streams.MemoryOutputStream;
import ufg.resources.ChunkFileIndex;
import ufg.resources.Localization;
import ufg.resources.PartDB;
import ufg.resources.PartDB.Part;
import ufg.resources.PartDB.PartValue;
import ufg.resources.PartDB.SlotDB;
import ufg.resources.Texture;
import ufg.structures.SHA1;
import ufg.structures.XmlDb;
import ufg.structures.XmlDb.XmlDbNode;
import ufg.structures.chunks.Chunk;
import ufg.structures.chunks.ChunkFileIndexEntry;
import ufg.util.Bytes;
import ufg.util.FileIO;
import ufg.util.UFGCRC;

public class Assembler {
    public static final String EXPORT_DIRECTORY = "E:/emu/rpcs3/dev_hdd0/game/NPUA80848/USRDIR/DATA/";

    private static class Package {
        private String name;

        private Chunk[] modelStreamingChunks;
        private Chunk[] materialStreamingChunks;
        private ArrayList<Texture> textureStreamingChunks = new ArrayList<>();
        private ArrayList<byte[]> texturePackData = new ArrayList<>();
        public String suffix;

        public Package() {}
        public Package(MeshImportData config) {
            name = config.name;
            MeshChunkerResult result =  MeshChunker.getMeshChunkData(config);
            modelStreamingChunks = result.modelStreamingChunks;
            textureStreamingChunks.addAll(Arrays.asList(result.textureStreamingChunks));
            texturePackData.addAll(Arrays.asList(result.texturePackData));
            materialStreamingChunks = result.materialStreamingChunks;
            this.suffix = config.suffix;
        }

    }

    private final PartDB customizationDatabase;
    private final PartDB objectDatabase;
    private final PartDB uiDatabase;

    private final SlotDB customizationSlotDatabase;
    private final SlotDB objectSlotDatabase;
    private final SlotDB uiSlotDatabase;

    private final XmlDb xmlDatabase;
    private final Localization translationDatabase;

    private final ArrayList<Package> packages = new ArrayList<>();

    public Assembler(Chunk[] customization, Chunk[] object, Chunk[] ui, XmlDb xmlDB, Localization transDB) {
        this.customizationDatabase = customization[0].loadResource(PartDB.class);
        this.customizationSlotDatabase = customization[1].loadResource(SlotDB.class);

        this.objectDatabase = object[0].loadResource(PartDB.class);
        this.objectSlotDatabase = object[1].loadResource(SlotDB.class);

        this.uiDatabase = ui[0].loadResource(PartDB.class);
        this.uiSlotDatabase = ui[1].loadResource(SlotDB.class);

        this.xmlDatabase = xmlDB;
        this.translationDatabase = transDB;

        for (Part part : this.customizationDatabase.parts) {
            part.remove("DRMKey"); // Unlocks all DLC content
            part.remove("Selectable");
            part.remove(-503371329);
        }

        // Unhides developer objects
        for (Part part : this.objectDatabase.parts)
            part.remove("Developer");

        // Enable hidden UI options
        for (Part part : this.uiDatabase.parts)
        {
            part.remove("devonly");
        }

        // Restore some unused functionality
        addUnusedFunctions();

        addMockPackage("Body_IceCream", "Wheel_Cherry");
        addMockPackage("Body_Modnation", "Wheel_Cardboard1");

        FileIO.write(
            Chunk.saveChunks(new Chunk[] { 
                Chunk.toChunk(0x8D43D0B4, this.uiDatabase),
                Chunk.toChunk(0x01BC0C5C, this.uiSlotDatabase)
            }), 
            new File(EXPORT_DIRECTORY, "UI/CONFIG/SCREENDATA/PARTS.BIN").getAbsolutePath()
        );

        System.out.println(new File(EXPORT_DIRECTORY, "PARTDB/PARTS.BIN").getAbsolutePath());

        FileIO.write(
            Chunk.saveChunks(new Chunk[] { 
                Chunk.toChunk(0x8D43D0B4, this.customizationDatabase),
                Chunk.toChunk(0x01BC0C5C, this.customizationSlotDatabase)
            }), 
            new File(EXPORT_DIRECTORY, "PARTDB/PARTS.BIN").getAbsolutePath()
        );

        System.exit(0);

    }

    public void addMockPackage(String name, String wheels)
    {
        Part part = new Part();
        part.name = name;

        part.set(-503371329, new PartValue(PartAttrib.INT, 0));
        // part.set(893209143, 1635704381); // steering wheel?
        
        part.set(-630973327, 1703604333);
        part.set("AudioHorn", "GENERIC02");
        part.set(-168681289, "KARTBODY_CRDBRD_LARGE");
        part.set("AudioEngine", "DODGE_CHALLENGER");
        part.set(1603261606, "KARTHORN_GEN02_D"); // default horn
        // part.set("Slot_Kart_Seat", "Seat_Sponge"); // default seat 
        part.set("Slot_Kart_Suspension", 159626361);
        part.set(1870276171, "DODGE_CHALLENGER_D");
        // part.set("Slot_Kart_Steering", "Steering_Sponge"); // default steering
        // part.set("LocalizationDesc", name + "_desc");
        part.set("Slot_Kart_Wheel", wheels);
        // part.set(472226872, "Slot_Kart_Suspension");
        part.set(-1631555585, "Dodge_Challenger");
        part.set("UICategory", 74775394);

        this.customizationDatabase.parts.add(part);
        int index = -1;
        for (int i = 0; i < this.customizationSlotDatabase.entries.length; ++i) {
            if (this.customizationSlotDatabase.entries[i].name.equals("Slot_Kart_Body")) {
                index = i;
                break;
            }
        }

        if (index == -1) throw new RuntimeException("Slot not found!");
        this.customizationSlotDatabase.entries[index].uids.add(UFGCRC.qStringHashUpper32(part.name));
    }

    public void addSeatPackage(String name, String translation, String iconSourcePath, String glbSourcePath) {
        name = "Seat_" + name;
        this.translationDatabase.put(name, translation);
        Package pkg = new Package(new MeshImportData(name, glbSourcePath, ImportType.KartBody, "A"));
        this.packages.add(pkg);
        if (iconSourcePath != null) {
            TextureChunkerResult icon = TextureChunker.convertTextureData(name.toUpperCase() + "_ICON", ByteBuffer.wrap(FileIO.read(iconSourcePath)), "64");
            pkg.textureStreamingChunks.add(icon.texture);
            pkg.texturePackData.add(icon.data);
        }

        Part part = new Part();
        part.name = name;
        part.set("UICategory", 483648051);

        this.customizationDatabase.parts.add(part);
        int index = -1;
        for (int i = 0; i < this.customizationSlotDatabase.entries.length; ++i) {
            if (this.customizationSlotDatabase.entries[i].name.equals("Slot_Kart_Seat")) {
                index = i;
                break;
            }
        }

        if (index == -1) throw new RuntimeException("Slot not found!");
        this.customizationSlotDatabase.entries[index].uids.add(UFGCRC.qStringHashUpper32(part.name));
    }

    public void addSteeringPackage(String name, String translation, String iconSourcePath, String glbSourcePath) {
        
        name = "Steering_" + name;
        this.translationDatabase.put(name, translation);
        Package pkg = new Package(new MeshImportData(name, glbSourcePath, ImportType.KartBody, "A"));
        this.packages.add(pkg);
        if (iconSourcePath != null) {
            TextureChunkerResult icon = TextureChunker.convertTextureData(name.toUpperCase() + "_ICON", ByteBuffer.wrap(FileIO.read(iconSourcePath)), "64");
            pkg.textureStreamingChunks.add(icon.texture);
            pkg.texturePackData.add(icon.data);
        }

        Part part = new Part();
        part.name = name;
        part.set("UICategory", (int)4140139653L);

        this.customizationDatabase.parts.add(part);
        int index = -1;
        for (int i = 0; i < this.customizationSlotDatabase.entries.length; ++i) {
            if (this.customizationSlotDatabase.entries[i].name.equals("Slot_Kart_Steering")) {
                index = i;
                break;
            }
        }

        if (index == -1) throw new RuntimeException("Slot not found!");
        this.customizationSlotDatabase.entries[index].uids.add(UFGCRC.qStringHashUpper32(part.name));
    }

    public void addKartPackage(String name, String translation, String description, String iconSourcePath, String glbSourceBody, String glbSourceSuspensionLodPrefix, String suspension) {
        
        String partName = "Body_" + name;
        this.translationDatabase.put(partName + "_desc", description);
        this.translationDatabase.put(partName, translation);

        Package pkg = new Package(new MeshImportData(partName, glbSourceBody, ImportType.KartBody, "A"));
        this.packages.add(pkg);
        if (iconSourcePath != null) {
            TextureChunkerResult icon = TextureChunker.convertTextureData(partName.toUpperCase() + "_ICON", ByteBuffer.wrap(FileIO.read(iconSourcePath)), "64");
            pkg.textureStreamingChunks.add(icon.texture);
            pkg.texturePackData.add(icon.data);
        }


        // TextureChunkerResult shadow = TextureChunker.convertTextureData(partName.toUpperCase() + "_" + suspension.toUpperCase() + "_SHADOW", FileIO.read("F:/KART/SHADOW.PNG"), true);
        // pkg.textureStreamingChunks.add(shadow.texture);
        // pkg.texturePackData.add(shadow.data);

        String suspensionName = suspension + "_" + partName;
        MeshImportData config = new MeshImportData(suspensionName, glbSourceSuspensionLodPrefix + "_A.GLB", ImportType.KartSuspension, "A");
        this.packages.add(new Package(config));
        config = new MeshImportData(suspensionName, glbSourceSuspensionLodPrefix + "_B.GLB", ImportType.KartSuspension, "B");
        this.packages.add(new Package(config));
        config = new MeshImportData(suspensionName, glbSourceSuspensionLodPrefix + "_C.GLB", ImportType.KartSuspension, "C");
        this.packages.add(new Package(config));

        Part part = new Part();
        part.name = partName;

        boolean seatExists = false;
        boolean steeringExists = false;
        boolean wheelsExist = false;
        for (Part existingPart : this.customizationDatabase.parts) {

            if (existingPart.name.equals("Seat_" + name)) seatExists = true;
            if (existingPart.name.equals("Steering_" + name)) steeringExists = true;
            if (existingPart.name.equals("Wheel_" + name)) wheelsExist = true;
        }

        part.set(893209143, 1635704381);
        part.set(-630973327, (int)2418010899L);
        part.set(-503371329, new PartValue(PartAttrib.INT, 1));
        part.set("AudioHorn", "GENERIC02");
        part.set(-168681289, "KARTBODY_METAL_LARGE");
        part.set("AudioEngine", "DODGE_CHALLENGER");
        part.set(1603261606, "KARTHORN_GEN02_D"); // default horn
        part.set("Slot_Kart_Seat", "Seat_" + (seatExists ? name : "Sponge")); // default seat 
        part.set("Slot_Kart_Suspension", suspension);
        part.set(1870276171, "DODGE_CHALLENGER_D");
        part.set("Slot_Kart_Steering", "Steering_" + (steeringExists ? name : "Sponge")); // default steering
        part.set("LocalizationDesc", partName + "_desc");
        part.set("Slot_Kart_Wheel", "Wheel_" + (wheelsExist ? name : "Sponge"));
        part.set(472226872, "Slot_Kart_Suspension");
        part.set(-1631555585, "Dodge_Challenger");

        this.customizationDatabase.parts.add(part);
        int index = -1;
        for (int i = 0; i < this.customizationSlotDatabase.entries.length; ++i) {
            if (this.customizationSlotDatabase.entries[i].name.equals("Slot_Kart_Body")) {
                index = i;
                break;
            }
        }

        if (index == -1) throw new RuntimeException("Slot not found!");
        this.customizationSlotDatabase.entries[index].uids.add(UFGCRC.qStringHashUpper32(part.name));
    }

    public Part addUIComponent(String name, String slot)
    {
        Part part = new Part();
        part.name = name;

        int index = -1;
        for (int i = 0; i < this.uiSlotDatabase.entries.length; ++i) {
            if (this.uiSlotDatabase.entries[i].name.equals(slot)) {
                index = i;
                break;
            }
        }

        if (index == -1) throw new RuntimeException("Slot not found!");


        this.uiDatabase.parts.add(part);
        this.uiSlotDatabase.entries[index].uids.add(UFGCRC.qStringHashUpper32(name));

        return part;
    }

    public void addUnusedFunctions()
    {
        // Loops
        Part loops = addUIComponent("TE_Edit_Track_Loops", "TE_MainMenu_Edit_Track_Menu_Config");
        loops.set("Content", new PartValue(PartAttrib.STRING, "Loops"));
        loops.set("WidgetItemType", new PartValue(PartAttrib.UID, 1357437510));
        loops.set("Page", new PartValue(PartAttrib.INT, 1));
        loops.set("UITrackEditMode", new PartValue(PartAttrib.UID, UFGCRC.qStringHashUpper32("CONFIG_TE_MODE_LOOP_EDIT")));
        loops.set("ProcedureSubMode", new PartValue(PartAttrib.INT, 1));
        loops.set("ConditionToHide", new PartValue(PartAttrib.STRING, "IsValidArena || !IsValidTrack"));
        loops.set("IsCollapsibleHeader", new PartValue(PartAttrib.INT, 1));
        loops.set("UIMoreMenuAllowed", new PartValue(PartAttrib.INT, 0));
        // Snap to track
        Part snap = addUIComponent("TE_MainMenu_Popit_ToolsBag_Track_Placement", "TE_MainMenu_Tools_Bag_Menu_Config");
        snap.set("Icon", new PartValue(PartAttrib.STRING, "FE_TSIcon_ScrubPlacement_bw"));
        snap.set("OpenConfigOnSelect", new PartValue(PartAttrib.UID, 470306574));
        snap.set("Content", new PartValue(PartAttrib.STRING, "$TRACK_EDITOR_PLACE_ALONG_TRACK"));
        snap.set("WidgetItemType", new PartValue(PartAttrib.UID, (int)4229532545l));
        snap.set("Page", new PartValue(PartAttrib.INT, 0));
        snap.set("UITrackEditMode", new PartValue(PartAttrib.UID, UFGCRC.qStringHashUpper32("CONFIG_TE_MODE_PROPS_SCRUB")));
        // Autopopulate
        Part autopopulate = addUIComponent("TE_Edit_Track_Autopopulate", "TE_MainMenu_Edit_Track_Menu_Config");
        autopopulate.set("Icon", new PartValue(PartAttrib.STRING, "FE_TSIcon_AutoPopulate_bw"));
        autopopulate.set("Content", new PartValue(PartAttrib.STRING, "$TRACK_EDITOR_POPULATE"));
        autopopulate.set("WidgetItemType", new PartValue(PartAttrib.UID, 2024420685));
        autopopulate.set("Page", new PartValue(PartAttrib.INT, 0));
        autopopulate.set("UITrackEditMode", new PartValue(PartAttrib.UID, UFGCRC.qStringHashUpper32("CONFIG_TE_MODE_AUTO_POPULATE_PAINTING")));
        autopopulate.set("ConditionToHide", new PartValue(PartAttrib.STRING, "IsValidArena || !IsValidTrack"));
    }

    public void addCostumePackage(String packageName, String translation, String slot, int category, String[] selections, String iconSourcePath, String glbSourcePath) {
        this.addCostumePackage(packageName, translation, slot, category, selections, iconSourcePath, glbSourcePath, 0);
    }

    public void addCostumePackage(String packageName, String translation, String slot, int category, String[] selections, String iconSourcePath, String glbSourcePath, int flags) {
        Package pkg = new Package(new MeshImportData(packageName, glbSourcePath, ImportType.Costume, "A"));

        this.translationDatabase.put(packageName, translation);

        if (iconSourcePath != null) {
            TextureChunkerResult icon = TextureChunker.convertTextureData(packageName.toUpperCase() + "_ICON", ByteBuffer.wrap(FileIO.read(iconSourcePath)), "64");
            pkg.textureStreamingChunks.add(icon.texture);
            pkg.texturePackData.add(icon.data);
        }

        // Add to part tables

        Part packagePart = new Part();
        packagePart.name = packageName;
        packagePart.set("UICategory", new PartValue(PartAttrib.UID, category));

        if ((flags & DISABLE_ZIPPER) != 0)
            packagePart.set(-1279932325, new PartValue(PartAttrib.UID, -1));

        if (selections != null)
            for (int i = 0; i < selections.length; ++i)
                packagePart.set("HiddenSelectionSet" + (i), new PartValue(PartAttrib.UID, UFGCRC.qStringHashUpper32(selections[i].toUpperCase())));
        this.customizationDatabase.parts.add(packagePart);

        // Add to associated slot

        int index = -1;
        for (int i = 0; i < this.customizationSlotDatabase.entries.length; ++i) {
            if (this.customizationSlotDatabase.entries[i].name.equals(slot)) {
                index = i;
                break;
            }
        }

        if (index == -1) throw new RuntimeException("Slot not found!");

        this.customizationSlotDatabase.entries[index].uids.add(UFGCRC.qStringHashUpper32(packageName));
        
        this.packages.add(pkg);
    }

    public static int DISABLE_TONGUE = 0x1;
    public static int DISABLE_ZIPPER = 0x2;

    public void addSkinPackage(String packageName, String translation, String iconSourcePath, String diffuseIconPath, String normalIconPath, String specularIconPath) {
        Package pkg = new Package();
        this.translationDatabase.put(packageName, translation);
        pkg.name = packageName;

        pkg.modelStreamingChunks = new Chunk[0];
        pkg.materialStreamingChunks = new Chunk[0];

        TextureChunkerResult texture = TextureChunker.convertTextureData(packageName.toUpperCase() + "_D", ByteBuffer.wrap(FileIO.read(diffuseIconPath)), "256");
        pkg.textureStreamingChunks.add(texture.texture);
        pkg.texturePackData.add(texture.data);

        texture = TextureChunker.convertTextureData(packageName.toUpperCase() + "_N", ByteBuffer.wrap(FileIO.read(normalIconPath)), "64");
        pkg.textureStreamingChunks.add(texture.texture);
        pkg.texturePackData.add(texture.data);

        texture = TextureChunker.convertTextureData(packageName.toUpperCase() + "_S", ByteBuffer.wrap(FileIO.read(specularIconPath)), "128");
        pkg.textureStreamingChunks.add(texture.texture);
        pkg.texturePackData.add(texture.data);

        texture = TextureChunker.convertTextureData(packageName.toUpperCase() + "_ICON", ByteBuffer.wrap(FileIO.read(iconSourcePath)), "64");
        pkg.textureStreamingChunks.add(texture.texture);
        pkg.texturePackData.add(texture.data);

        // Add to part tables

        Part packagePart = new Part();
        packagePart.name = packageName;
        packagePart.set("UICategory", new PartValue(PartAttrib.UID, 1000219946));
        packagePart.set("StateBlock", new PartValue(PartAttrib.STRING, "Cloth"));
        this.customizationDatabase.parts.add(packagePart);

        // Add to associated slot

        int index = -1;
        for (int i = 0; i < this.customizationSlotDatabase.entries.length; ++i) {
            if (this.customizationSlotDatabase.entries[i].name.equals("Slot_Char_Pattern")) {
                index = i;
                break;
            }
        }

        if (index == -1) throw new RuntimeException("Slot not found!");

        this.customizationSlotDatabase.entries[index].uids.add(UFGCRC.qStringHashUpper32(packageName));
        
        this.packages.add(pkg);
    }

    public void save(String name, String type, String path) {
        File root = new File(path);
        
        ArrayList<Chunk> textureChunks = new ArrayList<>();

        ArrayList<SHA1> hashes = new ArrayList<>();
        ArrayList<Integer> offsets = new ArrayList<>();
        ArrayList<Integer> uids = new ArrayList<>();

        ArrayList<byte[]> modelChunkFiles = new ArrayList<>();
        ChunkFileIndex modelPackIndex = new ChunkFileIndex();
        ChunkFileIndex texturePackIndex = new ChunkFileIndex();

        int textureCount = this.packages.stream()
                                        .mapToInt(pkg -> pkg.textureStreamingChunks.size())
                                        .reduce(0, (total, current) -> total + current);

        final boolean USE_PERM_PACK = true;
        final boolean USE_TEMP_PACK = !USE_PERM_PACK;
        final boolean USE_PATCHES = false;

        MemoryOutputStream texturePackStream = new MemoryOutputStream((4_767_119 * textureCount) + (0x80 * textureCount * 2));
        if (USE_TEMP_PACK)
            texturePackStream.i32(0x5E73CDD7);

        int modelOffset = 0;

        // Do first material chunk pass
        for (Package pkg : this.packages) {
            if (pkg.materialStreamingChunks == null || pkg.materialStreamingChunks.length == 0) continue;
            byte[] chunkFile = Chunk.saveChunks(pkg.materialStreamingChunks);
            modelChunkFiles.add(chunkFile);
            modelOffset += chunkFile.length;
        }

        if (modelChunkFiles.size() != 0) {
            modelPackIndex.entries.add(new ChunkFileIndexEntry(UFGCRC.qStringHashUpper32(name + "ModelPackStreaming_shared"), 0, modelOffset));
            byte[] deadbeef = new MemoryOutputStream(0x400).AddDeadBeef().getBuffer();
            modelChunkFiles.add(deadbeef);
            modelOffset += deadbeef.length;
        }

        for (Package pkg : this.packages) {
            if (pkg.modelStreamingChunks.length != 0) {
                byte[] chunkFile = Chunk.saveChunks(pkg.modelStreamingChunks);

                modelPackIndex.entries.add(new ChunkFileIndexEntry(UFGCRC.qStringHashUpper32(pkg.name + pkg.suffix), modelOffset, chunkFile.length));

                modelChunkFiles.add(chunkFile);
                byte[] deadbeef = new MemoryOutputStream(0x400).AddDeadBeef().getBuffer();
                modelChunkFiles.add(deadbeef);

                modelOffset += chunkFile.length;
                modelOffset += deadbeef.length;
            }

            for (int i = 0; i < pkg.texturePackData.size(); ++i) {
                Texture texture = pkg.textureStreamingChunks.get(i);
                byte[] imageData = pkg.texturePackData.get(i);

                int textureDataUID = UFGCRC.qStringHash32(texture.name + ".VRAM.TEX");

                SHA1 sha1 = SHA1.fromBuffer(imageData);

                int offset;
                int hash_index = hashes.indexOf(sha1);
                if (hash_index == -1) {

                    if (USE_TEMP_PACK) {
                        hashes.add(sha1);
                        if ((texturePackStream.getOffset()) % 0x80 != 0)
                            texturePackStream.pad((0x80 - ((texturePackStream.getOffset()) % 0x80)));
                        offset = texturePackStream.getOffset();
                        offsets.add(offset);
                        texturePackStream.bytes(imageData);
                        if ((texturePackStream.getOffset()) % 0x80 != 0)
                            texturePackStream.pad((0x80 - ((texturePackStream.getOffset()) % 0x80)));
                    }

                    if (USE_PERM_PACK) {
                        offset = 0x80;

                        offsets.add(offset);
                        uids.add(textureDataUID);

                        int textureDataStartOffset = texturePackStream.getOffset();

                        texturePackStream.i32(0x5E73CDD7);
                        int dataOffset = 0x70;

                        int dataSize = imageData.length;
                        dataSize += dataOffset;

                        texturePackIndex.entries.add(new ChunkFileIndexEntry(textureDataUID, textureDataStartOffset, dataSize + 0x10));

                        texturePackStream.i32(dataSize);
                        texturePackStream.i32(dataSize);
                        texturePackStream.i32(dataOffset);
                        texturePackStream.pad(dataOffset);
                        texturePackStream.bytes(imageData);

                        texturePackStream.AddDeadBeef();
                        texturePackStream.AddPaddedRegion();
                    }

                } else {
                    if (USE_PERM_PACK)
                        textureDataUID = uids.get(hash_index);
                    offset = offsets.get(hash_index);
                }
                

                // Append texture to stream

                if (USE_PERM_PACK) {
                    int textureIndexUID = UFGCRC.qStringHash32(texture.name + ".HEAD.TEX");

                    texture.alphaStateSampler = textureDataUID;
                    texture.imageDataPosition = 0x80;
                    texture.imageDataByteSize = imageData.length + 0x70 + 0x10 - 0x80;


                    texturePackIndex.entries.add(new ChunkFileIndexEntry(textureIndexUID, texturePackStream.getOffset(), 0x110));
                
                    texturePackStream.i32(ResourceType.TEXTURE_DESCRIPTOR.getValue());
                    texturePackStream.i32(0x100); texturePackStream.i32(0x100);
                    texturePackStream.i32(0);

                    Serializer serializer = new Serializer(texturePackStream);
                    serializer.struct(texture, Texture.class);

                    texturePackStream.AddDeadBeef();
                    if (i + 1 < pkg.texturePackData.size())
                        texturePackStream.AddPaddedRegion();
                } else {
                    if (USE_PATCHES)
                        texture.alphaStateSampler = UFGCRC.qStringHashUpper32(String.format("Illusion:Texture:VINYLTEXTUREPACKSTREAMING_PATCH2.TEMP.BIN", name));
                    else
                        texture.alphaStateSampler = UFGCRC.qStringHashUpper32(String.format("Illusion:Texture:%sTexturePack.TEMP.BIN", name));
                    texture.imageDataPosition = offset;
                    texture.imageDataByteSize = imageData.length;
                    textureChunks.add(Chunk.toChunk(ResourceType.TEXTURE_DESCRIPTOR.getValue(), texture));
                }
            }
        }

        if (USE_TEMP_PACK) {
            int end = texturePackStream.getOffset();
            texturePackStream.seek(0x4, SeekMode.Begin);
            texturePackStream.i32(end - 0x10);
            texturePackStream.i32(end - 0x10);
            texturePackStream.seek(end, SeekMode.Begin);

            texturePackStream.shrink();
            byte[] texturePack = texturePackStream.getBuffer();

            if (USE_PATCHES) {
                FileIO.write(texturePack, new File(root, "CREATE/ROBOTOY/TEXTURES/RESLO/PATCH2/VINYLTEXTUREPACKSTREAMING_PATCH2.TEMP.BIN").getAbsolutePath());
                FileIO.write(Chunk.saveChunks(textureChunks.toArray(Chunk[]::new)), new File(root, "CREATE/ROBOTOY/TEXTURES/RESLO/PATCH2/VINYLTEXTUREPACKSTREAMING_PATCH2.PERM.BIN").getAbsolutePath());
            } else {
                FileIO.write(texturePack, new File(root, String.format("MODS/%s/%sTEXTUREPACK.TEMP.BIN", type.toUpperCase(), name.toUpperCase())).getAbsolutePath());
                FileIO.write(Chunk.saveChunks(textureChunks.toArray(Chunk[]::new)), new File(root, String.format("MODS/%s/%sTEXTUREPACK.PERM.BIN", type.toUpperCase(), name.toUpperCase())).getAbsolutePath());
            }

        }

        if (USE_PERM_PACK) {
            texturePackStream.shrink();
            byte[] texturePack = texturePackStream.getBuffer();

            if (USE_PATCHES)
                FileIO.write(texturePack, new File(root, String.format("CREATE/ROBOTOY/TEXTURES/RESLO/PATCH2/VINYLTEXTUREPACKSTREAMING_PATCH2.PERM.BIN", name.toUpperCase(), name.toUpperCase())).getAbsolutePath());
            else
                FileIO.write(texturePack, new File(root, String.format("MODS/%s/%sTEXTUREPACK.PERM.BIN", type.toUpperCase(), name.toUpperCase())).getAbsolutePath());

            texturePackIndex.entries.sort((a, b) -> Integer.compareUnsigned(a.filenameUID, b.filenameUID));
            texturePackIndex.name = name + "TexturePack.perm";
            if (USE_PATCHES)
                texturePackIndex.name = "vinyltexturepackstreaming_patch2.perm";
            texturePackIndex.UID = UFGCRC.qStringHashUpper32(texturePackIndex.name);

            if (USE_PATCHES) {
                FileIO.write(
                    Chunk.saveChunks(new Chunk[] { Chunk.toChunk(ResourceType.CHUNK_FILE_INDEX.getValue(), texturePackIndex) }),
                    new File(root, String.format("CREATE/ROBOTOY/TEXTURES/RESLO/PATCH2/VINYLTEXTUREPACKSTREAMING_PATCH2.PERM.IDX", name.toUpperCase(), name.toUpperCase())).getAbsolutePath()
                );
            } else {
                FileIO.write(
                    Chunk.saveChunks(new Chunk[] { Chunk.toChunk(ResourceType.CHUNK_FILE_INDEX.getValue(), texturePackIndex) }),
                    new File(root, String.format("MODS/%s/%sTEXTUREPACK.PERM.IDX", type.toUpperCase(), name.toUpperCase())).getAbsolutePath()
                );
            }
        }


        FileIO.write(
            Chunk.saveChunks(new Chunk[] { 
                Chunk.toChunk(0x8D43D0B4, this.customizationDatabase),
                Chunk.toChunk(0x01BC0C5C, this.customizationSlotDatabase)
            }), 
            new File(root, "PARTDB/PARTS.BIN").getAbsolutePath()
        );

        FileIO.write(
            Chunk.saveChunks(new Chunk[] { 
                Chunk.toChunk(0x8D43D0B4, this.objectDatabase),
                Chunk.toChunk(0x01BC0C5C, this.objectSlotDatabase)
            }), 
            new File(root, "WORLD/TRACKSTUDIO2/THEMES/PARTS.BIN").getAbsolutePath()
        );

        FileIO.write(
            Chunk.saveChunks(new Chunk[] { 
                Chunk.toChunk(0x8D43D0B4, this.uiDatabase),
                Chunk.toChunk(0x01BC0C5C, this.uiSlotDatabase)
            }), 
            new File(root, "UI/CONFIG/SCREENDATA/PARTS.BIN").getAbsolutePath()
        );

        FileIO.write(
            Chunk.saveChunks(new Chunk[] {
                Chunk.toChunk(0x90CE6B7A, this.translationDatabase),
            }),
            new File(root, "LOC/EN/CHARPART.BIN").getAbsolutePath()
        );

        modelPackIndex.entries.sort((a, b) -> Long.compareUnsigned(a.filenameUID & 0xffffffffl, b.filenameUID & 0xffffffffl));

        modelPackIndex.name = name + "ModelPackStreaming";
        if (USE_PATCHES)
            modelPackIndex.name = "CharModelPackStreaming_Patch2";

        modelPackIndex.UID = UFGCRC.qStringHashUpper32(modelPackIndex.name);

        if (modelPackIndex.entries.size() != 0) {
            if (USE_PATCHES) {
                FileIO.write(
                    Chunk.saveChunks(new Chunk[] { Chunk.toChunk(ResourceType.CHUNK_FILE_INDEX.getValue(), modelPackIndex) }),
                    new File(root, "CREATE\\ROBOTOY\\CHARMODELPACKSTREAMING_PATCH2.IDX").getAbsolutePath()
                );

                FileIO.write(
                    Bytes.combine(modelChunkFiles.toArray(byte[][]::new)),
                    new File(root, "CREATE\\ROBOTOY\\CHARMODELPACKSTREAMING_PATCH2.BIN").getAbsolutePath()
                );
            }
            else {
                FileIO.write(
                    Chunk.saveChunks(new Chunk[] { Chunk.toChunk(ResourceType.CHUNK_FILE_INDEX.getValue(), modelPackIndex) }),
                    new File(root, "MODS/" + type.toUpperCase() + "/" + name.toUpperCase() + "MODELPACKSTREAMING.IDX").getAbsolutePath()
                );

                FileIO.write(
                    Bytes.combine(modelChunkFiles.toArray(byte[][]::new)),
                    new File(root, "MODS/" + type.toUpperCase() + "/" + name.toUpperCase() + "MODELPACKSTREAMING.BIN").getAbsolutePath()
                );
            }
        }


        String texturePath = "Mods\\" + type  + "\\" + name + "TexturePack.perm.bin";
        XmlDbNode texturePackXML = this.xmlDatabase.getNode("Data\\Create\\" + type + "\\Textures\\ResLo\\TexturePacks.xml");

        if (USE_PATCHES) {
            texturePackXML = this.xmlDatabase.addNode("Data\\Create\\RoboToy\\Textures\\ResLo\\Patch1\\TexturePacks.xml");
            texturePath = "create\\robotoy\\textures\\reslo\\patch2\\vinyltexturepackstreaming_patch2.perm.bin";
        }

        if (USE_PERM_PACK) {
            texturePath = "Mods\\" + type + "\\" + name + "TexturePack.perm.idx";

            if (USE_PATCHES) {
                texturePackXML = this.xmlDatabase.addNode("Data\\Create\\RoboToy\\Textures\\ResLo\\Patch1\\TexturePacks.xml");
                texturePath = "create\\robotoy\\textures\\reslo\\patch2\\vinyltexturepackstreaming_patch2.perm.idx";
            }

        }

        // String texturePackPath = String.format("Data\\Mods\\%s\\TexturePacks.xml", name);
        if (texturePackXML.strings.indexOf("$(PatchDir)\\Data\\" + texturePath) == -1)
            texturePackXML.strings.add(0, "$(PatchDir)\\Data\\" + texturePath);
        
        // FileIO.write(
        //     String.format("<?xml version=\"1.0\"?>\n<!-- Generated by pipeline.exe -->\n<TexturePack>\n\t<TexturePackStream>%s</TexturePackStream>\n</TexturePack>", texturePath).getBytes(),
        //     new File(root, String.format("MODS/%s/TEXTUREPACKS.XML", name.toUpperCase())).getAbsolutePath()
        // );

        if (modelPackIndex.entries.size() != 0) {
            String modelPath = "Mods\\" + type + "\\" + name + "ModelPackStreaming.idx";
            if (USE_PATCHES)
                modelPath = "Create\\RoboToy\\CharModelPackStreaming_Patch2.idx";

            // String modelPackPath = String.format("Data\\Mods\\%s\\ModelPacks.xml", name);
            XmlDbNode modelPackXML = this.xmlDatabase.getNode("Data\\Create\\" + type + "\\ModelPacks.xml");

            if (modelPackXML.strings.indexOf("$(PatchDir)\\Data\\" + modelPath) == -1)
                modelPackXML.strings.add(0, "$(PatchDir)\\Data\\" + modelPath);

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
            Chunk.loadChunks("F:/KART/PART/CUSTOMIZATION.BIN"),
            Chunk.loadChunks("F:/KART/PART/OBJECTS.BIN"),
            Chunk.loadChunks("F:/KART/PART/UI.BIN"),
            Chunk.loadChunk("F:/KART/PART/XMLDB.BIN").loadResource(XmlDb.class),
            Chunk.loadChunk("F:/KART/LOC/CHARPART.BIN").loadResource(Localization.class)
        );

        assembler.addKartPackage(
            "CrazyTaxi",
            "Crazy Taxi",
            "Error - Change me",
            null,
            "F:/KART/PACKAGES/CRAZYTAXI/BODY_CRAZYTAXI.GLB",
            "F:/KART/PACKAGES/CRAZYTAXI/SUSPENSION_DRAG_BODY_CRAZYTAXI",
            "Suspension_LowRider"
        );

        // assembler.save("CrazyTaxi", "Kart", EXPORT_DIRECTORY);

        assembler.addKartPackage(
            "PattyWagon", 
            "Patty Wagon", 
            "You don't need a license to drive a sandwich",
            "F:/KART/PACKAGES/PATTYWAGON/THUMB_BODY_PATTYWAGON_A.PNG",
            "F:/KART/PACKAGES/PATTYWAGON/BODY_PATTYWAGON.GLB",
            "F:/KART/PACKAGES/PATTYWAGON/SUSPENSION_STOCK_BODY_PATTYWAGON",
            "Suspension_Stock"
        );

        // assembler.save("PattyWagon", "Kart", EXPORT_DIRECTORY);

        assembler.addSteeringPackage(
            "BlueFalcon",
            "Blue Falcon",
            null,
            "F:/KART/PACKAGES/BLUEFALCON/STEERING_BLUEFALCON.GLB"
        );

        assembler.addSeatPackage(
            "BlueFalcon",
            "Blue Falcon",
            null,
            "F:/KART/PACKAGES/BLUEFALCON/SEAT_BLUEFALCON.GLB"
        );

        assembler.addKartPackage(
            "BlueFalcon", 
            "Blue Falcon", 
            "Error - Change me",
            "F:/KART/PACKAGES/BLUEFALCON/THUMB_BODY_BLUEFALCON_A.PNG",
            "F:/KART/PACKAGES/BLUEFALCON/BODY_BLUEFALCON.GLB",
            "F:/KART/PACKAGES/BLUEFALCON/SUSPENSION_LOWRIDER_BODY_BLUEFALCON",
            "Suspension_LowRider"
        );

        // assembler.save("BlueFalcon", "Kart", EXPORT_DIRECTORY);

        assembler.addKartPackage(
            "Feisar",
            "Feisar Prototype",
            "Error - Change me",
            null,
            "F:/KART/PACKAGES/FEISAR/BODY_FEISAR.GLB",
            "F:/KART/PACKAGES/FEISAR/SUSPENSION_HOVER_BODY_FEISAR",
            "Suspension_Hover"
        );

        // assembler.save("Feisar", "Kart", EXPORT_DIRECTORY);

        assembler.addKartPackage(
            "Scooty",
            "Mister Scooty",
            "Error - Change me",
            null,
            "F:/KART/PACKAGES/SCOOTY/BODY_SCOOTY.GLB",
            "F:/KART/PACKAGES/SCOOTY/SUSPENSION_STOCK_BODY_SCOOTY",
            "Suspension_LowRider"
        );

        // assembler.save("Scooty", "Kart", EXPORT_DIRECTORY);

        assembler.addKartPackage(
            "Victini",
            "Banana",
            "Error - Change me",
            null,
            "F:/KART/PACKAGES/VICTINI/BODY_VICTINI.GLB",
            "F:/KART/PACKAGES/VICTINI/SUSPENSION_STOCK_BODY_VICTINI",
            "Suspension_Stock"
        );

        //assembler.save("Victini", "Kart", EXPORT_DIRECTORY);

        assembler.save("KartCache", "Kart", EXPORT_DIRECTORY);


        assembler.addCostumePackage(
            "vcm_to",
            "The Devil of Carnivalia",
            "Slot_Char_Shirt",
            193860055,
            new String[] { "_TORSO", "_TORSO1", "_SLEEVER", "_SLEEVEL" },
            "F:/KART/PACKAGES/VCM/ICONS/THUMB_VCM_TORSO_A.PNG",
            "F:/KART/PACKAGES/VCM/VCM_TO.GLB",
            DISABLE_ZIPPER
        );

        assembler.addCostumePackage(
            "vcm_he",
            "The Devil of Carnivalia",
            "Slot_Char_Headgear",
            65959567,
            new String[] { },
            "F:/KART/PACKAGES/VCM/ICONS/THUMB_VCM_HEAD_A.PNG",
            "F:/KART/PACKAGES/VCM/VCM_HE.GLB"
        );

        // assembler.save("VCM", "RoboToy", EXPORT_DIRECTORY);

        assembler.addCostumePackage(
            "vex_hd",
            "Vex Gloves",
            "Slot_Char_Hands",
            -147077727,
            new String[] { "_GLOVEL", "_GLOVER" },
            "F:/KART/PACKAGES/VEX/ICONS/THUMB_VEX_HANDS_A.PNG",
            "F:/KART/PACKAGES/VEX/VEX_HD.GLB"
        );

        assembler.addCostumePackage(
            "vex_ft",
            "Vex Shoes",
            "Slot_Char_Shoes",
            -1267069371,
            new String[] { "_SOCKS" },
            "F:/KART/PACKAGES/VEX/ICONS/THUMB_VEX_FEET_A.PNG",
            "F:/KART/PACKAGES/VEX/VEX_FT.GLB"
        );

        assembler.addCostumePackage(
            "vex_nk",
            "Vex Cape",
            "Slot_Char_HeadgearAccess",
            -1133499747,
            new String[] { "_SOCKS" },
            "F:/KART/PACKAGES/VEX/ICONS/THUMB_VEX_NECK_A.PNG",
            "F:/KART/PACKAGES/VEX/VEX_NK.GLB"
        );

        assembler.addCostumePackage(
            "vex_he",
            "Vex Head",
            "Slot_Char_Headgear",
            65959567,
            new String[] { "_SCALP", "_BROW" },
            "F:/KART/PACKAGES/VEX/ICONS/THUMB_VEX_HEAD_A.PNG",
            "F:/KART/PACKAGES/VEX/VEX_HE.GLB",
            DISABLE_TONGUE
        );

        assembler.addCostumePackage(
            "vex_lg",
            "Vex Trousers",
            "Slot_Char_Pants",
            193205195,
            new String[] { "_SHORTS", "_PANTS", "_LEGS" },
            "F:/KART/PACKAGES/VEX/ICONS/THUMB_VEX_LEGS_A.PNG",
            "F:/KART/PACKAGES/VEX/VEX_LG.GLB"
        );

        assembler.addCostumePackage(
            "vex_to",
            "Vex Vest",
            "Slot_Char_Shirt",
            193860055,
            new String[] { "_TORSO", "_TORSO1", "_SLEEVER", "_SLEEVEL", "_ARML", "_ARMR" },
            "F:/KART/PACKAGES/VEX/ICONS/THUMB_VEX_TORSO_A.PNG",
            "F:/KART/PACKAGES/VEX/VEX_TO.GLB",
            DISABLE_ZIPPER
        );

        // assembler.save("Vex", "RoboToy", EXPORT_DIRECTORY);

        assembler.addSkinPackage(
            "easterbunny_base",
            "Easter Bunny Skin",
            "F:/KART/PACKAGES/EASTERBUNNY/ICONS/THUMB_EASTERBUNNY_SKIN_A.png",
            "F:/KART/PACKAGES/EASTERBUNNY/TEXTURES/T_EASTERBUNNY_SKIN_A.PNG",
            "F:/KART/PACKAGES/EASTERBUNNY/TEXTURES/T_EASTERBUNNY_SKIN_N.PNG",
            "F:/KART/PACKAGES/EASTERBUNNY/TEXTURES/T_EASTERBUNNY_SKIN_S.PNG"
        );

        assembler.addCostumePackage(
            "easterbunny_he",
            "Easter Bunny Ears",
            "Slot_Char_Headgear",
            65959567,
            null,
            "F:/KART/PACKAGES/EASTERBUNNY/ICONS/Thumb_EASTERBUNNY_HEAD_A.PNG",
            "F:/KART/PACKAGES/EASTERBUNNY/EASTERBUNNY_HE.GLB"
        );

        assembler.addCostumePackage(
            "easterbunny_to",
            "Easter Bunny Coat",
            "Slot_Char_Shirt",
            193860055,
            new String[] { "_TORSO", "_TORSO1", "_SLEEVER", "_SLEEVEL", "_ARML", "_ARMR" },
            "F:/KART/PACKAGES/EASTERBUNNY/ICONS/Thumb_EASTERBUNNY_TORSO_A.PNG",
            "F:/KART/PACKAGES/EASTERBUNNY/EASTERBUNNY_TO.GLB",
            DISABLE_ZIPPER
        );

        assembler.addCostumePackage(
            "easterbunny_lg",
            "Easter Bunny Pants",
            "Slot_Char_Pants",
            193205195,
            new String[] { "_SHORTS", "_PANTS", "_LEGS" },
            "F:/KART/PACKAGES/EASTERBUNNY/ICONS/THUMB_EASTERBUNNY_LEGS_A.PNG",
            "F:/KART/PACKAGES/EASTERBUNNY/EASTERBUNNY_LG.GLB"
        );

        assembler.addCostumePackage(
            "easterbunny_nk",
            "Easter Bunny Bowtie",
            "Slot_Char_HeadgearAccess",
            -1133499747,
            new String[] { "_SOCKS" },
            "F:/KART/PACKAGES/EASTERBUNNY/ICONS/THUMB_EASTERBUNNY_NECK_A.PNG",
            "F:/KART/PACKAGES/EASTERBUNNY/EASTERBUNNY_NK.GLB"
        );

        assembler.addCostumePackage(
            "easterbunny_fe",
            "Easter Bunny Feet",
            "Slot_Char_Shoes",
            -1267069371,
            new String[] { "_SOCKS" },
            "F:/KART/PACKAGES/EASTERBUNNY/ICONS/THUMB_EASTERBUNNY_FEET_A.PNG",
            "F:/KART/PACKAGES/EASTERBUNNY/EASTERBUNNY_FE.GLB"
        );

        assembler.addCostumePackage(
            "easterbunny_hd",
            "Easter Bunny Gloves",
            "Slot_Char_Hands",
            -147077727,
            new String[] { "_GLOVEL", "_GLOVER" },
            "F:/KART/PACKAGES/EASTERBUNNY/ICONS/THUMB_EASTERBUNNY_HANDS_A.PNG",
            "F:/KART/PACKAGES/EASTERBUNNY/EASTERBUNNY_HD.GLB"
        );

        assembler.addCostumePackage(
            "easterbunny_ft",
            "Easter Bunny Nose",
            "Slot_Char_Mouth",
            -1267069371,
            null,
            "F:/KART/PACKAGES/EASTERBUNNY/ICONS/THUMB_EASTERBUNNY_MOUTH_A.PNG",
            "F:/KART/PACKAGES/EASTERBUNNY/EASTERBUNNY_FT.GLB"
        );

        assembler.addCostumePackage(
            "easterbunny_hr",
            "Easter Bunny Hair",
            "Slot_Char_Hair",
            -1771885287,
            null,
            "F:/KART/PACKAGES/EASTERBUNNY/ICONS/THUMB_EASTERBUNNY_HAIR_A.PNG",
            "F:/KART/PACKAGES/EASTERBUNNY/EASTERBUNNY_HR.GLB"
        );

        // assembler.save("EasterBunny", "RoboToy", EXPORT_DIRECTORY);

        assembler.addCostumePackage(
            "rivet_ft",
            "Rivet Nose",
            "Slot_Char_Mouth",
            -1267069371,
            null,
            "F:/KART/PACKAGES/RIVET/ICONS/THUMB_LOMBAX_MOUTH_A.PNG",
            "F:/KART/PACKAGES/RIVET/RIVET_FT.GLB"
        );

        assembler.addCostumePackage(
            "rivet_hd",
            "Rivet Arm",
            "Slot_Char_Hands",
            -147077727,
            new String[] { "_GLOVEL", "_GLOVER" },
            "F:/KART/PACKAGES/RIVET/ICONS/THUMB_LOMBAX_HANDS_A.PNG",
            "F:/KART/PACKAGES/RIVET/RIVET_HD.GLB"
        );

        assembler.addCostumePackage(
            "rivet_fe",
            "Rivet Boots",
            "Slot_Char_Shoes",
            -1267069371,
            new String[] { "_SOCKS" },
            "F:/KART/PACKAGES/RIVET/ICONS/THUMB_LOMBAX_FEET_A.PNG",
            "F:/KART/PACKAGES/RIVET/RIVET_FE.GLB"
        );

        assembler.addCostumePackage(
            "rivet_nk",
            "Rivet Scarf",
            "Slot_Char_HeadgearAccess",
            -1133499747,
            new String[] { "_SOCKS" },
            "F:/KART/PACKAGES/RIVET/ICONS/THUMB_LOMBAX_NECK_A.PNG",
            "F:/KART/PACKAGES/RIVET/RIVET_NK.GLB"
        );

        assembler.addCostumePackage(
            "rivet_lg",
            "Rivet Pants",
            "Slot_Char_Pants",
            193205195,
            new String[] { "_SHORTS", "_PANTS", "_LEGS" },
            "F:/KART/PACKAGES/RIVET/ICONS/THUMB_LOMBAX_LEGS_A.PNG",
            "F:/KART/PACKAGES/RIVET/RIVET_LG.GLB"
        );

        assembler.addCostumePackage(
            "rivet_to",
            "Rivet Suit",
            "Slot_Char_Shirt",
            193860055,
            new String[] { "_TORSO", "_TORSO1", "_SLEEVER", "_SLEEVEL", "_ARML", "_ARMR" },
            "F:/KART/PACKAGES/RIVET/ICONS/THUMB_LOMBAX_TORSO_A.PNG",
            "F:/KART/PACKAGES/RIVET/RIVET_TO.GLB",
            DISABLE_ZIPPER
        );

        assembler.addCostumePackage(
            "rivet_he",
            "Rivet Ears",
            "Slot_Char_Headgear",
            65959567,
            null,
            "F:/KART/PACKAGES/RIVET/ICONS/THUMB_LOMBAX_HEAD_A.PNG",
            "F:/KART/PACKAGES/RIVET/RIVET_HE.GLB"
        );

        assembler.addSkinPackage(
            "rivet_base",
            "Rivet Skin",
            "F:/KART/PACKAGES/RIVET/ICONS/THUMB_LOMBAX_SKIN_A.png",
            "F:/KART/PACKAGES/RIVET/TEXTURES/T_LOMBAX_SKIN_A.PNG",
            "F:/KART/PACKAGES/RIVET/TEXTURES/T_LOMBAX_SKIN_N.PNG",
            "F:/KART/PACKAGES/RIVET/TEXTURES/T_LOMBAX_SKIN_S.PNG"
        );

        // assembler.save("Rivet", "RoboToy", EXPORT_DIRECTORY);

        assembler.addCostumePackage(
            "lerry_he",
            "Lerry",
            "Slot_Char_Headgear",
            65959567,
            new String[] { "_SCALP", "_BROW" },
            null,
            "F:/KART/LERRY.GLB",
            DISABLE_TONGUE
        );

        // assembler.save("Lerry", "RoboToy", EXPORT_DIRECTORY);

        assembler.addCostumePackage(
            "videogameday_he",
            "Videogame Day Head",
            "Slot_Char_Headgear",
            65959567,
            new String[] { "_SCALP", "_BROW" },
            null,
            "F:/KART/PACKAGES/VIDEOGAMEDAY/VIDEOGAMEDAY_HE.GLB",
            DISABLE_TONGUE
        );

        assembler.addCostumePackage(
            "videogameday_hd",
            "Videogame Day Gloves",
            "Slot_Char_Hands",
            -147077727,
            new String[] { "_GLOVEL", "_GLOVER" },
            null,
            "F:/KART/PACKAGES/VIDEOGAMEDAY/VIDEOGAMEDAY_HD.GLB"
        );

        assembler.addCostumePackage(
            "videogameday_ft",
            "Videogame Day Shoes",
            "Slot_Char_Shoes",
            -1267069371,
            new String[] { "_SOCKS" },
            null,
            "F:/KART/PACKAGES/VIDEOGAMEDAY/VIDEOGAMEDAY_FT.GLB"
        );

        assembler.addCostumePackage(
            "videogameday_lg",
            "Videogame Day Pants",
            "Slot_Char_Pants",
            193205195,
            new String[] { "_SHORTS", "_PANTS", "_LEGS" },
            null,
            "F:/KART/PACKAGES/VIDEOGAMEDAY/VIDEOGAMEDAY_LG.GLB"
        );

        assembler.addCostumePackage(
            "videogameday_to",
            "Videogame Day Suit",
            "Slot_Char_Shirt",
            193860055,
            new String[] { "_TORSO", "_TORSO1", "_SLEEVER", "_SLEEVEL", "_ARML", "_ARMR" },
            null,
            "F:/KART/PACKAGES/VIDEOGAMEDAY/VIDEOGAMEDAY_TO.GLB",
            DISABLE_ZIPPER
        );

        // assembler.save("VideoGameDay", "RoboToy", EXPORT_DIRECTORY);

        assembler.save("CharCache", "RoboToy", EXPORT_DIRECTORY);
    }
}
