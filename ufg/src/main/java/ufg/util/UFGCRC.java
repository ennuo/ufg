package ufg.util;

public class UFGCRC {
    public static final int[] CRC_TABLE_32 = new int[] {
        0x00000000, 0x04C11DB7, 0x09823B6E, 0x0D4326D9, 0x130476DC,
        0x17C56B6B, 0x1A864DB2, 0x1E475005, 0x2608EDB8, 0x22C9F00F,
        0x2F8AD6D6, 0x2B4BCB61, 0x350C9B64, 0x31CD86D3, 0x3C8EA00A,
        0x384FBDBD, 0x4C11DB70, 0x48D0C6C7, 0x4593E01E, 0x4152FDA9,
        0x5F15ADAC, 0x5BD4B01B, 0x569796C2, 0x52568B75, 0x6A1936C8,
        0x6ED82B7F, 0x639B0DA6, 0x675A1011, 0x791D4014, 0x7DDC5DA3,
        0x709F7B7A, 0x745E66CD, 0x9823B6E0, 0x9CE2AB57, 0x91A18D8E,
        0x95609039, 0x8B27C03C, 0x8FE6DD8B, 0x82A5FB52, 0x8664E6E5,
        0xBE2B5B58, 0xBAEA46EF, 0xB7A96036, 0xB3687D81, 0xAD2F2D84,
        0xA9EE3033, 0xA4AD16EA, 0xA06C0B5D, 0xD4326D90, 0xD0F37027,
        0xDDB056FE, 0xD9714B49, 0xC7361B4C, 0xC3F706FB, 0xCEB42022,
        0xCA753D95, 0xF23A8028, 0xF6FB9D9F, 0xFBB8BB46, 0xFF79A6F1,
        0xE13EF6F4, 0xE5FFEB43, 0xE8BCCD9A, 0xEC7DD02D, 0x34867077,
        0x30476DC0, 0x3D044B19, 0x39C556AE, 0x278206AB, 0x23431B1C,
        0x2E003DC5, 0x2AC12072, 0x128E9DCF, 0x164F8078, 0x1B0CA6A1,
        0x1FCDBB16, 0x018AEB13, 0x054BF6A4, 0x0808D07D, 0x0CC9CDCA,
        0x7897AB07, 0x7C56B6B0, 0x71159069, 0x75D48DDE, 0x6B93DDDB,
        0x6F52C06C, 0x6211E6B5, 0x66D0FB02, 0x5E9F46BF, 0x5A5E5B08,
        0x571D7DD1, 0x53DC6066, 0x4D9B3063, 0x495A2DD4, 0x44190B0D,
        0x40D816BA, 0xACA5C697, 0xA864DB20, 0xA527FDF9, 0xA1E6E04E,
        0xBFA1B04B, 0xBB60ADFC, 0xB6238B25, 0xB2E29692, 0x8AAD2B2F,
        0x8E6C3698, 0x832F1041, 0x87EE0DF6, 0x99A95DF3, 0x9D684044,
        0x902B669D, 0x94EA7B2A, 0xE0B41DE7, 0xE4750050, 0xE9362689,
        0xEDF73B3E, 0xF3B06B3B, 0xF771768C, 0xFA325055, 0xFEF34DE2,
        0xC6BCF05F, 0xC27DEDE8, 0xCF3ECB31, 0xCBFFD686, 0xD5B88683,
        0xD1799B34, 0xDC3ABDED, 0xD8FBA05A, 0x690CE0EE, 0x6DCDFD59,
        0x608EDB80, 0x644FC637, 0x7A089632, 0x7EC98B85, 0x738AAD5C,
        0x774BB0EB, 0x4F040D56, 0x4BC510E1, 0x46863638, 0x42472B8F,
        0x5C007B8A, 0x58C1663D, 0x558240E4, 0x51435D53, 0x251D3B9E,
        0x21DC2629, 0x2C9F00F0, 0x285E1D47, 0x36194D42, 0x32D850F5,
        0x3F9B762C, 0x3B5A6B9B, 0x0315D626, 0x07D4CB91, 0x0A97ED48,
        0x0E56F0FF, 0x1011A0FA, 0x14D0BD4D, 0x19939B94, 0x1D528623,
        0xF12F560E, 0xF5EE4BB9, 0xF8AD6D60, 0xFC6C70D7, 0xE22B20D2,
        0xE6EA3D65, 0xEBA91BBC, 0xEF68060B, 0xD727BBB6, 0xD3E6A601,
        0xDEA580D8, 0xDA649D6F, 0xC423CD6A, 0xC0E2D0DD, 0xCDA1F604,
        0xC960EBB3, 0xBD3E8D7E, 0xB9FF90C9, 0xB4BCB610, 0xB07DABA7,
        0xAE3AFBA2, 0xAAFBE615, 0xA7B8C0CC, 0xA379DD7B, 0x9B3660C6,
        0x9FF77D71, 0x92B45BA8, 0x9675461F, 0x8832161A, 0x8CF30BAD,
        0x81B02D74, 0x857130C3, 0x5D8A9099, 0x594B8D2E, 0x5408ABF7,
        0x50C9B640, 0x4E8EE645, 0x4A4FFBF2, 0x470CDD2B, 0x43CDC09C,
        0x7B827D21, 0x7F436096, 0x7200464F, 0x76C15BF8, 0x68860BFD,
        0x6C47164A, 0x61043093, 0x65C52D24, 0x119B4BE9, 0x155A565E,
        0x18197087, 0x1CD86D30, 0x029F3D35, 0x065E2082, 0x0B1D065B,
        0x0FDC1BEC, 0x3793A651, 0x3352BBE6, 0x3E119D3F, 0x3AD08088,
        0x2497D08D, 0x2056CD3A, 0x2D15EBE3, 0x29D4F654, 0xC5A92679,
        0xC1683BCE, 0xCC2B1D17, 0xC8EA00A0, 0xD6AD50A5, 0xD26C4D12,
        0xDF2F6BCB, 0xDBEE767C, 0xE3A1CBC1, 0xE760D676, 0xEA23F0AF,
        0xEEE2ED18, 0xF0A5BD1D, 0xF464A0AA, 0xF9278673, 0xFDE69BC4,
        0x89B8FD09, 0x8D79E0BE, 0x803AC667, 0x84FBDBD0, 0x9ABC8BD5,
        0x9E7D9662, 0x933EB0BB, 0x97FFAD0C, 0xAFB010B1, 0xAB710D06,
        0xA6322BDF, 0xA2F33668, 0xBCB4666D, 0xB8757BDA, 0xB5365D03,
        0xB1F740B4
    };

