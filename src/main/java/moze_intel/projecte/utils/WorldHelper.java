package moze_intel.projecte.utils;

import java.lang.reflect.Constructor;
import java.util.*;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockFlower;
import net.minecraft.block.IGrowable;
import net.minecraft.block.material.Material;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityBlaze;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.monster.EntityGhast;
import net.minecraft.entity.monster.EntityPigZombie;
import net.minecraft.entity.monster.EntitySilverfish;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.entity.monster.EntityWitch;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.EntityBat;
import net.minecraft.entity.passive.EntityChicken;
import net.minecraft.entity.passive.EntityCow;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.passive.EntityMooshroom;
import net.minecraft.entity.passive.EntityOcelot;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.entity.passive.EntitySquid;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.IShearable;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.event.world.ExplosionEvent;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import moze_intel.projecte.config.ProjectEConfig;
import moze_intel.projecte.gameObjs.entity.EntityLootBall;

/**
 * Helper class for anything that touches a World. Notice: Please try to keep methods tidy and alphabetically ordered.
 * Thanks!
 */
public final class WorldHelper {

    public static final ImmutableList<Class<? extends EntityLiving>> peacefuls = ImmutableList
        .<Class<? extends EntityLiving>>of(
            EntitySheep.class,
            EntityPig.class,
            EntityCow.class,
            EntityMooshroom.class,
            EntityChicken.class,
            EntityBat.class,
            EntityVillager.class,
            EntitySquid.class,
            EntityOcelot.class,
            EntityWolf.class,
            EntityHorse.class);
    public static final ImmutableList<Class<? extends EntityLiving>> mobs = ImmutableList
        .<Class<? extends EntityLiving>>of(
            EntityZombie.class,
            EntitySkeleton.class,
            EntityCreeper.class,
            EntitySpider.class,
            EntityEnderman.class,
            EntitySilverfish.class,
            EntityPigZombie.class,
            EntityGhast.class,
            EntityBlaze.class,
            EntitySlime.class,
            EntityWitch.class);

    public static Set<Class<? extends Entity>> interdictionBlacklist = Sets.newHashSet();

    public static Set<Class<? extends Entity>> swrgBlacklist = Sets.newHashSet();

    /** The random ring effects touch each block position once in this many calls on average. */
    private static final int RANDOM_EFFECT_ODDS = 128;

    public static boolean blacklistInterdiction(Class<? extends Entity> clazz) {
        if (!interdictionBlacklist.contains(clazz)) {
            interdictionBlacklist.add(clazz);
            return true;
        }
        return false;
    }

    public static boolean blacklistSwrg(Class<? extends Entity> clazz) {
        if (!interdictionBlacklist.contains(clazz)) {
            interdictionBlacklist.add(clazz);
            return true;
        }
        return false;
    }

    public static void createLootDrop(List<ItemStack> drops, World world, double x, double y, double z) {
        if (drops.isEmpty()) {
            return;
        }

        ItemHelper.compactItemList(drops);

        if (ProjectEConfig.useLootBalls) {
            world.spawnEntityInWorld(new EntityLootBall(world, drops, x, y, z));
        } else {
            for (ItemStack drop : drops) {
                spawnEntityItem(world, drop, x, y, z);
            }
        }
    }

    /**
     * Equivalent of World.newExplosion
     */
    public static void createNovaExplosion(World world, Entity exploder, double x, double y, double z, float power) {
        NovaExplosion explosion = new NovaExplosion(world, exploder, x, y, z, power);
        if (!MinecraftForge.EVENT_BUS.post(new ExplosionEvent.Start(world, explosion))) {
            explosion.doExplosionA();
            explosion.doExplosionB(true);
        }
    }

    /** Puts out fire in the 3x3x3 blocks around the player. */
    public static void extinguishNearby(World world, EntityPlayer player) {
        int centerX = MathHelper.floor_double(player.posX);
        int centerY = MathHelper.floor_double(player.posY);
        int centerZ = MathHelper.floor_double(player.posZ);

        for (int x = centerX - 1; x <= centerX + 1; x++) {
            for (int y = centerY - 1; y <= centerY + 1; y++) {
                for (int z = centerZ - 1; z <= centerZ + 1; z++) {
                    if (world.getBlock(x, y, z) == Blocks.fire
                        && PlayerHelper.hasBreakPermission((EntityPlayerMP) player, x, y, z)) {
                        world.setBlockToAir(x, y, z);
                    }
                }
            }
        }
    }

