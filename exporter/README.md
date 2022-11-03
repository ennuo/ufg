# UFG Exporter

CLI utility for exporting model and mesh data.

## Usage

To export just model data:
```bash
java -jar ufg.jar -g <game> <...model_packs> -o <output_folder>
```

To export model data w/ textures:
```bash
java -jar ufg.jar -g <game> <...model_packs> -o <output_folder> -tp <perm.bin> <temp.bin>
```

To export model data w/ textures (indexed texture stream):
```
java -jar ufg.jar -g <game> <...model_packs> -o <output_folder> -tps <perm.bin> <perm.idx>
```

To just export a texture pack:
```bash
java -jar ufg.jar -g <game> -o <output_folder> -tps <perm.bin> <perm.idx> --dump-textures
java -jar ufg.jar -g <game> -o <output_folder> -tp <perm.bin> <temp.bin> --dump-textures
```

**NOTE: You may specify multiple texture packs and model packs by repeating respective arguments.**

## Arguments
        --game, -g <game>
        Specifies which game source data is from.
        Supported games are mnr/modnation and karting/lbpk

        --output, -o <output_folder>
        Specifies what folder to store output

        --texture-pack, -tp <perm.bin> <temp.bin>
        Loads a texture pack

        --texture-pack-stream, -tps <perm.bin> <.idx>
        Loads an indexed texture pack stream

        --dump-textures, -d
        Dumps all texture packs to PNGs