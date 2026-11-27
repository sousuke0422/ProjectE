package moze_intel.projecte.utils;

/**
 * The block positions a bounding box covers, the way the old position by position scans counted them, and how to cut
 * that range into chunks and chunk sections. Kept free of game classes so it can be unit tested.
 */
public final class BlockRange {

    public final int minX, minY, minZ;
    public final int maxX, maxY, maxZ;

    private BlockRange(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        this.minX = minX;
        this.minY = minY;
        this.minZ = minZ;
        this.maxX = maxX;
        this.maxY = maxY;
        this.maxZ = maxZ;
    }

    /**
     * The range of {@code for (int x = (int) minX; x <= maxX; x++)} on each axis: the lower bound is cut towards zero,
     * the upper bound is floored.
     */
    public static BlockRange ofBox(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        return new BlockRange((int) minX, (int) minY, (int) minZ, floor(maxX), floor(maxY), floor(maxZ));
    }

    /** The same range with y limited to the given bounds. Empty when the box lies outside them. */
    public BlockRange clampY(int lowest, int highest) {
        return new BlockRange(minX, Math.max(minY, lowest), minZ, maxX, Math.min(maxY, highest), maxZ);
    }

    public boolean contains(int x, int y, int z) {
        return x >= minX && x <= maxX && y >= minY && y <= maxY && z >= minZ && z <= maxZ;
    }

    /** The first chunk or chunk section (16 blocks along one axis) the range touches. */
    public static int firstPart(int min) {
        return min >> 4;
    }

    /** The last chunk or chunk section the range touches. Smaller than the first when the range is empty. */
    public static int lastPart(int max) {
        return max >> 4;
    }

    /** The first position of the range that lies in the given chunk or chunk section. */
    public static int from(int min, int part) {
        return Math.max(min, part << 4);
    }

    /** The last position of the range that lies in the given chunk or chunk section. */
    public static int to(int max, int part) {
        return Math.min(max, (part << 4) + 15);
    }

    private static int floor(double value) {
        int cut = (int) value;
        return value < cut ? cut - 1 : cut;
    }
}