    /**
     * Turns water to ice and lays snow on solid ground inside the box.
     *
     * @param player whose permissions are checked; null for none (a pedestal)
     * @param random true to affect each position with a chance of 1 in {@link #RANDOM_EFFECT_ODDS} per call, false
     *               for every position
     */
    public static void freezeInBoundingBox(World world, AxisAlignedBB box, EntityPlayer player, boolean random) {
        int minX = MathHelper.floor_double(box.minX);
        int minY = MathHelper.floor_double(box.minY);
        int minZ = MathHelper.floor_double(box.minZ);
        int sizeX = MathHelper.floor_double(box.maxX) - minX + 1;
        int sizeY = MathHelper.floor_double(box.maxY) - minY + 1;
        int sizeZ = MathHelper.floor_double(box.maxZ) - minZ + 1;

        if (random) {
            // Same rate per position as rolling for each of them, without visiting them all
            Random rand = world.rand;
            for (int n = randomPicks(rand, sizeX * sizeY * sizeZ); n > 0; n--) {
                freezeAt(
                    world,
                    minX + rand.nextInt(sizeX),
                    minY + rand.nextInt(sizeY),
                    minZ + rand.nextInt(sizeZ),
                    player);
            }
            return;
        }

        for (int x = minX; x < minX + sizeX; x++) {
            for (int y = minY; y < minY + sizeY; y++) {
                for (int z = minZ; z < minZ + sizeZ; z++) {
                    freezeAt(world, x, y, z, player);
                }
            }
        }
    }

    private static void freezeAt(World world, int x, int y, int z, EntityPlayer player) {
        Block block = world.getBlock(x, y, z);

        if (block == Blocks.water || block == Blocks.flowing_water) {
            replaceBlock(world, x, y, z, Blocks.ice, player);
        } else if (block.isSideSolid(world, x, y, z, ForgeDirection.UP) && world.getBlock(x, y + 1, z) == Blocks.air) {
            replaceBlock(world, x, y + 1, z, Blocks.snow_layer, player);
        }
    }

    private static void replaceBlock(World world, int x, int y, int z, Block with, EntityPlayer player) {
        if (player != null) {
            PlayerHelper.checkedReplaceBlock((EntityPlayerMP) player, x, y, z, with, 0);
        } else {
            world.setBlock(x, y, z, with);
        }
    }

    /**
     * How many positions to pick out of {@code positions} so that each one comes up once in
     * {@link #RANDOM_EFFECT_ODDS} calls on average.
     */
    private static int randomPicks(Random rand, int positions) {
        int picks = positions / RANDOM_EFFECT_ODDS;
        if (rand.nextInt(RANDOM_EFFECT_ODDS) < positions % RANDOM_EFFECT_ODDS) {
            picks++;
        }
        return picks;
    }

    public static List<TileEntity> getAdjacentTileEntities(World world, TileEntity tile) {
        return ImmutableList.copyOf(getAdjacentTileEntitiesMapped(world, tile).values());
    }

    public static Map<ForgeDirection, TileEntity> getAdjacentTileEntitiesMapped(final World world,
        final TileEntity tile) {
        Map<ForgeDirection, TileEntity> ret = new EnumMap<>(ForgeDirection.class);

        for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
            TileEntity candidate = world
                .getTileEntity(tile.xCoord + dir.offsetX, tile.yCoord + dir.offsetY, tile.zCoord + dir.offsetZ);
            if (candidate != null) {
                ret.put(dir, candidate);
            }
        }

