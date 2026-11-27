package moze_intel.projecte.emc;

import java.util.function.IntToLongFunction;

/**
 * Picks upgrade targets in a list of fuels sorted by ascending emc value. Kept free of game classes so it can be unit
 * tested.
 */
final class FuelOrder {

    private FuelOrder() {}

    /**
     * @param size    how many fuels there are
     * @param valueAt the emc value of the fuel at an index, ascending
     * @return the index of the first fuel after {@code index} that is worth more, or -1 if there is none. Fuels of the
     *         same value are skipped.
     */
    static int nextMoreValuable(int size, IntToLongFunction valueAt, int index) {
        long value = valueAt.applyAsLong(index);

        for (int i = index + 1; i < size; i++) {
            if (valueAt.applyAsLong(i) > value) {
                return i;
            }
        }

        return -1;
    }

    /** @return the index of the fuel that the one at {@code index} upgrades into. The best fuels wrap around to 0. */
    static int upgradeTarget(int size, IntToLongFunction valueAt, int index) {
        int next = nextMoreValuable(size, valueAt, index);

        return next == -1 ? 0 : next;
    }
}
