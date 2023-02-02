package moze_intel.projecte.gameObjs.gui;

import moze_intel.projecte.PECore;
import moze_intel.projecte.gameObjs.container.RelayMK1Container;
import moze_intel.projecte.gameObjs.tiles.RelayMK1Tile;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;

import org.lwjgl.opengl.GL11;

public class GUIRelayMK1 extends GuiContainer {

    private static final ResourceLocation texture = new ResourceLocation(
            PECore.MODID.toLowerCase(),
            "textures/gui/relay1.png");
    private RelayMK1Tile tile;

    public GUIRelayMK1(InventoryPlayer invPlayer, RelayMK1Tile tile) {
        super(new RelayMK1Container(invPlayer, tile));
        this.tile = tile;
        this.xSize = 175;
        this.ySize = 176;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int var1, int var2) {
        this.fontRendererObj.drawString(StatCollector.translateToLocal("pe.relay.mk1"), 10, 6, 4210752);
        this.fontRendererObj.drawString(Integer.toString(tile.displayEmc), 88, 24, 4210752);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float var1, int var2, int var3) {
        GL11.glColor4f(1F, 1F, 1F, 1F);
        Minecraft.getMinecraft().renderEngine.bindTexture(texture);

        int x = (width - xSize) / 2;
        int y = (height - ySize) / 2;

        this.drawTexturedModalRect(x, y, 0, 0, xSize, ySize);

        // Emc bar progress. Max is 102.
        int progress = tile.getEmcScaled(102);
        this.drawTexturedModalRect(x + 64, y + 6, 30, 177, progress, 10);

        // Klein start bar progress. Max is 30.
        progress = tile.getChargingEMCScaled(30);
        this.drawTexturedModalRect(x + 116, y + 67, 0, 177, progress, 10);

        // Burn Slot bar progress. Max is 30.
        progress = tile.getRawEmcScaled(30);
        drawTexturedModalRect(x + 64, y + 67, 0, 177, progress, 10);
    }
}
