package moze_intel.projecte.gameObjs.tiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import moze_intel.projecte.gameObjs.tiles.FurnaceBlockState.Transition;

class FurnaceBlockStateTest {

    @Test
    void normalSingleTickLifecycleLightsThenExtinguishesOnNextTick() {
        FurnaceBlockState state = new FurnaceBlockState();
        state.initialize(false, false, false);

        assertEquals(Transition.TURN_ON, state.update(10L, false, true));
        assertFalse(state.isPendingExtinguish());

        assertEquals(Transition.NONE, state.update(11L, true, true));
        assertFalse(state.isPendingExtinguish());

        assertEquals(Transition.TURN_OFF, state.update(12L, true, false));
        assertFalse(state.isPendingExtinguish());

        assertEquals(Transition.NONE, state.update(13L, false, false));
    }

    @Test
    void defersExtinguishWithinSameWorldTickUnderAcceleration() {
        FurnaceBlockState state = new FurnaceBlockState();
        state.initialize(false, false, false);

        // First accelerated call on tick 10 ignites the furnace.
        assertEquals(Transition.TURN_ON, state.update(10L, false, true));

        // Second accelerated call on the same tick runs out of fuel/items: defer TURN_OFF.
        assertEquals(Transition.NONE, state.update(10L, true, false));
        assertTrue(state.isPendingExtinguish());

        // Subsequent idle calls in the same tick still stay quiet.
        assertEquals(Transition.NONE, state.update(10L, false, false));
        assertTrue(state.isPendingExtinguish());

        // Next world tick with no fuel/items finally extinguishes once.
        assertEquals(Transition.TURN_OFF, state.update(11L, false, false));
        assertFalse(state.isPendingExtinguish());

        // Further idle ticks do not emit TURN_OFF again.
        assertEquals(Transition.NONE, state.update(12L, false, false));
    }

    @Test
    void reignitionInSameOrNextTickCancelsPendingExtinguish() {
        FurnaceBlockState state = new FurnaceBlockState();
        state.initialize(false, false, false);

        assertEquals(Transition.TURN_ON, state.update(20L, false, true));
        assertEquals(Transition.NONE, state.update(20L, true, false));
        assertTrue(state.isPendingExtinguish());

        // Reignites on the same tick (e.g. hopper/input refilled during acceleration).
        assertEquals(Transition.TURN_ON, state.update(20L, false, true));
        assertFalse(state.isPendingExtinguish());

        // Runs out again in tick 20, then reignites on tick 21 before TURN_OFF fires.
        assertEquals(Transition.NONE, state.update(20L, true, false));
        assertTrue(state.isPendingExtinguish());
        assertEquals(Transition.TURN_ON, state.update(21L, false, true));
        assertFalse(state.isPendingExtinguish());
    }

    @Test
    void reloadWhileLitWithNoBurnTimeExtinguishesOnFirstIdleTick() {
        FurnaceBlockState state = new FurnaceBlockState();
        state.initialize(false, false, true);
        assertTrue(state.isPendingExtinguish());

        assertEquals(Transition.TURN_OFF, state.update(100L, false, false));
        assertFalse(state.isPendingExtinguish());
    }

    @Test
    void reloadWhileLitAndImmediatelyBurningClearsPendingExtinguish() {
        FurnaceBlockState state = new FurnaceBlockState();
        state.initialize(false, false, true);
        assertTrue(state.isPendingExtinguish());

        assertEquals(Transition.TURN_ON, state.update(100L, false, true));
        assertFalse(state.isPendingExtinguish());
    }
}