        return ret;
    }

    public static ArrayList<ItemStack> getBlockDrops(World world, EntityPlayer player, Block block, ItemStack stack,
        int x, int y, int z) {
        int meta = world.getBlockMetadata(x, y, z);

        if (EnchantmentHelper.getEnchantmentLevel(Enchantment.silkTouch.effectId, stack) > 0
            && block.canSilkHarvest(world, player, x, y, z, meta)) {
            return Lists.newArrayList(new ItemStack(block, 1, meta));
        }

        return block
            .getDrops(world, x, y, z, meta, EnchantmentHelper.getEnchantmentLevel(Enchantment.fortune.effectId, stack));
    }

    /**
     * Gets an AABB for AOE digging operations. The offset increases both the breadth and depth of the box.
     */
    public static AxisAlignedBB getBroadDeepBox(Coordinates coords, ForgeDirection direction, int offset) {
        if (direction.offsetX > 0) {
            return AxisAlignedBB.getBoundingBox(
                coords.x - offset,
                coords.y - offset,
                coords.z - offset,
                coords.x,
                coords.y + offset,
                coords.z + offset);
        } else if (direction.offsetX < 0) {
            return AxisAlignedBB.getBoundingBox(
                coords.x,
                coords.y - offset,
                coords.z - offset,
                coords.x + offset,
                coords.y + offset,
                coords.z + offset);
        } else if (direction.offsetY > 0) {
            return AxisAlignedBB.getBoundingBox(
                coords.x - offset,
                coords.y - offset,
                coords.z - offset,
                coords.x + offset,
                coords.y,
                coords.z + offset);
        } else if (direction.offsetY < 0) {
            return AxisAlignedBB.getBoundingBox(
                coords.x - offset,
                coords.y,
                coords.z - offset,
                coords.x + offset,
                coords.y + offset,
                coords.z + offset);
        } else if (direction.offsetZ > 0) {
            return AxisAlignedBB.getBoundingBox(
                coords.x - offset,
                coords.y - offset,
                coords.z - offset,
                coords.x + offset,
                coords.y + offset,
                coords.z);
        } else if (direction.offsetZ < 0) {
            return AxisAlignedBB.getBoundingBox(
                coords.x - offset,
                coords.y - offset,
                coords.z,
                coords.x + offset,
                coords.y + offset,
                coords.z + offset);
        }
        return AxisAlignedBB.getBoundingBox(0, 0, 0, 0, 0, 0);
    }

    /**
     * Returns in AABB that is always 3x3 orthogonal to the side hit, but varies in depth in the direction of the side
     * hit
     */
    public static AxisAlignedBB getDeepBox(Coordinates coords, ForgeDirection direction, int depth) {
        if (direction.offsetX != 0) {
            if (direction.offsetX > 0) {
                return AxisAlignedBB
                    .getBoundingBox(coords.x - depth, coords.y - 1, coords.z - 1, coords.x, coords.y + 1, coords.z + 1);
            } else return AxisAlignedBB
                .getBoundingBox(coords.x, coords.y - 1, coords.z - 1, coords.x + depth, coords.y + 1, coords.z + 1);
        } else if (direction.offsetY != 0) {
            if (direction.offsetY > 0) {
                return AxisAlignedBB
                    .getBoundingBox(coords.x - 1, coords.y - depth, coords.z - 1, coords.x + 1, coords.y, coords.z + 1);
            } else return AxisAlignedBB
                .getBoundingBox(coords.x - 1, coords.y, coords.z - 1, coords.x + 1, coords.y + depth, coords.z + 1);
        } else {
            if (direction.offsetZ > 0) {
                return AxisAlignedBB
                    .getBoundingBox(coords.x - 1, coords.y - 1, coords.z - depth, coords.x + 1, coords.y + 1, coords.z);
            } else return AxisAlignedBB
                .getBoundingBox(coords.x - 1, coords.y - 1, coords.z, coords.x + 1, coords.y + 1, coords.z + depth);
        }
    }

    /**
     * Gets an AABB for AOE digging operations. The charge increases only the breadth of the box. Y level remains
     * constant. As such, a direction hit is unneeded.
     */
    public static AxisAlignedBB getFlatYBox(Coordinates coords, int offset) {
        return AxisAlignedBB.getBoundingBox(
            coords.x - offset,
            coords.y,
            coords.z - offset,
            coords.x + offset,
            coords.y,
            coords.z + offset);
    }

    public static <T extends Entity> T getNewEntityInstance(Class<T> c, World world) {
        try {
            Constructor<T> constr = c.getConstructor(World.class);
            T ent = constr.newInstance(world);

            if (ent instanceof EntitySkeleton) {
                if (world.rand.nextInt(2) == 0) {
                    ((EntitySkeleton) ent).setSkeletonType(1);
                    ent.setCurrentItemOrArmor(0, new ItemStack(Items.stone_sword));
                } else {
                    ent.setCurrentItemOrArmor(0, new ItemStack(Items.bow));
                }
            } else if (ent instanceof EntityPigZombie) {
                ent.setCurrentItemOrArmor(0, new ItemStack(Items.golden_sword));
            }

            return ent;
        } catch (Exception e) {
            PELogger.logFatal("Could not create new entity instance for: " + c.getCanonicalName());
            e.printStackTrace();
        }

        return null;
    }

    public static EntityLiving getRandomEntity(World world, EntityLiving toRandomize) {
        Class<? extends EntityLiving> entClass = toRandomize.getClass();

        if (peacefuls.contains(entClass)) {
            return getNewEntityInstance(CollectionHelper.getRandomListEntry(peacefuls, entClass), world);
        } else if (mobs.contains(entClass)) {
            return getNewEntityInstance(CollectionHelper.getRandomListEntry(mobs, entClass), world);
        } else if (world.rand.nextInt(2) == 0) {
            return new EntitySlime(world);
        } else {
            return new EntitySheep(world);
        }
    }

    public static List<TileEntity> getTileEntitiesWithinAABB(World world, AxisAlignedBB bBox) {
        return getTileEntitiesWithinAABB(world, bBox, false);
    }

    /**
     * The tile entities in a box, ordered by x, then y, then z. Reads them from the chunks' own tile entity maps, so
     * the cost follows the number of tile entities around rather than the volume of the box.
     *
     * @param skipUnloadedChunks leave chunks that are not loaded alone instead of loading them
     */
    public static List<TileEntity> getTileEntitiesWithinAABB(World world, AxisAlignedBB bBox,
        boolean skipUnloadedChunks) {
        List<TileEntity> list = Lists.newArrayList();
        // The same block range the old position by position scan covered
        BlockRange range = BlockRange.ofBox(bBox.minX, bBox.minY, bBox.minZ, bBox.maxX, bBox.maxY, bBox.maxZ);

        for (int chunkX = BlockRange.firstPart(range.minX); chunkX <= BlockRange.lastPart(range.maxX); chunkX++) {
            for (int chunkZ = BlockRange.firstPart(range.minZ); chunkZ <= BlockRange.lastPart(range.maxZ); chunkZ++) {
                if (skipUnloadedChunks && !world.getChunkProvider()
                    .chunkExists(chunkX, chunkZ)) {
                    continue;
                }
                for (Object obj : world.getChunkFromChunkCoords(chunkX, chunkZ).chunkTileEntityMap.values()) {
                    TileEntity tile = (TileEntity) obj;
                    if (!tile.isInvalid() && range.contains(tile.xCoord, tile.yCoord, tile.zCoord)) {
                        list.add(tile);
                    }
                }
            }
        }

        Collections.sort(list, new Comparator<TileEntity>() {

            @Override
            public int compare(TileEntity a, TileEntity b) {
                if (a.xCoord != b.xCoord) return a.xCoord < b.xCoord ? -1 : 1;
                if (a.yCoord != b.yCoord) return a.yCoord < b.yCoord ? -1 : 1;
                return a.zCoord < b.zCoord ? -1 : a.zCoord == b.zCoord ? 0 : 1;
            }
        });
        return list;
    }

    /**
     * Gravitates an entity, vanilla xp orb style, towards a position Code adapted from EntityXPOrb and OpenBlocks
     * Vacuum Hopper, mostly the former
     */
    public static void gravitateEntityTowards(Entity ent, double x, double y, double z) {
        double dX = x - ent.posX;
        double dY = y - ent.posY;
        double dZ = z - ent.posZ;
        double dist = Math.sqrt(dX * dX + dY * dY + dZ * dZ);

        double vel = 1.0 - dist / 15.0;
        if (vel > 0.0D) {
            vel *= vel;
            ent.motionX += dX / dist * vel * 0.05;
            ent.motionY += dY / dist * vel * 0.1;
            ent.motionZ += dZ / dist * vel * 0.05;
            ent.moveEntity(ent.motionX, ent.motionY, ent.motionZ);
        }
    }

    /**
     * Grows the plants in the 11x7x11 blocks around the point, at random.
     *
     * @param harvest true to also break whatever is fully grown, and to grow twice as fast
     * @param player  whose permissions are checked when breaking; null for none (a pedestal)
     */
    public static void growNearbyRandomly(boolean harvest, World world, double xCoord, double yCoord, double zCoord,
        EntityPlayer player) {
        int growOdds = harvest ? 16 : 32;
        int centerX = MathHelper.floor_double(xCoord);
        int centerY = MathHelper.floor_double(yCoord);
        int centerZ = MathHelper.floor_double(zCoord);

        for (int x = centerX - 5; x <= centerX + 5; x++) {
            for (int y = centerY - 3; y <= centerY + 3; y++) {
                for (int z = centerZ - 5; z <= centerZ + 5; z++) {
                    Block plant = world.getBlock(x, y, z);

                    if (plant.getMaterial() == Material.air) {
                        continue;
                    }

                    if (plant instanceof IShearable) {
                        // Vines, leaves, tall grass, dead bushes, double plants
                        if (harvest) {
                            harvestBlock(world, x, y, z, player);
                        }
                    } else if (plant instanceof IGrowable) {
                        // Crops, cocoa, saplings, stems, mushrooms, and grass blocks (which sprout flowers and grass)
                        IGrowable growable = (IGrowable) plant;

                        if (harvest && !growable.func_149851_a(world, x, y, z, false)) {
                            harvestBlock(world, x, y, z, player);
                        } else if (world.rand.nextInt(growOdds) == 0
                            && (ProjectEConfig.harvBandGrass || !plant.getUnlocalizedName()
                                .toLowerCase(Locale.ROOT)
                                .contains("grass"))) {
                                    growable.func_149853_b(world, world.rand, x, y, z);
                                }
                    } else if (plant instanceof IPlantable) {
                        // Cactus, reeds, nether wart, flowers, and most modded plants: these grow on their own tick
                        if (world.rand.nextInt(growOdds / 4) == 0) {
                            for (int i = harvest ? 8 : 4; i > 0; i--) {
                                plant.updateTick(world, x, y, z, world.rand);
                            }
                        }

                        if (harvest) {
                            harvestPlantable(world, x, y, z, plant, player);
                        }
                    }
                }
            }
        }
    }

    private static void harvestPlantable(World world, int x, int y, int z, Block plant, EntityPlayer player) {
        if (plant instanceof BlockFlower) {
            harvestBlock(world, x, y, z, player);
        }

        if (plant == Blocks.reeds || plant == Blocks.cactus) {
            // Only when three high. Reeds keep their bottom block so they grow back
            if (world.getBlock(x, y + 1, z) == plant && world.getBlock(x, y + 2, z) == plant) {
                for (int up = plant == Blocks.reeds ? 1 : 0; up <= 2; up++) {
                    harvestBlock(world, x, y + up, z, player);
                }
            }
        }

        if (plant == Blocks.nether_wart && ((IPlantable) plant).getPlantMetadata(world, x, y, z) == 3) {
            harvestBlock(world, x, y, z, player);
        }
    }

    /** Breaks the block with its drops, unless the player is not allowed to. */
    private static void harvestBlock(World world, int x, int y, int z, EntityPlayer player) {
        if (player == null || PlayerHelper.hasBreakPermission((EntityPlayerMP) player, x, y, z)) {
            world.func_147480_a(x, y, z, true);
        }
    }

    /**
     * Recursively mines out a vein of the given Block, starting from the provided coordinates
     */
    public static void harvestVein(World world, EntityPlayer player, ItemStack stack, Coordinates coords, Block target,
        List<ItemStack> currentDrops, int numMined) {
        if (numMined >= Constants.MAX_VEIN_SIZE) {
            return;
        }

        AxisAlignedBB b = AxisAlignedBB
            .getBoundingBox(coords.x - 1, coords.y - 1, coords.z - 1, coords.x + 1, coords.y + 1, coords.z + 1);

        for (int x = (int) b.minX; x <= b.maxX; x++)
            for (int y = (int) b.minY; y <= b.maxY; y++) for (int z = (int) b.minZ; z <= b.maxZ; z++) {
                Block block = world.getBlock(x, y, z);

                if (block == target || (target == Blocks.lit_redstone_ore && block == Blocks.redstone_ore)) {
                    numMined++;
                    if (PlayerHelper.hasBreakPermission(((EntityPlayerMP) player), x, y, z)) {
                        currentDrops.addAll(getBlockDrops(world, player, block, stack, x, y, z));
                        world.setBlockToAir(x, y, z);
                        harvestVein(world, player, stack, new Coordinates(x, y, z), target, currentDrops, numMined);
                    }
                }
            }
    }

    /**
     * Lights fires at random in the 17x11x17 blocks around the player, each air block with a chance of 1 in
     * {@link #RANDOM_EFFECT_ODDS} per call.
     */
    public static void igniteNearby(World world, EntityPlayer player) {
        int minX = MathHelper.floor_double(player.posX) - 8;
        int minY = MathHelper.floor_double(player.posY) - 5;
        int minZ = MathHelper.floor_double(player.posZ) - 8;
        Random rand = world.rand;

        // Same rate per position as rolling for each of them, without visiting them all
        for (int n = randomPicks(rand, 17 * 11 * 17); n > 0; n--) {
            int x = minX + rand.nextInt(17);
            int y = minY + rand.nextInt(11);
            int z = minZ + rand.nextInt(17);

            // Fire with nothing to stand on or burn goes out the moment it is placed, so skip those
            if (world.isAirBlock(x, y, z) && Blocks.fire.canPlaceBlockAt(world, x, y, z)) {
                PlayerHelper.checkedPlaceBlock((EntityPlayerMP) player, x, y, z, Blocks.fire, 0);
            }
        }
    }

    public static boolean isArrowInGround(EntityArrow arrow) {
        return ReflectionHelper.getArrowInGround(arrow);
    }

    /**
     * Repels projectiles and mobs in the given AABB away from a given point
     */
    public static void repelEntitiesInAABBFromPoint(World world, AxisAlignedBB effectBounds, double x, double y,
        double z, boolean isSWRG) {
        List<Entity> list = world.getEntitiesWithinAABB(Entity.class, effectBounds);

        for (Entity ent : list) {
            if ((isSWRG && !swrgBlacklist.contains(ent.getClass()))
                || (!isSWRG && !interdictionBlacklist.contains(ent.getClass()))) {
                if ((ent instanceof EntityLiving) || (ent instanceof IProjectile)) {
                    if (!isSWRG && ProjectEConfig.interdictionMode
                        && !(ent instanceof IMob || ent instanceof IProjectile)) {
                        continue;
                    } else {
                        if (ent instanceof EntityArrow && ((EntityArrow) ent).onGround) {
                            continue;
                        }
                        double dx = ent.posX - x;
                        double dy = ent.posY - y;
                        double dz = ent.posZ - z;
                        double distance = Math.sqrt(dx * dx + dy * dy + dz * dz) + 0.1D;

                        ent.motionX += dx / 1.5D / distance;
                        ent.motionY += dy / 1.5D / distance;
                        ent.motionZ += dz / 1.5D / distance;
                    }
                }
            }
        }
    }

    public static void spawnEntityItem(World world, ItemStack stack, double x, double y, double z) {
        float f = world.rand.nextFloat() * 0.8F + 0.1F;
        float f1 = world.rand.nextFloat() * 0.8F + 0.1F;
        EntityItem entityitem;

        for (float f2 = world.rand.nextFloat() * 0.8F + 0.1F; stack.stackSize > 0; world
            .spawnEntityInWorld(entityitem)) {
            int j1 = world.rand.nextInt(21) + 10;

            if (j1 > stack.stackSize) j1 = stack.stackSize;

            stack.stackSize -= j1;
            entityitem = new EntityItem(
                world,
                (double) ((float) x + f),
                (double) ((float) y + f1),
                (double) ((float) z + f2),
                new ItemStack(stack.getItem(), j1, stack.getItemDamage()));
            float f3 = 0.05F;
            entityitem.motionX = (double) ((float) world.rand.nextGaussian() * f3);
            entityitem.motionY = (double) ((float) world.rand.nextGaussian() * f3 + 0.2F);
            entityitem.motionZ = (double) ((float) world.rand.nextGaussian() * f3);

            if (stack.hasTagCompound()) {
                entityitem.getEntityItem()
                    .setTagCompound(
                        (NBTTagCompound) stack.getTagCompound()
                            .copy());
            }
        }

    }
}
