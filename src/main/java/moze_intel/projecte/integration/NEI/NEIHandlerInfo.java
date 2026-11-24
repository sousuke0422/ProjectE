package moze_intel.projecte.integration.NEI;

import net.minecraft.nbt.NBTTagCompound;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.event.FMLInterModComms;

/**
 * Tells NEI (GTNH) how to present recipe pages it does not already know from its own handler list: the tab icon, and
 * the machines listed beside the page. Sent as IMC, so this class must not touch any NEI class.
 */
public final class NEIHandlerInfo {

    private static final String NEI = "NotEnoughItems";
    private static final String COLLECTOR_FUEL = "moze_intel.projecte.integration.NEI.NEICollectorFuelHandler";

    private NEIHandlerInfo() {}

    /** Has to run during init: NEI reads these messages before post-init. */
    public static void sendIMC() {
        if (!Loader.isModLoaded(NEI)) {
            return;
        }

        // Same icon as the collector category of current ProjectE versions: the MK1 collector
        NBTTagCompound info = new NBTTagCompound();
        info.setString("handler", COLLECTOR_FUEL);
        info.setString("modName", "ProjectE");
        info.setString("modId", "ProjectE");
        info.setBoolean("modRequired", true);
        info.setString("itemName", "ProjectE:collector_mk1");
        info.setInteger("handlerHeight", 65);
        info.setInteger("maxRecipesPerPage", 4);
        FMLInterModComms.sendMessage(NEI, "registerHandlerInfo", info);

        String[] collectors = { "ProjectE:collector_mk1", "ProjectE:collector_mk2", "ProjectE:collector_mk3" };
        for (int i = 0; i < collectors.length; i++) {
            NBTTagCompound catalyst = new NBTTagCompound();
            // Catalysts are keyed by the id the page is opened with, not by the class
            catalyst.setString("handlerID", "collectorFuelUpgrade");
            catalyst.setString("itemName", collectors[i]);
            catalyst.setInteger("priority", -i);
            FMLInterModComms.sendMessage(NEI, "registerCatalystInfo", catalyst);
        }
    }
}
