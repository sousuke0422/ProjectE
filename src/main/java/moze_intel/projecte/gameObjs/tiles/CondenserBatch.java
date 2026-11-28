package moze_intel.projecte.gameObjs.tiles;

/**
 * Computes how many locked items can be condensed into a single output slot in one batch, and the total EMC cost for
 * that batch. Kept free of game classes so it can be unit tested.
 */
final class CondenserBatch {

    private CondenserBatch() {}

    static int countForSlot(int slotStackSize, int stackMaxSize, int inventoryStackLimit, double storedEmc,
        long requiredEmc) {
        if (requiredEmc <= 0L || storedEmc < requiredEmc) {
            return 0;
        }
        int maxAllowed = Math.min(stackMaxSize, inventoryStackLimit);
        int freeSpace = maxAllowed - slotStackSize;
        if (freeSpace <= 0) {
            return 0;
        }
        long affordable = (long) (storedEmc / requiredEmc);
        if (affordable <= 0L) {
            return 0;
        }
        return (int) Math.min((long) freeSpace, affordable);
    }

    static double totalCost(long requiredEmc, int count) {
        return (double) requiredEmc * count;
    }
}
