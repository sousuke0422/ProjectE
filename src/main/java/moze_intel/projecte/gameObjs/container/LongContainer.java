package moze_intel.projecte.gameObjs.container;

import net.minecraft.inventory.Container;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public abstract class LongContainer extends Container {

    @SideOnly(Side.CLIENT)
    public void updateProgressBarLong(int id, long data) {}
}
