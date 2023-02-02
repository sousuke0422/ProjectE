package moze_intel.projecte.gameObjs.gui;

import moze_intel.projecte.PECore;
import moze_intel.projecte.gameObjs.container.EternalDensityContainer;
import moze_intel.projecte.gameObjs.container.inventory.EternalDensityInventory;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;

import org.lwjgl.opengl.GL11;

public class GUIEternalDensity extends GuiContainer {

    private static final ResourceLocation texture = new ResourceLocation(
            PECore.MODID.toLowerCase(),
            "textures/gui/eternal_density.png");
    private EternalDensityInventory inventory;

    public GUIEternalDensity(InventoryPlayer invPlayer, EternalDensityInventory invGem) {
        super(new EternalDensityContainer(invPlayer, invGem));

        this.inventory = invGem;

        this.xSize = 180;
        this.ySize = 180;
    }

    @Override
    public void initGui() {
        super.initGui();

        this.buttonList.add(
                new GuiButton(
                        1,
                        (width - xSize) / 2 + 62,
                        (height - ySize) / 2 + 4,
                        52,
                        20,
                        inventory.isWhitelistMode() ? "Whitelist" : "Blacklist"));
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        super.actionPerformed(button);

        inventory.changeMode();

        button.displayString = StatCollector
                .translateToLocal(inventory.isWhitelistMode() ? "pe.gemdensity.whitelist" : "pe.gemdensity.blacklist");
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float p_146976_1_, int p_146976_2_, int p_146976_3_) {
        GL11.glColor4f(1F, 1F, 1F, 1F);
        Minecraft.getMinecraft().renderEngine.bindTexture(texture);
        this.drawTexturedModalRect((width - xSize) / 2, (height - ySize) / 2, 0, 0, xSize, ySize);
    }
}
