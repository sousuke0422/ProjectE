package moze_intel.projecte.gameObjs.tiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class CondenserBatchTest {

    @Test
    void fillsEmptySlotUpToStackLimit() {
        assertEquals(64, CondenserBatch.countForSlot(0, 64, 64, 10_000.0, 100L));
        assertEquals(16, CondenserBatch.countForSlot(0, 16, 64, 10_000.0, 100L));
        assertEquals(1, CondenserBatch.countForSlot(0, 1, 64, 10_000.0, 100L));
        assertEquals(32, CondenserBatch.countForSlot(0, 64, 32, 10_000.0, 100L));
    }

    @Test
    void fillsOnlyRemainingSpaceInPartialSlot() {
        assertEquals(24, CondenserBatch.countForSlot(40, 64, 64, 10_000.0, 100L));
        assertEquals(0, CondenserBatch.countForSlot(64, 64, 64, 10_000.0, 100L));
    }

    @Test
    void capsByAffordableEmc() {
        assertEquals(5, CondenserBatch.countForSlot(0, 64, 64, 599.9, 100L));
        assertEquals(3, CondenserBatch.countForSlot(40, 64, 64, 350.0, 100L));
        assertEquals(0, CondenserBatch.countForSlot(0, 64, 64, 99.9, 100L));
    }

    @Test
    void returnsZeroForNonPositiveRequiredEmc() {
        assertEquals(0, CondenserBatch.countForSlot(0, 64, 64, 1_000.0, 0L));
        assertEquals(0, CondenserBatch.countForSlot(0, 64, 64, 1_000.0, -5L));
    }

    @Test
    void totalCostAvoidsLongOverflowForHugeRequiredEmc() {
        // 2^58 * 64 = 2^64 overflows signed long to 0 (and 2^57 * 64 = 2^63 overflows to Long.MIN_VALUE).
        long powerOfTwo = 1L << 57;
        double cost = CondenserBatch.totalCost(powerOfTwo, 64);
        assertTrue(cost > 0.0, "totalCost must stay positive instead of overflowing signed long");
        assertEquals((double) powerOfTwo * 64.0, cost);
    }
}
