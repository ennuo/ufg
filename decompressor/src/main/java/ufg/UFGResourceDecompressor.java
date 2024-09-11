package ufg;

import java.io.File;

import ufg.util.DecompressLZ;
import ufg.util.FileIO;

public class UFGResourceDecompressor {
    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("UFG Resource Decompressor by ennuo\n");
            System.out.println("Usage:");

            System.out.println("java -jar decompress.jar <.INPUT.BIN> <.OUTPUT.BIN>");

            return;
        }

        if (!new File(args[0]).exists()) {
            System.err.println(args[0] + " does not exist!");
            return;
        }

        byte[] decompressed = FileIO.read(args[0]);
        if (decompressed == null) {
            System.err.println("Failed to read file!");
            return;
        }

        try { decompressed = DecompressLZ.decompress(decompressed); }
        catch (IllegalArgumentException ex) {
            System.err.println("File was not quick compressed!");
            return;
        }
        catch (Exception ex) {
            System.err.println("Some error occurred during decompression!");
            return;
        }

        FileIO.write(decompressed, args[1]);
    }
}
