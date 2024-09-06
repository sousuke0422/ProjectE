package moze_intel.projecte.gameObjs.items.itemEntities;

import net.minecraft.client.renderer.texture.IIconRegister;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import moze_intel.projecte.gameObjs.items.ItemPE;

public class LavaOrb extends ItemPE {

    public LavaOrb() {
        this.setCreativeTab(null);
        this.setUnlocalizedName("lava_orb");
        this.setMaxStackSize(1);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister register) {
        this.itemIcon = register.registerIcon(this.getTexture("entities", "lava_orb"));
    }
}
