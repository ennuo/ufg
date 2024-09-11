package ufg.resources;

import ufg.io.streams.MemoryInputStream;
import ufg.util.Bytes;

public class Attributes {
    // mMaxAttributes
    // mMaxDataSize
    // mNumAttributes
    // mDataSize - size of the data at the end of the buffer
    // 0x14 bytes - used in memory, doesnt matter, zero initialize it
    // mAttributes
        // each is like 0x14 bytes?
            // int searchUid
            // int parentUid
            // int nameUid
            // int typeUid
            // int dataOffset

        // WorldSection
            // byte len
            // char[len] str
            // align to 0x10 bytes (0x18 byte structure)
                // int uid
                // 0x10 bytes of nothing
                // int ? (2)

        // WorldModel
            // byte model_string_offset
            // byte string1_offset [null]
            // byte string2_offset [null]
            // byte model_uid_offset
            // char[] string
            // char[] string1
            // char[] string2
            // int uid

        // ModelResource
            // int modelUID
            // int uid_count
            // int[uid_count] texture_uids

        // SubmodelResource
            // int modelUID
            // int uid_count
            // int[uid_count] submodel_uids


    public static void main(String[] args) {
        MemoryInputStream stream = new MemoryInputStream("CHUNKS.BIN");
        stream.seek(0x50);
        int maxAttributes = stream.i32();
        int maxDataSize = stream.i32();
        int numAttributes = stream.i32();
        int dataSize = stream.i32();

        stream.forward(0x14);

        for (int j = 0; j < 4; ++j) {
            System.out.println();
            System.out.println("START SECTION");
            System.out.println();

            for (int i = 0; i < numAttributes; ++i) {
                // SectionChunk
                stream.i32(); // searchUid
                stream.i32(); // parentUid
                stream.i32(); // nameUid
                int typeUid = stream.i32();
                int dataOffset = stream.i32();
            }

            System.out.println();
            System.out.println("END SECTION");
            System.out.println();
        }

        System.out.println(stream.getOffset());


    }
}
