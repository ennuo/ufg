package ufg.util;

import ufg.enums.GameVersion;

public class ExecutionContext {
    /**
     * Used to indicate that the serializer should expect
     * modnation resources.
     */
    public static GameVersion Version = GameVersion.Karting;
    public static boolean isModNation()
    {
        return Version == GameVersion.ModNation;
    }
    public static boolean isKarting()
    {
        return Version == GameVersion.Karting;
    }
    public static boolean isKartingMilestone()
    {
        return Version == GameVersion.KartingMilestone;
    }
}
