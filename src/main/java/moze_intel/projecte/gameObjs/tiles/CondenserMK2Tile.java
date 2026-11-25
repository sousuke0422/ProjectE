package moze_intel.projecte.gameObjs.tiles;

import net.minecraft.item.ItemStack;

import moze_intel.projecte.utils.EMCHelper;

public class CondenserMK2Tile extends CondenserTile {

    private static final int LOCK_SLOT = 0;
    private static final int INPUT_SLOTS_LOWER = 1;
    private static final int INPUT_SLOTS_UPPER = 42;
    private static final int OUTPUT_SLOTS_LOWER = 43;
    private static final int OUTPUT_SLOTS_UPPER = 84;

    public CondenserMK2Tile() {
        this.inventory = new ItemStack[85];
        this.loadChecks = false;
        initAccessibleSlots();
    }

    @Override
    protected void condense() {
        boolean dirty = false;

        while (this.getStoredEmc() >= requiredEmc) {
            int slot = getSlotForStack();
            if (slot == 0) {
                break;
            }
            long affordable = (long) (this.getStoredEmc() / requiredEmc);
            int pushed = pushStackBatchToSlot(slot, affordable);
            if (pushed <= 0) {
                break;
            }
            this.removeEMC((double) (requiredEmc * (long) pushed));
            dirty = true;
        }

        if (this.hasSpace()) {
            for (int i = INPUT_SLOTS_LOWER; i <= INPUT_SLOTS_UPPER; i++) {
                ItemStack stack = inventory[i];

                if (stack == null) {
                    continue;
                }

                this.addEMC(EMCHelper.getEmcValue(stack) * stack.stackSize);
                inventory[i] = null;
                dirty = true;
                break;
            }
        }

        if (dirty) {
            this.markDirty();
        }
    }

    @Override
    protected boolean hasSpace() {
        for (int i = OUTPUT_SLOTS_LOWER; i <= OUTPUT_SLOTS_UPPER; i++) {
            ItemStack stack = inventory[i];

            if (stack == null) {
                return true;
            }

            if (isStackEqualToLock(stack) && stack.stackSize < stack.getMaxStackSize()) {
                return true;
            }
        }

        return false;
    }

    @Override
    protected int getSlotForStack() {
        for (int i = OUTPUT_SLOTS_LOWER; i <= OUTPUT_SLOTS_UPPER; i++) {
            ItemStack stack = inventory[i];

            if (stack == null) {
                return i;
            }

            if (isStackEqualToLock(stack) && stack.stackSize < stack.getMaxStackSize()) {
                return i;
            }
        }

        return 0;
    }

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack) {
        if (slot == LOCK_SLOT || slot >= OUTPUT_SLOTS_LOWER) {
            return false;
        }

        return !isStackEqualToLock(stack) && EMCHelper.doesItemHaveEmc(stack);
    }

    @Override
    public String getInventoryName() {
        return "tile.pe_condenser_mk2.name";
    }
}
