package xyz.akirin.fmpe;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = FMPEMain.ID, name = FMPEMain.Name, version = FMPEMain.Version)
public class FMPEMain {
    static final String ID = "FMPE";
    static final String Name = "FMProjectE";
    static final String Version = "core:1.2.0, codename:none";

    //public static final String RVersion = "@VERSION@";
    public static final String RVersion = "2.0.0";

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        FMPELogger.logInfo("##########FMProjectE Information##########");
        FMPELogger.logInfo("Mod ID: " + ID);
        FMPELogger.logInfo("Mod Name: " + Name);
        FMPELogger.logInfo("Version: " + Version );
        FMPELogger.logInfo("ReleaseVersion: " + RVersion );
        FMPELogger.logInfo("##########################################");
        FMPELogger.logDebug("##########FMProjectE SystemCheck##########");
        FMPELogger.logDebug("Integer: " + Integer.MAX_VALUE);
        FMPELogger.logDebug("Long: " + Long.MAX_VALUE);
        FMPELogger.logDebug("Float: " + Float.MAX_VALUE);
        FMPELogger.logDebug("Double: " + Double.MAX_VALUE);
        FMPELogger.logDebug("##########################################");
    }
}