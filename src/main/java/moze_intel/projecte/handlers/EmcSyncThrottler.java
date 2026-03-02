package moze_intel.projecte.handlers;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;

import moze_intel.projecte.gameObjs.container.TransmutationContainer;
import moze_intel.projecte.network.PacketHandler;
import moze_intel.projecte.network.packets.TransmutationEmcSyncPKT;

/**
 * EMC同期パケットの送信をスロットルする。
 * SP/MP共に最大500ms間隔で送信する。
 */
public final class EmcSyncThrottler {

    private static final int THROTTLE_TICKS = 10; // 500ms (20tps)
    private static final Map<String, PendingSync> PENDING = new HashMap<>();

    /**
     * EMC同期を要求する。500ms間隔で送信される（スロットル）。
     */
    public static void requestSync(EntityPlayerMP player, double emc) {
        String key = player.getCommandSenderName();
        PendingSync sync = PENDING.get(key);
        if (sync == null) {
            sync = new PendingSync();
            sync.ticksUntilSend = THROTTLE_TICKS;
            PENDING.put(key, sync);
        }
        sync.emc = emc;
    }

    /**
     * サーバーTickごとに呼び出す。500ms間隔でパケットを送信する。
     */
    public static void tick() {
        for (Iterator<Map.Entry<String, PendingSync>> it = PENDING.entrySet()
            .iterator(); it.hasNext();) {
            Map.Entry<String, PendingSync> entry = it.next();
            PendingSync sync = entry.getValue();
            sync.ticksUntilSend--;
            if (sync.ticksUntilSend <= 0) {
                EntityPlayerMP player = findPlayer(entry.getKey());
                if (player != null && player.openContainer instanceof TransmutationContainer) {
                    PacketHandler.sendTo(new TransmutationEmcSyncPKT(sync.emc), player);
                    sync.ticksUntilSend = THROTTLE_TICKS; // 次回送信まで500ms
                } else {
                    it.remove(); // 錬成盤を閉じた等で削除
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static EntityPlayerMP findPlayer(String name) {
        for (EntityPlayer player : (List<EntityPlayer>) MinecraftServer.getServer()
            .getConfigurationManager().playerEntityList) {
            if (name.equals(player.getCommandSenderName())) {
                return (EntityPlayerMP) player;
            }
        }
        return null;
    }

    /**
     * プレイヤー切断時に呼び出し、保留中の同期をクリアする。
     */
    public static void removePlayer(EntityPlayer player) {
        PENDING.remove(player.getCommandSenderName());
    }

    private static class PendingSync {

        double emc;
        int ticksUntilSend;
    }
}
