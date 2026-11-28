package moze_intel.projecte.gameObjs.tiles;

/**
 * Tracks whether a matter furnace's lit block state should change, deferring extinguish until the next world tick so
 * tick acceleration cannot cause repeated on/off block updates within a single tick. Kept free of game classes so it
 * can be unit tested.
 */
final class FurnaceBlockState {

    enum Transition {
        NONE,
        TURN_ON,
        TURN_OFF
    }

    private long lastActiveTick = Long.MIN_VALUE;
    private boolean pendingExtinguish = false;
    private boolean initialized = false;

    boolean isInitialized() {
        return initialized;
    }

    void initialize(boolean isCurrentlyBurning, boolean wasBurning, boolean blockIsLit) {
        if (!initialized) {
            initialized = true;
            if (!isCurrentlyBurning && !wasBurning && blockIsLit) {
                pendingExtinguish = true;
            }
        }
    }

    Transition update(long currentTick, boolean wasBurning, boolean isBurningNow) {
        if (isBurningNow) {
            lastActiveTick = currentTick;
            pendingExtinguish = false;
            return wasBurning ? Transition.NONE : Transition.TURN_ON;
        }
        if (wasBurning && lastActiveTick == currentTick) {
            pendingExtinguish = true;
            return Transition.NONE;
        }
        if ((wasBurning || pendingExtinguish) && lastActiveTick != currentTick) {
            pendingExtinguish = false;
            return Transition.TURN_OFF;
        }
        return Transition.NONE;
    }

    boolean isPendingExtinguish() {
        return pendingExtinguish;
    }
}
