package moze_intel.projecte.gameObjs.items.rings;

import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

import com.google.common.collect.Lists;

import baubles.api.BaubleType;
import baubles.api.IBauble;
import cpw.mods.fml.common.Optional;
import moze_intel.projecte.api.item.IPedestalItem;
import moze_intel.projecte.config.ProjectEConfig;
import moze_intel.projecte.gameObjs.tiles.DMPedestalTile;
import moze_intel.projecte.handlers.PlayerTimers;
import moze_intel.projecte.utils.MathUtils;

@Optional.Interface(iface = "baubles.api.IBauble", modid = "Baubles")
public class LifeStone extends RingToggle implements IBauble, IPedestalItem {

    public LifeStone() {
        super("life_stone");
        this.setNoRepair();
    }

    @Override
    public void onUpdate(ItemStack stack, World world, Entity entity, int par4, boolean par5) {
        if (world.isRemote || par4 > 8 || !(entity instanceof EntityPlayer)) {
            return;
        }

        super.onUpdate(stack, world, entity, par4, par5);

        EntityPlayer player = (EntityPlayer) entity;

        if (stack.getItemDamage() != 0) {
            if (!consumeFuel(player, stack, 2 * 64, false)) {
                stack.setItemDamage(0);
            } else {
                PlayerTimers.activateFeed(player);
                PlayerTimers.activateHeal(player);

                if (player.getHealth() < player.getMaxHealth() && PlayerTimers.canHeal(player)) {
                    world.playSoundAtEntity(player, "projecte:item.peheal", 1.0F, 1.0F);
                    player.heal(2.0F);
                    removeEmc(stack, 64);
                }

                if (player.getFoodStats()
                    .needFood() && PlayerTimers.canFeed(player)) {
                    world.playSoundAtEntity(player, "projecte:item.peheal", 1.0F, 1.0F);
                    player.getFoodStats()
                        .addStats(2, 10);
                    removeEmc(stack, 64);
                }
            }
        }
    }

    @Override
    public void changeMode(EntityPlayer player, ItemStack stack) {
        if (stack.getItemDamage() == 0) {
            if (getEmc(stack) < 64 && !consumeFuel(player, stack, 64, false)) {
                // NOOP (used to be sounds)
            } else {
                stack.setItemDamage(1);
            }
        } else {
            stack.setItemDamage(0);
        }
    }

    @Override
    @Optional.Method(modid = "Baubles")
    public baubles.api.BaubleType getBaubleType(ItemStack itemstack) {
        return BaubleType.AMULET;
    }

    @Override
    @Optional.Method(modid = "Baubles")
    public void onWornTick(ItemStack stack, EntityLivingBase player) {
        this.onUpdate(stack, player.worldObj, player, 0, false);
    }

    @Override
    @Optional.Method(modid = "Baubles")
    public void onEquipped(ItemStack itemstack, EntityLivingBase player) {}

    @Override
    @Optional.Method(modid = "Baubles")
    public void onUnequipped(ItemStack itemstack, EntityLivingBase player) {}

    @Override
    @Optional.Method(modid = "Baubles")
    public boolean canEquip(ItemStack stack, EntityLivingBase player) {
        return true;
    }

    @Override
    @Optional.Method(modid = "Baubles")
    public boolean canUnequip(ItemStack itemstack, EntityLivingBase player) {
        return true;
    }

    @Override
    public void updateInPedestal(World world, int x, int y, int z) {
        if (!world.isRemote && ProjectEConfig.lifePedCooldown != -1) {
            DMPedestalTile tile = ((DMPedestalTile) world.getTileEntity(x, y, z));
            if (tile.getActivityCooldown() == 0) {
                List<EntityPlayerMP> players = world
                    .getEntitiesWithinAABB(EntityPlayerMP.class, tile.getEffectBounds());

                for (EntityPlayerMP player : players) {
                    if (player.getHealth() < player.getMaxHealth()) {
                        world.playSoundAtEntity(player, "projecte:item.peheal", 1.0F, 1.0F);
                        player.heal(1.0F); // 1/2 heart
                    }
                    if (player.getFoodStats()
                        .needFood()) {
                        world.playSoundAtEntity(player, "projecte:item.peheal", 1.0F, 1.0F);
                        player.getFoodStats()
                            .addStats(1, 1); // 1/2 shank
                    }
                }

                tile.setActivityCooldown(ProjectEConfig.lifePedCooldown);
            } else {
                tile.decrementActivityCooldown();
            }
        }
    }

    @Override
    public List<String> getPedestalDescription() {
        List<String> list = Lists.newArrayList();
        if (ProjectEConfig.lifePedCooldown != -1) {
            list.add(EnumChatFormatting.BLUE + StatCollector.translateToLocal("pe.life.pedestal1"));
            list.add(
                EnumChatFormatting.BLUE + String.format(
                    StatCollector.translateToLocal("pe.life.pedestal2"),
                    MathUtils.tickToSecFormatted(ProjectEConfig.lifePedCooldown)));
        }
        return list;
    }
}
