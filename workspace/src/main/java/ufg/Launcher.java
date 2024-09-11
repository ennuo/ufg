package ufg;

import ufg.workspace.Launchable;
import ufg.workspace.SceneExtractor;

public class Launcher {
    public static void main(String[] args)
    {
        // args = new String[] 
        // {
        //     "E:\\emu\\rpcs3\\dev_hdd0\\game\\LBPKPROTO\\USRDIR\\DATA\\WORLD\\TRACKSTUDIO2\\ENVIRONMENT\\ENV_ROOM\\",
        //     "E:\\work\\backgrounds\\ENV_ROOM"
        // };
        // Launchable launchable = new Chunkinator();
        args = new String[] 
        {
            "C:\\\\Users\\\\Aidan\\\\Desktop\\\\SCENECHUNKS.BIN",
            "C:\\Users\\Aidan\\Desktop\\TEST.JSON"
        };
        Launchable launchable = new SceneExtractor();
        if (launchable.validate(args))
            launchable.launch(args);
    }
}
