package moze_intel.projecte.gameObjs.items.rings;

import java.util.List;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntitySnowball;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

import baubles.api.BaubleType;
import baubles.api.IBauble;
import cpw.mods.fml.common.Optional;
import moze_intel.projecte.api.item.IExtraFunction;
import moze_intel.projecte.api.item.IModeChanger;
import moze_intel.projecte.api.item.IProjectileShooter;
import moze_intel.projecte.config.ProjectEConfig;
import moze_intel.projecte.gameObjs.entity.EntityFireProjectile;
import moze_intel.projecte.gameObjs.entity.EntitySWRGProjectile;
import moze_intel.projecte.gameObjs.items.IFireProtector;
import moze_intel.projecte.gameObjs.items.IFlightProvider;
import moze_intel.projecte.gameObjs.items.ItemPE;
import moze_intel.projecte.utils.PlayerHelper;
import moze_intel.projecte.utils.WorldHelper;

/**
 * The four elemental rings in one. The item damage is the mode; the ring only acts while switched on.
 */
@Optional.Interface(iface = "baubles.api.IBauble", modid = "Baubles")
public class Arcana extends ItemPE
    implements IBauble, IModeChanger, IFlightProvider, IFireProtector, IExtraFunction, IProjectileShooter {

    private static final int ZERO = 0;
    private static final int IGNITION = 1;
    private static final int HARVEST = 2;
    private static final int SWRG = 3;
    private static final int MODES = 4;

    private static final String ACTIVE = "Active";
    /** World time of the last row of fire, for {@link ProjectEConfig#arcanaFireRowCooldown}. */
    private static final String LAST_FIRE_ROW = "LastFireRow";

    /** The row of fire reaches this far to the player's left and right, front and back, and up and down. */
    private static final int FIRE_ROW_LENGTH = 30;
    private static final int FIRE_ROW_DEPTH = 3;
    private static final int FIRE_ROW_HEIGHT = 5;

    private final IIcon[] icons = new IIcon[MODES];
    private final IIcon[] iconsOn = new IIcon[MODES];

    public Arcana() {
        super();
        setUnlocalizedName("arcana_ring");
        setMaxStackSize(1);
        setNoRepair();
        setContainerItem(this);
    }

    @Override
    public boolean doesContainerItemLeaveCraftingGrid(ItemStack stack) {
        return false;
    }

    @Override
    public byte getMode(ItemStack stack) {
        return (byte) stack.getItemDamage();
    }

    @Override
    public void changeMode(EntityPlayer player, ItemStack stack) {
        stack.setItemDamage((stack.getItemDamage() + 1) % MODES);
    }

    private static boolean isActive(ItemStack stack) {
        return stack.hasTagCompound() && stack.getTagCompound()
            .getBoolean(ACTIVE);
    }

    private static NBTTagCompound tagOf(ItemStack stack) {
        if (!stack.hasTagCompound()) {
            stack.setTagCompound(new NBTTagCompound());
        }
        return stack.getTagCompound();
    }

    @Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
        if (!world.isRemote) {
            NBTTagCompound tag = tagOf(stack);
            tag.setBoolean(ACTIVE, !tag.getBoolean(ACTIVE));
        }
        return stack;
    }

    /** The passive effect of the current mode, around the player. */
    private void applyPassive(ItemStack stack, World world, EntityPlayerMP player) {
        if (!isActive(stack)) {
            return;
        }

        switch (stack.getItemDamage()) {
            case ZERO:
                WorldHelper.freezeInBoundingBox(world, player.boundingBox.expand(5, 5, 5), player, true);
                break;
            case IGNITION:
                WorldHelper.igniteNearby(world, player);
                break;
            case HARVEST:
                WorldHelper.growNearbyRandomly(true, world, player.posX, player.posY, player.posZ, player);
                break;
            case SWRG:
                WorldHelper.repelEntitiesInAABBFromPoint(
                    world,
                    player.boundingBox.expand(5, 5, 5),
                    player.posX,
                    player.posY,
                    player.posZ,
                    true);
                break;
            default:
                break;
        }
    }

    @Override
    public void onUpdate(ItemStack stack, World world, Entity entity, int slot, boolean held) {
        tagOf(stack);

        // Only on the hotbar
        if (!world.isRemote && slot <= 8 && entity instanceof EntityPlayerMP) {
            applyPassive(stack, world, (EntityPlayerMP) entity);
        }
    }

    @Override
    @Optional.Method(modid = "Baubles")
    public BaubleType getBaubleType(ItemStack stack) {
        return BaubleType.RING;
    }

    @Override
    @Optional.Method(modid = "Baubles")
    public void onWornTick(ItemStack stack, EntityLivingBase entity) {
        tagOf(stack);

        if (!entity.worldObj.isRemote && entity instanceof EntityPlayerMP) {
            applyPassive(stack, entity.worldObj, (EntityPlayerMP) entity);
        }
    }

    @Override
    @Optional.Method(modid = "Baubles")
    public void onEquipped(ItemStack stack, EntityLivingBase player) {}

    @Override
    @Optional.Method(modid = "Baubles")
    public void onUnequipped(ItemStack stack, EntityLivingBase player) {}

    @Override
    @Optional.Method(modid = "Baubles")
    public boolean canEquip(ItemStack stack, EntityLivingBase player) {
        return true;
    }

    @Override
    @Optional.Method(modid = "Baubles")
    public boolean canUnequip(ItemStack stack, EntityLivingBase player) {
        return true;
    }

    @Override
    public IIcon getIcon(ItemStack stack, int pass) {
        return getIconIndex(stack);
    }

    @Override
    public IIcon getIconIndex(ItemStack stack) {
        int mode = MathHelper.clamp_int(stack.getItemDamage(), 0, MODES - 1);
        return isActive(stack) ? iconsOn[mode] : icons[mode];
    }

    @Override
    public void registerIcons(IIconRegister register) {
        for (int mode = 0; mode < MODES; mode++) {
            icons[mode] = register.registerIcon(this.getTexture("rings", "arcana_" + mode));
            iconsOn[mode] = register.registerIcon(this.getTexture("rings", "arcana_" + mode + "_on"));
        }
        itemIcon = icons[0];
    }

    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean advanced) {
        if (!stack.hasTagCompound()) {
            return;
        }

        if (isActive(stack)) {
            list.add(
                StatCollector.translateToLocal("pe.arcana.mode") + EnumChatFormatting.AQUA
                    + StatCollector.translateToLocal("pe.arcana.mode." + stack.getItemDamage()));
        } else {
            list.add(EnumChatFormatting.RED + StatCollector.translateToLocal("pe.arcana.inactive"));
        }
    }

    /** In ignition mode: a wide row of fire across the player's view. */
    @Override
    public void doExtraFunction(ItemStack stack, EntityPlayer player) {
        World world = player.worldObj;

        if (world.isRemote || stack.getItemDamage() != IGNITION) {
            return;
        }

        int cooldown = ProjectEConfig.arcanaFireRowCooldown;
        if (cooldown > 0) {
            NBTTagCompound tag = tagOf(stack);
            long now = world.getTotalWorldTime();
            long since = now - tag.getLong(LAST_FIRE_ROW);
            if (tag.hasKey(LAST_FIRE_ROW) && since >= 0 && since < cooldown) {
                return;
            }
            tag.setLong(LAST_FIRE_ROW, now);
        }

        // Facing north or south the row runs east-west, and the other way round
        int facing = MathHelper.floor_double(player.rotationYaw * 4.0F / 360.0F + 0.5) & 3;
        boolean alongX = facing == 0 || facing == 2;
        int reachX = alongX ? FIRE_ROW_LENGTH : FIRE_ROW_DEPTH;
        int reachZ = alongX ? FIRE_ROW_DEPTH : FIRE_ROW_LENGTH;
        int centerX = MathHelper.floor_double(player.posX);
        int centerY = MathHelper.floor_double(player.posY);
        int centerZ = MathHelper.floor_double(player.posZ);

        for (int x = centerX - reachX; x <= centerX + reachX; x++) {
            for (int y = centerY - FIRE_ROW_HEIGHT; y <= centerY + FIRE_ROW_HEIGHT; y++) {
                for (int z = centerZ - reachZ; z <= centerZ + reachZ; z++) {
                    // Fire with nothing to stand on or burn goes out the moment it is placed, so skip those
                    if (world.isAirBlock(x, y, z) && Blocks.fire.canPlaceBlockAt(world, x, y, z)) {
                        PlayerHelper.checkedPlaceBlock((EntityPlayerMP) player, x, y, z, Blocks.fire, 0);
                    }
                }
            }
        }
        world.playSoundAtEntity(player, "projecte:item.pepower", 1.0F, 1.0F);
    }

    @Override
    public boolean shootProjectile(EntityPlayer player, ItemStack stack) {
        World world = player.worldObj;

        if (world.isRemote) {
            return false;
        }

        switch (stack.getItemDamage()) {
            case ZERO:
                world.spawnEntityInWorld(new EntitySnowball(world, player));
                world.playSoundAtEntity(player, "random.bow", 1.0F, 1.0F);
                break;
            case IGNITION:
                world.spawnEntityInWorld(new EntityFireProjectile(world, player));
                world.playSoundAtEntity(player, "projecte:item.pepower", 1.0F, 1.0F);
                break;
            case SWRG:
                world.spawnEntityInWorld(new EntitySWRGProjectile(world, player));
                break;
            default:
                // The harvest mode has nothing to shoot
                break;
        }

        return true;
    }

    @Override
    public boolean canProtectAgainstFire(ItemStack stack, EntityPlayerMP player) {
        return true;
    }

    @Override
    public boolean canProvideFlight(ItemStack stack, EntityPlayerMP player) {
        return true;
    }
}
