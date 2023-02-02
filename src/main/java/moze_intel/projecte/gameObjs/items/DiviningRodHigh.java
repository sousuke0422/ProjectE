package moze_intel.projecte.gameObjs.items;

import net.minecraft.client.renderer.texture.IIconRegister;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class DiviningRodHigh extends DiviningRodMedium {

    public DiviningRodHigh() {
        super(new String[] { "3x3x3", "16x3x3", "64x3x3" });
        this.setUnlocalizedName("divining_rod_3");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister register) {
        this.itemIcon = register.registerIcon(this.getTexture("divining3"));
    }
}
