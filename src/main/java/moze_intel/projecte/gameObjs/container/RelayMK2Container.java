package moze_intel.projecte.gameObjs.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import moze_intel.projecte.gameObjs.container.slots.relay.SlotRelayInput;
import moze_intel.projecte.gameObjs.container.slots.relay.SlotRelayKlein;
import moze_intel.projecte.gameObjs.tiles.RelayMK2Tile;
import moze_intel.projecte.network.PacketHandler;
import moze_intel.projecte.network.packets.RelaySyncPKT;

public class RelayMK2Container extends Container {

    private RelayMK2Tile tile;
    private int lastDisplayEmc = Integer.MIN_VALUE;
    private double lastChargingEmc = Double.NaN;
    private double lastRawEmc = Double.NaN;

    public RelayMK2Container(InventoryPlayer invPlayer, RelayMK2Tile relay) {
        this.tile = relay;
        tile.openInventory();

        // Burn slot
        this.addSlotToContainer(new SlotRelayInput(tile, 0, 84, 44));

        // Inventory buffer
        for (int i = 0; i <= 2; i++) for (int j = 0; j <= 3; j++)
            this.addSlotToContainer(new SlotRelayInput(tile, i * 4 + j + 1, 26 + i * 18, 18 + j * 18));

        // Klein star slot
        this.addSlotToContainer(new SlotRelayKlein(tile, 13, 144, 44));

        // Main player inventory
        for (int i = 0; i < 3; i++) for (int j = 0; j < 9; j++)
            this.addSlotToContainer(new Slot(invPlayer, j + i * 9 + 9, 16 + j * 18, 101 + i * 18));

        // Player hotbar
        for (int i = 0; i < 9; i++) this.addSlotToContainer(new Slot(invPlayer, i, 16 + i * 18, 159));
    }

    @Override
    public void addCraftingToCrafters(ICrafting crafter) {
        super.addCraftingToCrafters(crafter);
        tile.refreshDisplayValues();
        if (crafter instanceof EntityPlayerMP) {
            PacketHandler.sendTo(
                new RelaySyncPKT(
                    tile.displayEmc,
                    tile.displayChargingEmc,
                    tile.displayRawEmc,
                    tile.xCoord,
                    tile.yCoord,
                    tile.zCoord),
                (EntityPlayerMP) crafter);
        }
        lastDisplayEmc = tile.displayEmc;
        lastChargingEmc = tile.displayChargingEmc;
        lastRawEmc = tile.displayRawEmc;
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        tile.refreshDisplayValues();

        if (lastDisplayEmc != tile.displayEmc || Double.compare(lastChargingEmc, tile.displayChargingEmc) != 0
            || Double.compare(lastRawEmc, tile.displayRawEmc) != 0) {
            RelaySyncPKT pkt = new RelaySyncPKT(
                tile.displayEmc,
                tile.displayChargingEmc,
                tile.displayRawEmc,
                tile.xCoord,
                tile.yCoord,
                tile.zCoord);
            for (int i = 0; i < this.crafters.size(); ++i) {
                ICrafting crafter = (ICrafting) this.crafters.get(i);
                if (crafter instanceof EntityPlayerMP) {
                    PacketHandler.sendTo(pkt, (EntityPlayerMP) crafter);
                }
            }
            lastDisplayEmc = tile.displayEmc;
            lastChargingEmc = tile.displayChargingEmc;
            lastRawEmc = tile.displayRawEmc;
        }
    }

    @Override
    public void onContainerClosed(EntityPlayer player) {
        super.onContainerClosed(player);
        tile.closeInventory();
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int slotIndex) {
        Slot slot = this.getSlot(slotIndex);

        if (slot == null || !slot.getHasStack()) {
            return null;
        }

        ItemStack stack = slot.getStack();
        ItemStack newStack = stack.copy();

        if (slotIndex < 14) {
            if (!this.mergeItemStack(stack, 14, this.inventorySlots.size(), true)) return null;
            slot.onSlotChanged();
        } else if (!this.mergeItemStack(stack, 0, 13, false)) {
            return null;
        }
        if (stack.stackSize == 0) {
            slot.putStack(null);
        } else {
            slot.onSlotChanged();
        }

        slot.onPickupFromSlot(player, newStack);
        return newStack;
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return player.getDistanceSq(tile.xCoord + 0.5, tile.yCoord + 0.5, tile.zCoord + 0.5) <= 64.0;
    }
}
