package moze_intel.projecte.gameObjs.container.slots.transmutation;

import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import moze_intel.projecte.gameObjs.container.inventory.TransmutationInventory;
import moze_intel.projecte.utils.EMCHelper;

public class SlotUnlearn extends Slot {

    private TransmutationInventory inv;

    public SlotUnlearn(TransmutationInventory inv, int par2, int par3, int par4) {
        super(inv, par2, par3, par4);
        this.inv = inv;
    }

    @Override
    public boolean isItemValid(ItemStack stack) {
        return !this.getHasStack() && EMCHelper.doesItemHaveEmc(stack);
    }

    @Override
    public void putStack(ItemStack stack) {
        if (stack != null) {
            inv.handleUnlearn(stack.copy());
        }

        super.putStack(stack);
    }

    @Override
    public int getSlotStackLimit() {
        return 1;
    }
}
