package moze_intel.projecte.gameObjs;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import moze_intel.projecte.PECore;

public class CreativeTab extends CreativeTabs {

    public CreativeTab() {
        super(PECore.MODID);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public Item getTabIconItem() {
        return ObjHandler.philosStone;
    }
}