    public static final long[] CRC_TABLE_64 = new long[] {
        0x0L, 0x7ad870c830358979L, 0xf5b0e190606b12f2L, 0x8f689158505e9b8bL, 0xc038e5739841b68fL, 0xbae095bba8743ff6L,
        0x358804e3f82aa47dL, 0x4f50742bc81f2d04L, 0xab28ecb46814fe75L, 0xd1f09c7c5821770cL, 0x5e980d24087fec87L,
        0x24407dec384a65feL, 0x6b1009c7f05548faL, 0x11c8790fc060c183L, 0x9ea0e857903e5a08L, 0xe478989fa00bd371L,
        0x7d08ff3b88be6f81L, 0x7d08ff3b88be6f8L, 0x88b81eabe8d57d73L, 0xf2606e63d8e0f40aL, 0xbd301a4810ffd90eL,
        0xc7e86a8020ca5077L, 0x4880fbd87094cbfcL, 0x32588b1040a14285L, 0xd620138fe0aa91f4L, 0xacf86347d09f188dL,
        0x2390f21f80c18306L, 0x594882d7b0f40a7fL, 0x1618f6fc78eb277bL, 0x6cc0863448deae02L, 0xe3a8176c18803589L,
        0x997067a428b5bcf0L, 0xfa11fe77117cdf02L, 0x80c98ebf2149567bL, 0xfa11fe77117cdf0L, 0x75796f2f41224489L,
        0x3a291b04893d698dL, 0x40f16bccb908e0f4L, 0xcf99fa94e9567b7fL, 0xb5418a5cd963f206L, 0x513912c379682177L,
        0x2be1620b495da80eL, 0xa489f35319033385L, 0xde51839b2936bafcL, 0x9101f7b0e12997f8L, 0xebd98778d11c1e81L,
        0x64b116208142850aL, 0x1e6966e8b1770c73L, 0x8719014c99c2b083L, 0xfdc17184a9f739faL, 0x72a9e0dcf9a9a271L,
        0x8719014c99c2b08L, 0x4721e43f0183060cL, 0x3df994f731b68f75L, 0xb29105af61e814feL, 0xc849756751dd9d87L,
        0x2c31edf8f1d64ef6L, 0x56e99d30c1e3c78fL, 0xd9810c6891bd5c04L, 0xa3597ca0a188d57dL, 0xec09088b6997f879L,
        0x96d1784359a27100L, 0x19b9e91b09fcea8bL, 0x636199d339c963f2L, 0xdf7adabd7a6e2d6fL, 0xa5a2aa754a5ba416L,
        0x2aca3b2d1a053f9dL, 0x50124be52a30b6e4L, 0x1f423fcee22f9be0L, 0x659a4f06d21a1299L, 0xeaf2de5e82448912L,
        0x902aae96b271006bL, 0x74523609127ad31aL, 0xe8a46c1224f5a63L, 0x81e2d7997211c1e8L, 0xfb3aa75142244891L,
        0xb46ad37a8a3b6595L, 0xceb2a3b2ba0eececL, 0x41da32eaea507767L, 0x3b024222da65fe1eL, 0xa2722586f2d042eeL,
        0xd8aa554ec2e5cb97L, 0x57c2c41692bb501cL, 0x2d1ab4dea28ed965L, 0x624ac0f56a91f461L, 0x1892b03d5aa47d18L,
        0x97fa21650afae693L, 0xed2251ad3acf6feaL, 0x95ac9329ac4bc9bL, 0x7382b9faaaf135e2L, 0xfcea28a2faafae69L,
        0x8632586aca9a2710L, 0xc9622c4102850a14L, 0xb3ba5c8932b0836dL, 0x3cd2cdd162ee18e6L, 0x460abd1952db919fL,
        0x256b24ca6b12f26dL, 0x5fb354025b277b14L, 0xd0dbc55a0b79e09fL, 0xaa03b5923b4c69e6L, 0xe553c1b9f35344e2L,
        0x9f8bb171c366cd9bL, 0x10e3202993385610L, 0x6a3b50e1a30ddf69L, 0x8e43c87e03060c18L, 0xf49bb8b633338561L,
        0x7bf329ee636d1eeaL, 0x12b592653589793L, 0x4e7b2d0d9b47ba97L, 0x34a35dc5ab7233eeL, 0xbbcbcc9dfb2ca865L,
        0xc113bc55cb19211cL, 0x5863dbf1e3ac9decL, 0x22bbab39d3991495L, 0xadd33a6183c78f1eL, 0xd70b4aa9b3f20667L,
        0x985b3e827bed2b63L, 0xe2834e4a4bd8a21aL, 0x6debdf121b863991L, 0x1733afda2bb3b0e8L, 0xf34b37458bb86399L,
        0x8993478dbb8deae0L, 0x6fbd6d5ebd3716bL, 0x7c23a61ddbe6f812L, 0x3373d23613f9d516L, 0x49aba2fe23cc5c6fL,
        0xc6c333a67392c7e4L, 0xbc1b436e43a74e9dL, 0x95ac9329ac4bc9b5L, 0xef74e3e19c7e40ccL, 0x601c72b9cc20db47L,
        0x1ac40271fc15523eL, 0x5594765a340a7f3aL, 0x2f4c0692043ff643L, 0xa02497ca54616dc8L, 0xdafce7026454e4b1L,
        0x3e847f9dc45f37c0L, 0x445c0f55f46abeb9L, 0xcb349e0da4342532L, 0xb1eceec59401ac4bL, 0xfebc9aee5c1e814fL,
        0x8464ea266c2b0836L, 0xb0c7b7e3c7593bdL, 0x71d40bb60c401ac4L, 0xe8a46c1224f5a634L, 0x927c1cda14c02f4dL,
        0x1d148d82449eb4c6L, 0x67ccfd4a74ab3dbfL, 0x289c8961bcb410bbL, 0x5244f9a98c8199c2L, 0xdd2c68f1dcdf0249L,
        0xa7f41839ecea8b30L, 0x438c80a64ce15841L, 0x3954f06e7cd4d138L, 0xb63c61362c8a4ab3L, 0xcce411fe1cbfc3caL,
        0x83b465d5d4a0eeceL, 0xf96c151de49567b7L, 0x76048445b4cbfc3cL, 0xcdcf48d84fe7545L, 0x6fbd6d5ebd3716b7L,
        0x15651d968d029fceL, 0x9a0d8ccedd5c0445L, 0xe0d5fc06ed698d3cL, 0xaf85882d2576a038L, 0xd55df8e515432941L,
        0x5a3569bd451db2caL, 0x20ed197575283bb3L, 0xc49581ead523e8c2L, 0xbe4df122e51661bbL, 0x3125607ab548fa30L,
        0x4bfd10b2857d7349L, 0x4ad64994d625e4dL, 0x7e7514517d57d734L, 0xf11d85092d094cbfL, 0x8bc5f5c11d3cc5c6L,
        0x12b5926535897936L, 0x686de2ad05bcf04fL, 0xe70573f555e26bc4L, 0x9ddd033d65d7e2bdL, 0xd28d7716adc8cfb9L,
        0xa85507de9dfd46c0L, 0x273d9686cda3dd4bL, 0x5de5e64efd965432L, 0xb99d7ed15d9d8743L, 0xc3450e196da80e3aL,
        0x4c2d9f413df695b1L, 0x36f5ef890dc31cc8L, 0x79a59ba2c5dc31ccL, 0x37deb6af5e9b8b5L, 0x8c157a32a5b7233eL,
        0xf6cd0afa9582aa47L, 0x4ad64994d625e4daL, 0x300e395ce6106da3L, 0xbf66a804b64ef628L, 0xc5bed8cc867b7f51L,
        0x8aeeace74e645255L, 0xf036dc2f7e51db2cL, 0x7f5e4d772e0f40a7L, 0x5863dbf1e3ac9deL, 0xe1fea520be311aafL,
        0x9b26d5e88e0493d6L, 0x144e44b0de5a085dL, 0x6e963478ee6f8124L, 0x21c640532670ac20L, 0x5b1e309b16452559L,
        0xd476a1c3461bbed2L, 0xaeaed10b762e37abL, 0x37deb6af5e9b8b5bL, 0x4d06c6676eae0222L, 0xc26e573f3ef099a9L,
        0xb8b627f70ec510d0L, 0xf7e653dcc6da3dd4L, 0x8d3e2314f6efb4adL, 0x256b24ca6b12f26L, 0x788ec2849684a65fL,
        0x9cf65a1b368f752eL, 0xe62e2ad306bafc57L, 0x6946bb8b56e467dcL, 0x139ecb4366d1eea5L, 0x5ccebf68aecec3a1L,
        0x2616cfa09efb4ad8L, 0xa97e5ef8cea5d153L, 0xd3a62e30fe90582aL, 0xb0c7b7e3c7593bd8L, 0xca1fc72bf76cb2a1L,
        0x45775673a732292aL, 0x3faf26bb9707a053L, 0x70ff52905f188d57L, 0xa2722586f2d042eL, 0x854fb3003f739fa5L,
        0xff97c3c80f4616dcL, 0x1bef5b57af4dc5adL, 0x61372b9f9f784cd4L, 0xee5fbac7cf26d75fL, 0x9487ca0fff135e26L,
        0xdbd7be24370c7322L, 0xa10fceec0739fa5bL, 0x2e675fb4576761d0L, 0x54bf2f7c6752e8a9L, 0xcdcf48d84fe75459L,
        0xb71738107fd2dd20L, 0x387fa9482f8c46abL, 0x42a7d9801fb9cfd2L, 0xdf7adabd7a6e2d6L, 0x772fdd63e7936bafL,
        0xf8474c3bb7cdf024L, 0x829f3cf387f8795dL, 0x66e7a46c27f3aa2cL, 0x1c3fd4a417c62355L, 0x935745fc4798b8deL,
        0xe98f353477ad31a7L, 0xa6df411fbfb21ca3L, 0xdc0731d78f8795daL, 0x536fa08fdfd90e51L, 0x29b7d047efec8728L,
        0x4c11db7L, 0x9823b6e0d4326d9L, 0x130476dc17c56b6bL, 0x1a864db21e475005L, 0x2608edb822c9f00fL,
        0x2f8ad6d62b4bcb61L, 0x350c9b6431cd86d3L, 0x3c8ea00a384fbdbdL, 0x4c11db7048d0c6c7L, 0x4593e01e4152fda9L,
        0x5f15adac5bd4b01bL, 0x569796c252568b75L, 0x6a1936c86ed82b7fL, 0x639b0da6675a1011L, 0x791d40147ddc5da3L,
        0x709f7b7a745e66cdL, 0x9823b6e09ce2ab57L, 0x91a18d8e95609039L, 0x8b27c03c8fe6dd8bL, 0x82a5fb528664e6e5L,
        0xbe2b5b58baea46efL, 0xb7a96036b3687d81L, 0xad2f2d84a9ee3033L, 0xa4ad16eaa06c0b5dL, 0xd4326d90d0f37027L,
        0xddb056fed9714b49L, 0xc7361b4cc3f706fbL, 0xceb42022ca753d95L, 0xf23a8028f6fb9d9fL, 0xfbb8bb46ff79a6f1L,
        0xe13ef6f4e5ffeb43L, 0xe8bccd9aec7dd02dL, 0x3486707730476dc0L, 0x3d044b1939c556aeL, 0x278206ab23431b1cL,
        0x2e003dc52ac12072L, 0x128e9dcf164f8078L, 0x1b0ca6a11fcdbb16L, 0x18aeb13054bf6a4L, 0x808d07d0cc9cdcaL,
        0x7897ab077c56b6b0L, 0x7115906975d48ddeL, 0x6b93dddb6f52c06cL, 0x6211e6b566d0fb02L, 0x5e9f46bf5a5e5b08L,
        0x571d7dd153dc6066L, 0x4d9b3063495a2dd4L, 0x44190b0d40d816baL, 0xaca5c697a864db20L, 0xa527fdf9a1e6e04eL,
        0xbfa1b04bbb60adfcL, 0xb6238b25b2e29692L, 0x8aad2b2f8e6c3698L, 0x832f104187ee0df6L, 0x99a95df39d684044L,
        0x902b669d94ea7b2aL, 0xe0b41de7e4750050L, 0xe9362689edf73b3eL, 0xf3b06b3bf771768cL, 0xfa325055fef34de2L,
        0xc6bcf05fc27dede8L, 0xcf3ecb31cbffd686L, 0xd5b88683d1799b34L, 0xdc3abdedd8fba05aL, 0x690ce0ee6dcdfd59L,
        0x608edb80644fc637L, 0x7a0896327ec98b85L, 0x738aad5c774bb0ebL, 0x4f040d564bc510e1L, 0x4686363842472b8fL,
        0x5c007b8a58c1663dL, 0x558240e451435d53L, 0x251d3b9e21dc2629L, 0x2c9f00f0285e1d47L, 0x36194d4232d850f5L,
        0x3f9b762c3b5a6b9bL, 0x315d62607d4cb91L, 0xa97ed480e56f0ffL, 0x1011a0fa14d0bd4dL, 0x19939b941d528623L,
        0xf12f560ef5ee4bb9L, 0xf8ad6d60fc6c70d7L, 0xe22b20d2e6ea3d65L, 0xeba91bbcef68060bL, 0xd727bbb6d3e6a601L,
        0xdea580d8da649d6fL, 0xc423cd6ac0e2d0ddL, 0xcda1f604c960ebb3L, 0xbd3e8d7eb9ff90c9L, 0xb4bcb610b07daba7L,
        0xae3afba2aafbe615L, 0xa7b8c0cca379dd7bL, 0x9b3660c69ff77d71L, 0x92b45ba89675461fL, 0x8832161a8cf30badL,
        0x81b02d74857130c3L, 0x5d8a9099594b8d2eL, 0x5408abf750c9b640L, 0x4e8ee6454a4ffbf2L, 0x470cdd2b43cdc09cL,
        0x7b827d217f436096L, 0x7200464f76c15bf8L, 0x68860bfd6c47164aL, 0x6104309365c52d24L, 0x119b4be9155a565eL,
        0x181970871cd86d30L, 0x29f3d35065e2082L, 0xb1d065b0fdc1becL, 0x3793a6513352bbe6L, 0x3e119d3f3ad08088L,
        0x2497d08d2056cd3aL, 0x2d15ebe329d4f654L, 0xc5a92679c1683bceL, 0xcc2b1d17c8ea00a0L, 0xd6ad50a5d26c4d12L,
        0xdf2f6bcbdbee767cL, 0xe3a1cbc1e760d676L, 0xea23f0afeee2ed18L, 0xf0a5bd1df464a0aaL, 0xf9278673fde69bc4L,
        0x89b8fd098d79e0beL, 0x803ac66784fbdbd0L, 0x9abc8bd59e7d9662L, 0x933eb0bb97ffad0cL, 0xafb010b1ab710d06L,
        0xa6322bdfa2f33668L, 0xbcb4666db8757bdaL, 0xb5365d03b1f740b4L
    };

