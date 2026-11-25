package moze_intel.projecte.utils;

import java.util.HashMap;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;

import com.google.common.collect.Maps;

public final class WorldTransmutations {

    public static final HashMap<MetaBlock, MetaBlock[]> MAP = Maps.newHashMap();

    static {
        register(Blocks.stone, Blocks.cobblestone, Blocks.grass);
        register(Blocks.cobblestone, Blocks.stone, Blocks.grass);
        register(Blocks.grass, Blocks.sand, Blocks.cobblestone);
        register(Blocks.dirt, Blocks.sand, Blocks.cobblestone);
        register(Blocks.sand, Blocks.grass, Blocks.cobblestone);
        register(Blocks.gravel, Blocks.sandstone);
        register(Blocks.water, Blocks.ice);
        register(Blocks.lava, Blocks.obsidian);
        register(Blocks.melon_block, Blocks.pumpkin);

        register(
            new MetaBlock(Blocks.log, 0),
            new MetaBlock[] { new MetaBlock(Blocks.log, 1), new MetaBlock(Blocks.log2, 1) });
        register(
            new MetaBlock(Blocks.leaves, 0),
            new MetaBlock[] { new MetaBlock(Blocks.leaves, 1), new MetaBlock(Blocks.leaves, 1) });

        for (int i = 1; i < 3; i++) {
            register(
                new MetaBlock(Blocks.log, i),
                new MetaBlock[] { new MetaBlock(Blocks.log, i + 1), new MetaBlock(Blocks.log, i - 1) });
            register(
                new MetaBlock(Blocks.leaves, i),
                new MetaBlock[] { new MetaBlock(Blocks.leaves, i + 1), new MetaBlock(Blocks.leaves, i - 1) });
        }

        register(
            new MetaBlock(Blocks.log, 3),
            new MetaBlock[] { new MetaBlock(Blocks.log2, 0), new MetaBlock(Blocks.log, 2) });
        register(
            new MetaBlock(Blocks.leaves, 3),
            new MetaBlock[] { new MetaBlock(Blocks.leaves2, 0), new MetaBlock(Blocks.leaves, 2) });
        register(
            new MetaBlock(Blocks.log2, 0),
            new MetaBlock[] { new MetaBlock(Blocks.log2, 1), new MetaBlock(Blocks.log, 3) });
        register(
            new MetaBlock(Blocks.leaves2, 0),
            new MetaBlock[] { new MetaBlock(Blocks.leaves2, 1), new MetaBlock(Blocks.leaves, 3) });
        register(
            new MetaBlock(Blocks.log2, 1),
            new MetaBlock[] { new MetaBlock(Blocks.log, 0), new MetaBlock(Blocks.log2, 0) });
        register(
            new MetaBlock(Blocks.leaves2, 1),
            new MetaBlock[] { new MetaBlock(Blocks.leaves, 0), new MetaBlock(Blocks.leaves2, 0) });

        register(
            new MetaBlock(Blocks.sapling, 0),
            new MetaBlock[] { new MetaBlock(Blocks.sapling, 1), new MetaBlock(Blocks.sapling, 5) });

        for (int i = 1; i < 5; i++) {
            register(
                new MetaBlock(Blocks.sapling, i),
                new MetaBlock[] { new MetaBlock(Blocks.sapling, i + 1), new MetaBlock(Blocks.sapling, i - 1) });
        }

        register(
            new MetaBlock(Blocks.sapling, 5),
            new MetaBlock[] { new MetaBlock(Blocks.sapling, 0), new MetaBlock(Blocks.sapling, 4) });

        register(
            new MetaBlock(Blocks.wool, 0),
            new MetaBlock[] { new MetaBlock(Blocks.wool, 1), new MetaBlock(Blocks.wool, 15) });

        for (int i = 1; i < 15; i++) {
            register(
                new MetaBlock(Blocks.wool, i),
                new MetaBlock[] { new MetaBlock(Blocks.wool, i + 1), new MetaBlock(Blocks.wool, i - 1) });
        }

        register(
            new MetaBlock(Blocks.wool, 15),
            new MetaBlock[] { new MetaBlock(Blocks.wool, 0), new MetaBlock(Blocks.wool, 14) });
    }

    private static boolean isVanillaFluid(Block b) {
        return b == Blocks.lava || b == Blocks.flowing_lava || b == Blocks.water || b == Blocks.flowing_water;
    }

    /**
     * Lava/water still vs flowing share one MAP entry (still block key). Used for lookup and overlay matching.
     * Only source blocks (meta 0) have a key: a flowing cell comes back for free as long as its source stands, so
     * transmuting it would hand out endless obsidian.
     */
    public static MetaBlock fluidMapKeyOrNull(MetaBlock fromWorld) {
        if (fromWorld.getMeta() != 0) {
            return null;
        }
        Block b = fromWorld.getBlock();
        if (b == Blocks.flowing_lava || b == Blocks.lava) {
            return new MetaBlock(Blocks.lava);
        }
        if (b == Blocks.flowing_water || b == Blocks.water) {
            return new MetaBlock(Blocks.water);
        }
        return null;
    }

    public static boolean sameTransmutableFluid(MetaBlock a, MetaBlock b) {
        MetaBlock ka = fluidMapKeyOrNull(a);
        MetaBlock kb = fluidMapKeyOrNull(b);
        if (ka == null || kb == null) {
            return false;
        }
        return ka.equals(kb);
    }

    /**
     * Whether a block in the charge area counts as the pointed block. Lava and water are compared as sources only;
     * {@link MetaBlock#equals} alone would let every flow level through, since liquids all drop damage 0.
     */
    public static boolean matchesTarget(MetaBlock there, MetaBlock pointed) {
        if (isVanillaFluid(there.getBlock()) || isVanillaFluid(pointed.getBlock())) {
            return sameTransmutableFluid(there, pointed);
        }
        return there.equals(pointed);
    }

    public static MetaBlock getWorldTransmutation(World world, int x, int y, int z, boolean isSneaking) {
        return getWorldTransmutation(new MetaBlock(world, x, y, z), isSneaking);
    }

    public static MetaBlock getWorldTransmutation(MetaBlock block, boolean isSneaking) {
        MetaBlock key = block;
        if (isVanillaFluid(block.getBlock())) {
            key = fluidMapKeyOrNull(block);
            if (key == null) {
                return null;
            }
        }

        if (MAP.containsKey(key)) {
            return MAP.get(key)[isSneaking ? 1 : 0];
        }

        return null;
    }

    private static void register(Block block, Block result) {
        MAP.put(new MetaBlock(block), new MetaBlock[] { new MetaBlock(result), new MetaBlock(result) });
        MAP.put(new MetaBlock(result), new MetaBlock[] { new MetaBlock(block), new MetaBlock(block) });
    }

    private static void register(Block block, Block b1, Block b2) {
        MAP.put(new MetaBlock(block), new MetaBlock[] { new MetaBlock(b1), new MetaBlock(b2) });
    }

    private static void register(MetaBlock block, MetaBlock[] result) {
        MAP.put(block, result);
    }

    public static void register(MetaBlock origin, MetaBlock result1, MetaBlock result2) {
        // Stopgap method for API impl. This class is much improved in 1.8 in terms of cleanliness
        if (result2 != null) {
            MAP.put(origin, new MetaBlock[] { result1, result2 });
        } else {
            MAP.put(origin, new MetaBlock[] { result1 });
        }
    }
}
