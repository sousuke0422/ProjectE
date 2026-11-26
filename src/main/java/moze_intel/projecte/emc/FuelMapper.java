package moze_intel.projecte.emc;

import java.util.Collections;
import java.util.List;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import com.google.common.collect.Lists;

import moze_intel.projecte.gameObjs.ObjHandler;
import moze_intel.projecte.utils.Comparators;
import moze_intel.projecte.utils.EMCHelper;
import moze_intel.projecte.utils.PELogger;

public final class FuelMapper {

    private static final List<SimpleStack> FUEL_MAP = Lists.newArrayList();
    /** Fuels registered by other mods. Kept apart because the map is rebuilt whenever emc values are (re)loaded. */
    private static final List<ItemStack> REGISTERED_FUELS = Lists.newArrayList();

    /**
     * Adds an item to the fuels that collectors can upgrade. Fuels are ordered by emc value, so the item slots in
     * between the fuels worth less and more than it. Items without an emc value are left out until they have one.
     * <p>
     * Call this during init on both sides, or send the IMC message {@code collectorfuel} with the item stack.
     *
     * @return false if the stack was null or already registered
     */
    public static synchronized boolean registerFuel(ItemStack stack) {
        if (stack == null || stack.getItem() == null) {
            return false;
        }
        for (ItemStack registered : REGISTERED_FUELS) {
            if (registered.getItem() == stack.getItem() && registered.getItemDamage() == stack.getItemDamage()) {
                return false;
            }
        }
        ItemStack copy = stack.copy();
        copy.stackSize = 1;
        REGISTERED_FUELS.add(copy);
        if (!FUEL_MAP.isEmpty()) {
            // Emc is already mapped: put it in place right away
            addToMap(copy);
            Collections.sort(FUEL_MAP, Comparators.SIMPLESTACK_ASCENDING);
        }
        return true;
    }

    /**
     * @return every collector fuel from the least to the most valuable; see {@link #getFuelUpgrade} for what each one
     *         upgrades into
     */
    public static synchronized List<ItemStack> getFuels() {
        List<ItemStack> fuels = Lists.newArrayListWithCapacity(FUEL_MAP.size());
        for (SimpleStack fuel : FUEL_MAP) {
            fuels.add(fuel.toItemStack());
        }
        return fuels;
    }

    public static synchronized void loadMap() {
        if (!FUEL_MAP.isEmpty()) {
            FUEL_MAP.clear();
        }

        addToMap(new ItemStack(Items.coal, 1, 1));
        addToMap(new ItemStack(Items.redstone));
        addToMap(new ItemStack(Blocks.redstone_block));
        addToMap(new ItemStack(Items.coal));
        addToMap(new ItemStack(Blocks.coal_block));
        addToMap(new ItemStack(Items.gunpowder));
        addToMap(new ItemStack(Items.glowstone_dust));
        addToMap(new ItemStack(ObjHandler.fuels, 1, 0));
        addToMap(new ItemStack(ObjHandler.fuelBlock, 1, 0));
        addToMap(new ItemStack(Items.blaze_powder));
        addToMap(new ItemStack(Blocks.glowstone));
        addToMap(new ItemStack(ObjHandler.fuels, 1, 1));
        addToMap(new ItemStack(ObjHandler.fuelBlock, 1, 1));
        addToMap(new ItemStack(ObjHandler.fuels, 1, 2));
        addToMap(new ItemStack(ObjHandler.fuelBlock, 1, 2));

        for (ItemStack registered : REGISTERED_FUELS) {
            addToMap(registered);
        }

        Collections.sort(FUEL_MAP, Comparators.SIMPLESTACK_ASCENDING);
    }

    private static void addToMap(ItemStack stack) {
        if (EMCHelper.doesItemHaveEmc(stack)) {
            addToMap(new SimpleStack(stack));
        }
    }

    public static boolean isStackFuel(ItemStack stack) {
        return mapContains(new SimpleStack(stack));
    }

    /** @return true if no fuel is worth more than this one, so there is nothing to upgrade it into */
    public static boolean isStackMaxFuel(ItemStack stack) {
        int index = indexInMap(new SimpleStack(stack));

        return index == FUEL_MAP.size() - 1 || (index != -1 && nextMoreValuable(index) == -1);
    }

    /**
     * @return the next fuel worth more than this one. Fuels of the same value are skipped: going from one to the
     *         other costs nothing, and collectors only upgrade at a cost. The best fuel wraps around to the worst.
     */
    public static ItemStack getFuelUpgrade(ItemStack stack) {
        SimpleStack fuel = new SimpleStack(stack);

        int index = indexInMap(fuel);

        if (index == -1) {
            PELogger.logFatal("Tried to upgrade invalid fuel: " + stack);
            return null;
        }

        int nextIndex = nextMoreValuable(index);

        return FUEL_MAP.get(nextIndex == -1 ? 0 : nextIndex)
            .toItemStack();
    }

    /** @return the index of the first fuel after {@code index} that is worth more, or -1 if there is none */
    private static int nextMoreValuable(int index) {
        long value = EMCHelper.getEmcValue(
            FUEL_MAP.get(index)
                .toItemStack());

        for (int i = index + 1; i < FUEL_MAP.size(); i++) {
            if (EMCHelper.getEmcValue(
                FUEL_MAP.get(i)
                    .toItemStack())
                > value) {
                return i;
            }
        }

        return -1;
    }

    private static void addToMap(SimpleStack stack) {
        if (stack.isValid()) {
            SimpleStack copy = stack.copy();
            copy.qnty = 1;

            if (!FUEL_MAP.contains(copy)) {
                FUEL_MAP.add(copy);
            }
        }
    }

    private static boolean mapContains(SimpleStack stack) {
        if (!stack.isValid()) {
            return false;
        }

        SimpleStack copy = stack.copy();
        copy.qnty = 1;

        return FUEL_MAP.contains(copy);
    }

    private static int indexInMap(SimpleStack stack) {
        SimpleStack copy = stack.copy();
        copy.qnty = 1;

        return FUEL_MAP.indexOf(copy);
    }
}
