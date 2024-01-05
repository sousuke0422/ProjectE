package moze_intel.projecte.gameObjs.items.armor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import moze_intel.projecte.utils.WorldHelper;
import moze_intel.projecte.utils.text.PELang;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GemLegs extends GemArmorBase {

	public GemLegs(Properties props) {
		super(ArmorItem.Type.LEGGINGS, props);
		NeoForge.EVENT_BUS.addListener(this::onJump);
	}

	@Override
	public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltips, @NotNull TooltipFlag flags) {
		super.appendHoverText(stack, level, tooltips, flags);
		tooltips.add(PELang.GEM_LORE_LEGS.translate());
	}

	private final Map<Integer, Long> lastJumpTracker = new HashMap<>();

	private void onJump(LivingEvent.LivingJumpEvent evt) {
		if (evt.getEntity() instanceof Player player && player.level().isClientSide) {
			lastJumpTracker.put(player.getId(), player.level().getGameTime());
		}
	}

	private boolean jumpedRecently(Player player) {
		return lastJumpTracker.containsKey(player.getId()) && player.level().getGameTime() - lastJumpTracker.get(player.getId()) < 5;
	}

	@Override
	public void onArmorTick(ItemStack stack, Level level, Player player) {
		if (level.isClientSide) {
			if (player.isSecondaryUseActive() && !player.onGround() && player.getDeltaMovement().y() > -8 && !jumpedRecently(player)) {
				player.setDeltaMovement(player.getDeltaMovement().add(0, -0.32F, 0));
			}
		}
		if (player.isSecondaryUseActive()) {
			AABB box = new AABB(player.getX() - 3.5, player.getY() - 3.5, player.getZ() - 3.5,
					player.getX() + 3.5, player.getY() + 3.5, player.getZ() + 3.5);
			WorldHelper.repelEntitiesSWRG(level, box, player);
			if (!level.isClientSide && player.getDeltaMovement().y() < -0.08) {
				List<Entity> entities = player.level().getEntities(player, player.getBoundingBox().move(player.getDeltaMovement()).inflate(2.0D),
						entity -> entity instanceof LivingEntity);
				for (Entity e : entities) {
					if (e.isPickable()) {
						e.hurt(level.damageSources().playerAttack(player), (float) -player.getDeltaMovement().y() * 6F);
					}
				}
			}
		}
	}
}