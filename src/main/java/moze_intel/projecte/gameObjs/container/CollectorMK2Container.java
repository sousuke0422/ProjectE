package moze_intel.projecte.gameObjs.container;

import javax.annotation.Nonnull;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import moze_intel.projecte.emc.FuelMapper;
import moze_intel.projecte.gameObjs.container.slots.collector.SlotCollectorInv;
import moze_intel.projecte.gameObjs.container.slots.collector.SlotCollectorLock;
import moze_intel.projecte.gameObjs.tiles.CollectorMK2Tile;
import moze_intel.projecte.network.PacketHandler;
import moze_intel.projecte.network.packets.CollectorSyncPKT;

public class CollectorMK2Container extends LongContainer {

    final CollectorMK2Tile tile;
    public int sunLevel = 0;
    public long emc = 0;
    public double kleinChargeProgress = 0;
    public double fuelProgress = 0;
    public int kleinEmc = 0;
    private long lastDisplayEmc = Long.MIN_VALUE;
    private double lastItemCharge = Double.NaN;

    public CollectorMK2Container(InventoryPlayer invPlayer, CollectorMK2Tile collector) {
        this.tile = collector;
        tile.openInventory();

        // Klein Star Slot
        this.addSlotToContainer(new SlotCollectorInv(tile, 0, 140, 58));

        // Fuel Upgrade Slot
        for (int i = 0; i < 3; i++) for (int j = 0; j < 4; j++)
            this.addSlotToContainer(new SlotCollectorInv(tile, i * 4 + j + 1, 18 + i * 18, 8 + j * 18));

        // Upgrade Result
        this.addSlotToContainer(new Slot(tile, 13, 140, 13));

        // Upgrade Target
        this.addSlotToContainer(new SlotCollectorLock(tile, 14, 169, 36));

        // Player inventory
        for (int i = 0; i < 3; i++) for (int j = 0; j < 9; j++)
            this.addSlotToContainer(new Slot(invPlayer, j + i * 9 + 9, 20 + j * 18, 84 + i * 18));

        // Player hotbar
        for (int i = 0; i < 9; i++) this.addSlotToContainer(new Slot(invPlayer, i, 20 + i * 18, 142));
    }

    @Override
    public void addCraftingToCrafters(ICrafting par1ICrafting) {
        super.addCraftingToCrafters(par1ICrafting);
        tile.refreshDisplayValues();
        par1ICrafting.sendProgressBarUpdate(this, 0, tile.displaySunLevel);
        if (par1ICrafting instanceof EntityPlayerMP) {
            PacketHandler.sendTo(
                new CollectorSyncPKT(tile.displayEmc, tile.displayItemCharge, tile.xCoord, tile.yCoord, tile.zCoord),
                (EntityPlayerMP) par1ICrafting);
        }
        sunLevel = tile.displaySunLevel;
        lastDisplayEmc = tile.displayEmc;
        lastItemCharge = tile.displayItemCharge;
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        tile.refreshDisplayValues();

        boolean sunChanged = sunLevel != tile.displaySunLevel;
        boolean emcChanged = lastDisplayEmc != tile.displayEmc
            || Double.compare(lastItemCharge, tile.displayItemCharge) != 0;

        if (sunChanged || emcChanged) {
            CollectorSyncPKT pkt = emcChanged
                ? new CollectorSyncPKT(tile.displayEmc, tile.displayItemCharge, tile.xCoord, tile.yCoord, tile.zCoord)
                : null;

            for (int i = 0; i < this.crafters.size(); ++i) {
                ICrafting icrafting = (ICrafting) this.crafters.get(i);

                if (sunChanged) {
                    icrafting.sendProgressBarUpdate(this, 0, tile.displaySunLevel);
                }

                if (pkt != null && icrafting instanceof EntityPlayerMP) {
                    PacketHandler.sendTo(pkt, (EntityPlayerMP) icrafting);
                }
            }

            sunLevel = tile.displaySunLevel;
            lastDisplayEmc = tile.displayEmc;
            lastItemCharge = tile.displayItemCharge;
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void updateProgressBar(int id, int data) {
        switch (id) {
            case 0:
                sunLevel = data;
                tile.displaySunLevel = data;
                break;
            case 1:
                emc = data;
                break;
            case 2:
                kleinChargeProgress = data / 8000.0;
                break;
            case 3:
                fuelProgress = data / 8000.0;
                break;
            case 4:
                kleinEmc = data;
                break;
        }
    }

    @Override
    public void onContainerClosed(EntityPlayer player) {
        super.onContainerClosed(player);
        tile.closeInventory();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void updateProgressBarLong(int id, long data) {
        switch (id) {
            case 1:
                emc = data;
                break;
            default:
                updateProgressBar(id, (int) data);
        }
    }

    @Nonnull
    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int slotIndex) {
        Slot slot = this.getSlot(slotIndex);

        if (slot == null || !slot.getHasStack()) {
            return null;
        }

        ItemStack stack = slot.getStack();
        ItemStack newStack = stack.copy();

        if (slotIndex <= 14) {
            if (!this.mergeItemStack(stack, 15, 50, false)) {
                return null;
            }
        } else if (slotIndex >= 15 && slotIndex <= 50) {
            if (!FuelMapper.isStackFuel(stack) || FuelMapper.isStackMaxFuel(stack)
                || !this.mergeItemStack(stack, 1, 12, false)) {
                return null;
            }
        } else {
            return null;
        }

        if (stack.stackSize == 0) {
            slot.putStack(null);
        } else {
            slot.onSlotChanged();
        }

        slot.onPickupFromSlot(player, stack);
        return newStack;
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return player.getDistanceSq(tile.xCoord + 0.5, tile.yCoord + 0.5, tile.zCoord + 0.5) <= 64.0;
    }
}
