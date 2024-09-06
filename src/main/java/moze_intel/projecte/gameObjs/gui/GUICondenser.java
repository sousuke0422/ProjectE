package moze_intel.projecte.gameObjs.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import moze_intel.projecte.PECore;
import moze_intel.projecte.gameObjs.container.CondenserContainer;
import moze_intel.projecte.gameObjs.tiles.CondenserTile;

public class GUICondenser extends GuiContainer {

    private static final ResourceLocation texture = new ResourceLocation(
        PECore.MODID.toLowerCase(),
        "textures/gui/condenser.png");
    private CondenserTile tile;

    public GUICondenser(InventoryPlayer invPlayer, CondenserTile tile) {
        super(new CondenserContainer(invPlayer, tile));
        this.tile = tile;
        this.xSize = 255;
        this.ySize = 233;
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float var1, int var2, int var3) {
        GL11.glColor4f(1F, 1F, 1F, 1F);
        Minecraft.getMinecraft().renderEngine.bindTexture(texture);

        int x = (width - xSize) / 2;
        int y = (height - ySize) / 2;

        this.drawTexturedModalRect(x, y, 0, 0, xSize, ySize);

        int progress = tile.getProgressScaled();
        this.drawTexturedModalRect(x + 33, y + 10, 0, 235, progress, 10);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int var1, int var2) {
        int toDisplay = tile.displayEmc > tile.requiredEmc ? tile.requiredEmc : tile.displayEmc;
        this.fontRendererObj.drawString(Integer.toString(toDisplay), 140, 10, 4210752);
    }
}
