package moze_intel.projecte.emc;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class FuelOrderTest {

    private static int next(long[] values, int index) {
        return FuelOrder.nextMoreValuable(values.length, i -> values[i], index);
    }

    private static int target(long[] values, int index) {
        return FuelOrder.upgradeTarget(values.length, i -> values[i], index);
    }

    @Test
    void stepsToTheNextFuel() {
        long[] values = { 32, 64, 128 };

        assertEquals(1, next(values, 0));
        assertEquals(2, next(values, 1));
    }

    @Test
    void skipsFuelsOfTheSameValue() {
        long[] values = { 32, 128, 128, 128, 512 };

        assertEquals(4, next(values, 1));
        assertEquals(4, next(values, 2));
        assertEquals(4, next(values, 3));
    }

    @Test
    void severalBestFuelsHaveNowhereToGo() {
        long[] values = { 32, 512, 512 };

        assertEquals(-1, next(values, 1));
        assertEquals(-1, next(values, 2));
        assertEquals(0, target(values, 1));
        assertEquals(0, target(values, 2));
    }

    @Test
    void theBestFuelWrapsAround() {
        long[] values = { 32, 64, 128 };

        assertEquals(-1, next(values, 2));
        assertEquals(0, target(values, 2));
    }

    @Test
    void aSingleFuelHasNowhereToGo() {
        long[] values = { 32 };

        assertEquals(-1, next(values, 0));
        assertEquals(0, target(values, 0));
    }
}
