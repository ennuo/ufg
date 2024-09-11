package ufg.resources;

import java.io.File;

import ufg.io.Serializable;
import ufg.io.Serializer;
import ufg.io.streams.MemoryInputStream;
import ufg.structures.chunks.ResourceData;
import ufg.util.Bytes;
import ufg.util.FileIO;
import ufg.util.UFGCRC;

public class CreationMetadata extends ResourceData {
    public static final int BASE_ALLOCATION_SIZE = 
        ResourceData.BASE_ALLOCATION_SIZE + 0x80;

    public int[] drmHashes, keywordHashes;

    public int creationID;
    public String creationName;
    public String creator;

    // CreationMetaDataV2 = 0xa81e71a6
    // CreationMetaDataV3 = 0xacdf6c11
    // CreationMetaDataV4 = 0xb2983c14
    // CreationMetaDataV5 = 0xb65921a3
    // CreationMetaDataV6 = 0xbb1a077a
    // CreationMetaDataV7 = 0xbfdb1acd
    // CreationMetaDataV8 = 0x8794a770
    // CreationMetaDataV9 = 0x8355bac7
    // CreationMetaDataV10 = 0x6f8f2ff5
    // CreationMetaDataV11 = 0x6b4e3242
    // CreationMetaDataV12 = 0x660d149b
    // CreationMetaDataV13 = 0x62cc092c

    // BINARY -> STRUCT
    // 0x50 -> 0xc
    // 0x74 -> 0x138
    // 0x54 -> 0x10
    // 0x58 -> 0xa0
    // 

    // CreationMetaDataV7
    // super qFileResource
    // + 0x40 = SavFileChecksum64
    // + 0x48 = Checksum64? (0x40 bytes, 0 this hash when calcing)
    // + 0x50 = CreationId
    // + 0x54 = ????
    // + 0x58 = ????
    // + 0x5c = ????
    // + 0x60 = ????
    // + 0x64 = ????
    // + 0x68 = ????
    // + 0x6C = DrmHashCount?
    // + 0x70 = KeywordHashCount?
    // + 0x74 = ???
    // + 0x78 = PublishedState
    // + 0x7C = u8 flags?
    // + 0x80 = DRMHashes
    // + 0x84 = KeywordHashes
    // + 0x88 = Name
    // + 0x8C = Description
    // + 0x90 = CreatorName
    // + 0x94 = ParentName
    // + 0x98 = ParentPlayerName
    // + 0x9c = OriginalCreatorName
    // + 0xa0 = RegionCode



    // 0 = LOCAL
    // 1 = ROM
    // 2 = UPLOAD
    // 3 = DOWNLOAD
    // 4 = DLC
    // 5 = REMOTE

    // If PublishedState is not 1, checks + 0x40 to see if it matches some hash?

    public static long GetMetadataChecksum(byte[] data, boolean verifyChecksum) {
        MemoryInputStream stream = new MemoryInputStream(data);
        stream.seek(0x58);
        long csum = stream.i64();
        for (int i = 0x58; i < 0x60; ++i) data[i] = 0;
        stream.seek(0x7c);
        int creationKeyCount = stream.i32();
        int drmKeyCount = stream.i32();
        stream.seek(0x10 + 0x40);
        long crc = UFGCRC.qFileHash64(stream.bytes(0x40), 0xFFFFFFFFFFFFFFFFL);
        int[] offsets = new int[9];
        for (int i = 0; i < offsets.length; ++i)
            offsets[i] = stream.getOffset() + stream.i32();

        stream.seek(offsets[0]);
        crc = UFGCRC.qFileHash64(stream.bytes(creationKeyCount * 0x4), crc);
        stream.seek(offsets[1]);
        crc = UFGCRC.qFileHash64(stream.bytes(drmKeyCount * 0x4), crc);
        for (int i = 2; i < offsets.length; ++i) {
            stream.seek(offsets[i]);
            while (true) {
                byte b = stream.i8();
                if (b == 0) break;
                crc = crc >>> 8 ^ UFGCRC.CRC_TABLE_64[(int) (((crc ^ b) & 0xff))]; 
            }
        }

        if (verifyChecksum && (csum != crc)) 
            throw new RuntimeException("MISMATCH!");

        return crc;
    }

    @SuppressWarnings("unchecked")
    @Override public CreationMetadata serialize(Serializer serializer, Serializable structure) {
        CreationMetadata details = (structure == null) ? new CreationMetadata() : (CreationMetadata) structure;

        super.serialize(serializer, details);

        MemoryInputStream stream = serializer.getInput();
        stream.forward(0x20);
        this.creationID = stream.i32();
        stream.forward(0x44);
        int nameOffset = stream.i32() - 4;
        stream.forward(nameOffset);
        details.creationName = stream.cstr();
        stream.cstr();
        details.creator = stream.cstr();

        return details;
    }

    public static void main(String[] args) {

        File[] files = new File("E:\\emu\\rpcs3\\dev_hdd0\\game\\BCUS98167_UCC\\USRDIR\\1\\DATA\\DOWNLOAD\\TRACK").listFiles();
        System.out.println(files.length);
        File output = new File("E:\\emu\\rpcs3\\dev_hdd0\\game\\BCUS98167_UCC\\USRDIR\\1\\DATA\\LOCAL\\TRACK");
        int id = 6000;
        for (File file : files) {
            if (!file.getAbsolutePath().endsWith(".CMD")) continue;


            String oldFilePath = file.getPath().split("[.]")[0];
            String fileName = file.getName().split("[.]")[0];
            String name = fileName.split("_")[1];

            byte[] data = FileIO.read(file.getAbsolutePath());
            GetMetadataChecksum(data, true);

            data[0x8b] = 0x0;

            data[0x60] = (byte)(id >> 24);
            data[0x61] = (byte)(id >> 16);
            data[0x62] = (byte)(id >> 8);
            data[0x63] = (byte)(id >> 0);

            long crc = GetMetadataChecksum(data, false);

            data[0x58] = (byte) (crc >>> 56);
            data[0x59] = (byte) (crc >>> 48);
            data[0x5a] = (byte) (crc >>> 40);
            data[0x5b] = (byte) (crc >>> 32);
            data[0x5c] = (byte) (crc >>> 24);
            data[0x5d] = (byte) (crc >>> 16);
            data[0x5e] = (byte) (crc >>> 8);
            data[0x5f] = (byte) (crc >>> 0);

            String newFileName = new File(output, Bytes.toHex(id++) + "_" + name).getAbsolutePath();


            System.out.println(newFileName);

            FileIO.write(data, newFileName + ".CMD");
            String[] extensions = new String[] { ".PNG", ".TRK", "_SMALL.PNG" };
            for (String extension : extensions) {
                File f = new File(oldFilePath + extension);
                if (f.exists())
                    FileIO.write(FileIO.read(f.getAbsolutePath()), newFileName + extension);
            }
        }
    }
}
