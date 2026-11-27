package moze_intel.projecte.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

/** Compares the chunk by chunk walk with the position by position scan it replaced. */
class BlockRangeTest {

    private static String key(int x, int y, int z) {
        return x + "," + y + "," + z;
    }

    /** The old scan, limited to the heights that hold blocks. */
    private static List<String> oldScan(double[] box) {
        List<String> positions = new ArrayList<>();
        for (int x = (int) box[0]; x <= box[3]; x++) {
            for (int y = (int) box[1]; y <= box[4]; y++) {
                for (int z = (int) box[2]; z <= box[5]; z++) {
                    if (y >= 0 && y <= 255) {
                        positions.add(key(x, y, z));
                    }
                }
            }
        }
        return positions;
    }

    /** The walk of TimeWatch.speedUpRandomTicks: chunks, then sections, then the positions inside each. */
    private static List<String> sectionWalk(double[] box) {
        BlockRange range = BlockRange.ofBox(box[0], box[1], box[2], box[3], box[4], box[5])
            .clampY(0, 255);
        List<String> positions = new ArrayList<>();

        for (int chunkX = BlockRange.firstPart(range.minX); chunkX <= BlockRange.lastPart(range.maxX); chunkX++) {
            for (int chunkZ = BlockRange.firstPart(range.minZ); chunkZ <= BlockRange.lastPart(range.maxZ); chunkZ++) {
                for (int section = BlockRange.firstPart(range.minY); section
                    <= BlockRange.lastPart(range.maxY); section++) {
                    assertTrue(section >= 0 && section <= 15, "section " + section + " is not in a chunk");

                    for (int x = BlockRange.from(range.minX, chunkX); x <= BlockRange.to(range.maxX, chunkX); x++) {
                        for (int y = BlockRange.from(range.minY, section); y
                            <= BlockRange.to(range.maxY, section); y++) {
                            for (int z = BlockRange.from(range.minZ, chunkZ); z
                                <= BlockRange.to(range.maxZ, chunkZ); z++) {
                                assertEquals(chunkX, x >> 4);
                                assertEquals(chunkZ, z >> 4);
                                assertEquals(section, y >> 4);
                                positions.add(key(x, y, z));
                            }
                        }
                    }
                }
            }
        }
        return positions;
    }

    private static void assertSamePositions(double... box) {
        List<String> expected = oldScan(box);
        List<String> actual = sectionWalk(box);

        Set<String> unique = new HashSet<>(actual);
        assertEquals(unique.size(), actual.size(), "a position was visited twice");

        Collections.sort(expected);
        Collections.sort(actual);
        assertEquals(expected, actual);
    }

    /** What WorldHelper.getTileEntitiesWithinAABB keeps, against the old scan without the height limit. */
    private static void assertSameMembership(double... box) {
        BlockRange range = BlockRange.ofBox(box[0], box[1], box[2], box[3], box[4], box[5]);
        Set<String> expected = new HashSet<>();
        for (int x = (int) box[0]; x <= box[3]; x++) {
            for (int y = (int) box[1]; y <= box[4]; y++) {
                for (int z = (int) box[2]; z <= box[5]; z++) {
                    expected.add(key(x, y, z));
                }
            }
        }

        // Every position of the chunks the walk visits, plus a margin of one chunk around them
        int count = 0;
        for (int x = (BlockRange.firstPart(range.minX) - 1) << 4; x
            <= ((BlockRange.lastPart(range.maxX) + 2) << 4) - 1; x++) {
            for (int y = (int) box[1] - 3; y <= box[4] + 3; y++) {
                for (int z = (BlockRange.firstPart(range.minZ) - 1) << 4; z
                    <= ((BlockRange.lastPart(range.maxZ) + 2) << 4) - 1; z++) {
                    boolean inChunks = (x >> 4) >= BlockRange.firstPart(range.minX)
                        && (x >> 4) <= BlockRange.lastPart(range.maxX)
                        && (z >> 4) >= BlockRange.firstPart(range.minZ)
                        && (z >> 4) <= BlockRange.lastPart(range.maxZ);
                    boolean kept = inChunks && range.contains(x, y, z);

                    assertEquals(expected.contains(key(x, y, z)), kept, "position " + key(x, y, z));
                    if (kept) {
                        count++;
                    }
                }
            }
        }
        assertEquals(expected.size(), count);
    }

    @Test
    void positiveCoordinates() {
        assertSamePositions(100.5, 60.0, 200.5, 117.5, 78.0, 217.5);
        assertSameMembership(100.5, 60.0, 200.5, 117.5, 78.0, 217.5);
    }

    @Test
    void negativeCoordinates() {
        // The lower bound is cut towards zero, the upper bound is floored: that asymmetry is the old behaviour
        assertSamePositions(-20.5, 60.0, -9.7, -3.5, 70.0, -0.3);
        assertSameMembership(-20.5, 60.0, -9.7, -3.5, 70.0, -0.3);
    }

    @Test
    void acrossZero() {
        assertSamePositions(-8.5, 60.0, -8.5, 8.5, 70.0, 8.5);
        assertSameMembership(-8.5, 60.0, -8.5, 8.5, 70.0, 8.5);
    }

    @Test
    void acrossChunkAndSectionBorders() {
        assertSamePositions(13.0, 14.0, 30.0, 34.0, 33.0, 49.0);
        assertSameMembership(13.0, 14.0, 30.0, 34.0, 33.0, 49.0);
    }

    @Test
    void exactlyOnChunkBorders() {
        assertSamePositions(16.0, 16.0, 16.0, 31.0, 31.0, 31.0);
        assertSamePositions(15.999, 15.999, 15.999, 32.0, 32.0, 32.0);
    }

    @Test
    void belowTheWorld() {
        assertSamePositions(0.5, -8.5, 0.5, 9.5, 8.5, 9.5);
        assertSameMembership(0.5, -8.5, 0.5, 9.5, 8.5, 9.5);
    }

    @Test
    void aboveTheWorld() {
        assertSamePositions(0.5, 248.5, 0.5, 9.5, 264.5, 9.5);
    }

    @Test
    void entirelyOutsideTheWorld() {
        assertEquals(Collections.emptyList(), sectionWalk(new double[] { 0.5, 300.0, 0.5, 9.5, 310.0, 9.5 }));
        assertEquals(Collections.emptyList(), sectionWalk(new double[] { 0.5, -20.0, 0.5, 9.5, -5.0, 9.5 }));
        assertSamePositions(0.5, 300.0, 0.5, 9.5, 310.0, 9.5);
        assertSamePositions(0.5, -20.0, 0.5, 9.5, -5.0, 9.5);
    }
}
