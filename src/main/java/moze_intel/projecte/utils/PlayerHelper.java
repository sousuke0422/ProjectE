package moze_intel.projecte.utils;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.function.Predicate;
import moze_intel.projecte.PECore;
import moze_intel.projecte.integration.IntegrationHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ClientboundAnimatePacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.BlockSnapshot;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.items.IItemHandler;

/**
 * Helper class for player-related methods. Notice: Please try to keep methods tidy and alphabetically ordered. Thanks!
 */
public final class PlayerHelper {

	public final static ObjectiveCriteria SCOREBOARD_EMC = new ReadOnlyScoreCriteria(PECore.MODID + ":emc_score");

	/**
	 * Tries placing a block and fires an event for it.
	 *
	 * @return Whether the block was successfully placed
	 */
	public static boolean checkedPlaceBlock(Player player, BlockPos pos, BlockState state) {
		return hasEditPermission(player, pos) && partiallyCheckedPlaceBlock(player, pos, state);
	}

	private static boolean partiallyCheckedPlaceBlock(Player player, BlockPos pos, BlockState state) {
		Level level = player.level();
		BlockSnapshot before = BlockSnapshot.create(level.dimension(), level, pos);
		level.setBlockAndUpdate(pos, state);
		BlockEvent.EntityPlaceEvent evt = new BlockEvent.EntityPlaceEvent(before, Blocks.AIR.defaultBlockState(), player);
		NeoForge.EVENT_BUS.post(evt);
		if (evt.isCanceled()) {
			level.restoringBlockSnapshots = true;
			before.restore(true, false);
			level.restoringBlockSnapshots = false;
			//PELogger.logInfo("Checked place block got canceled, restoring snapshot.");
			return false;
		}
		//PELogger.logInfo("Checked place block passed!");
		return true;
	}

	public static boolean checkedReplaceBlock(ServerPlayer player, BlockPos pos, BlockState state) {
		return hasBreakPermission(player, pos) && partiallyCheckedPlaceBlock(player, pos, state);
	}

	public static ItemStack findFirstItem(Player player, Item consumeFrom) {
		return player.getInventory().items.stream().filter(s -> !s.isEmpty() && s.is(consumeFrom)).findFirst().orElse(ItemStack.EMPTY);
	}

	public static boolean checkArmorHotbarCurios(Player player, Predicate<ItemStack> checker) {
		return player.getInventory().armor.stream().anyMatch(checker) || checkHotbarCurios(player, checker);
	}

	public static boolean checkHotbarCurios(Player player, Predicate<ItemStack> checker) {
		for (int i = 0; i < Inventory.getSelectionSize(); i++) {
			if (checker.test(player.getInventory().getItem(i))) {
				return true;
			}
		}
		if (checker.test(player.getOffhandItem())) {
			return true;
		}
		IItemHandler curios = player.getCapability(IntegrationHelper.CURIO_ITEM_HANDLER);
		if (curios != null) {
			for (int i = 0, slots = curios.getSlots(); i < slots; i++) {
				if (checker.test(curios.getStackInSlot(i))) {
					return true;
				}
			}
		}
		return false;
	}

	public static BlockHitResult getBlockLookingAt(Player player, double maxDistance) {
		return (BlockHitResult) player.pick(maxDistance, 1.0F, false);
	}

	/**
	 * Returns a vec representing where the player is looking, capped at maxDistance away.
	 */
	public static Vec3 getLookTarget(Player player, double maxDistance) {
		Vec3 lookAngle = player.getLookAngle();
		return player.getEyePosition().add(lookAngle.x * maxDistance, lookAngle.y * maxDistance, lookAngle.z * maxDistance);
	}

	public static boolean hasBreakPermission(ServerPlayer player, BlockPos pos) {
		return hasEditPermission(player, pos) && checkBreakPermission(player, pos);
	}

	static boolean checkBreakPermission(ServerPlayer player, BlockPos pos) {
		return CommonHooks.onBlockBreakEvent(player.level(), player.gameMode.getGameModeForPlayer(), player, pos) != -1;
	}

	public static boolean hasEditPermission(Player player, BlockPos pos) {
		return player.mayInteract(player.level(), pos) && Arrays.stream(Direction.values()).allMatch(e -> player.mayUseItemAt(pos, e, ItemStack.EMPTY));
	}

	public static void resetCooldown(Player player) {
		player.resetAttackStrengthTicker();
		PECore.packetHandler().resetCooldown((ServerPlayer) player);
	}

	public static void swingItem(Player player, InteractionHand hand) {
		if (player.level() instanceof ServerLevel level) {
			level.getChunkSource().broadcastAndSend(player, new ClientboundAnimatePacket(player, hand == InteractionHand.MAIN_HAND ? 0 : 3));
		}
	}

	public static void updateScore(ServerPlayer player, ObjectiveCriteria objective, BigInteger value) {
		updateScore(player, objective, value.compareTo(Constants.MAX_INTEGER) > 0 ? Integer.MAX_VALUE : value.intValueExact());
	}

	public static void updateScore(ServerPlayer player, ObjectiveCriteria objective, int value) {
		player.getScoreboard().forAllObjectives(objective, player, score -> score.set(value));
	}
}