    public static int qStringHash32(String str) {
        int crc = -1;
        for (int i = 0; i < str.length(); ++i)
            crc = crc << 8 ^ CRC_TABLE_32[(crc >> 0x18 ^ str.charAt(i)) & 0xff];
        return crc;
    }

    public static long qStringHash64(String str) {
        long crc = -1;
        for (int i = 0; i < str.length(); ++i)
            crc = crc >>> 8 ^ CRC_TABLE_64[(int) (((crc ^ str.charAt(i)) & 0xff))];
        return crc;
    }

    public static int qStringHashUpper32(String str) {
        return qStringHash32(str.toUpperCase());
    }

    public static long qStringHashUpper64(String str) {
        return qStringHash64(str.toUpperCase());
    }

    public static int qFileHash32(byte[] buffer) {
        int crc = -1;
        for (int i = 0; i < buffer.length; ++i)
            crc = crc << 8 ^ CRC_TABLE_32[(crc >> 0x18 ^ buffer[i]) & 0xff];
        return crc;
    }

    public static int qFileHash32(byte[] buffer, int crc) {
        if (buffer == null || buffer.length == 0) return crc;
        for (int i = 0; i < buffer.length; ++i)
            crc = crc << 8 ^ CRC_TABLE_32[(crc >> 0x18 ^ buffer[i]) & 0xff];
        return crc;
    }

    public static long qFileHash64(byte[] buffer) {
        long crc = -1;
        for (int i = 0; i < buffer.length; ++i)
            crc = crc >>> 8 ^ CRC_TABLE_64[(int) (((crc ^ buffer[i]) & 0xff))];        
        return crc;
    }

    public static long qFileHash64(byte[] buffer, long crc) {
        if (buffer == null || buffer.length == 0) return crc;
        for (int i = 0; i < buffer.length; ++i)
            crc = crc >>> 8 ^ CRC_TABLE_64[(int) (((crc ^ buffer[i]) & 0xff))];        
        return crc;
    }

    public static void main(String[] args) {
        System.out.println(Bytes.toHex(qStringHashUpper32("LBP_CHAR_MoustacheMan01")));
    }
}
