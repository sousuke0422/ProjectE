package moze_intel.projecte.gameObjs.items.itemBlocks;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import moze_intel.projecte.utils.AchievementHandler;

public class ItemRMFurnaceBlock extends ItemBlock {

    public ItemRMFurnaceBlock(Block block) {
        super(block);
    }

    @Override
    public void onCreated(ItemStack stack, World world, EntityPlayer player) {
        if (world != null) {
            player.addStat(AchievementHandler.RM_FURNACE, 1);
        }
    }
}
