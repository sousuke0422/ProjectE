package moze_intel.projecte.gameObjs.container;

import javax.annotation.Nonnull;

import moze_intel.projecte.emc.FuelMapper;
import moze_intel.projecte.gameObjs.container.slots.collector.SlotCollectorInv;
import moze_intel.projecte.gameObjs.container.slots.collector.SlotCollectorLock;
import moze_intel.projecte.gameObjs.tiles.CollectorMK3Tile;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class CollectorMK3Container extends LongContainer {

    final CollectorMK3Tile tile;
    public int sunLevel = 0;
    public long emc = 0;
    public double kleinChargeProgress = 0;
    public double fuelProgress = 0;
    public int kleinEmc = 0;

    public CollectorMK3Container(InventoryPlayer invPlayer, CollectorMK3Tile collector) {
        this.tile = collector;
        tile.openInventory();

        // Klein Star Slot
        this.addSlotToContainer(new SlotCollectorInv(tile, 0, 158, 58));

        // Fuel Upgrade Slot
        for (int i = 0; i < 4; i++) for (int j = 0; j < 4; j++)
            this.addSlotToContainer(new SlotCollectorInv(tile, i * 4 + j + 1, 18 + i * 18, 8 + j * 18));

        // Upgrade Result
        this.addSlotToContainer(new SlotCollectorInv(tile, 17, 158, 13));

        // Upgrade Target
        this.addSlotToContainer(new SlotCollectorLock(tile, 18, 187, 36));

        // Player inventory
        for (int i = 0; i < 3; i++) for (int j = 0; j < 9; j++)
            this.addSlotToContainer(new Slot(invPlayer, j + i * 9 + 9, 30 + j * 18, 84 + i * 18));

        // Player hotbar
        for (int i = 0; i < 9; i++) this.addSlotToContainer(new Slot(invPlayer, i, 30 + i * 18, 142));
    }

    @Override
    public void addCraftingToCrafters(ICrafting par1ICrafting) {
        super.addCraftingToCrafters(par1ICrafting);
        par1ICrafting.sendProgressBarUpdate(this, 0, tile.displaySunLevel);
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();

        for (int i = 0; i < this.crafters.size(); ++i) {
            ICrafting icrafting = (ICrafting) this.crafters.get(i);

            if (sunLevel != tile.getSunLevel()) icrafting.sendProgressBarUpdate(this, 1, tile.getSunLevel());
        }

        sunLevel = tile.getSunLevel();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void updateProgressBar(int id, int data) {
        switch (id) {
            case 0:
                sunLevel = data;
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

        if (slotIndex <= 18) {
            if (!this.mergeItemStack(stack, 19, 54, false)) {
                return null;
            }
        } else if (slotIndex >= 19 && slotIndex <= 54) {
            if (!FuelMapper.isStackFuel(stack) || FuelMapper.isStackMaxFuel(stack)
                    || !this.mergeItemStack(stack, 1, 16, false)) {
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
