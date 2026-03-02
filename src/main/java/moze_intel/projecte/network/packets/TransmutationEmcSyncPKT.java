package moze_intel.projecte.network.packets;

import net.minecraft.client.Minecraft;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import moze_intel.projecte.gameObjs.container.TransmutationContainer;
import moze_intel.projecte.gameObjs.gui.GUITransmutation;

public class TransmutationEmcSyncPKT implements IMessage {

    private double emc;

    public TransmutationEmcSyncPKT() {}

    public TransmutationEmcSyncPKT(double emc) {
        this.emc = emc;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        emc = buf.readDouble();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeDouble(emc);
    }

    public static class Handler implements IMessageHandler<TransmutationEmcSyncPKT, IMessage> {

        @Override
        public IMessage onMessage(TransmutationEmcSyncPKT pkt, MessageContext ctx) {
            if (Minecraft.getMinecraft().thePlayer == null) {
                return null;
            }
            double emc = pkt.emc;
            // 表示中のGUIを更新（SPではopenContainerと別のことがある）
            if (Minecraft.getMinecraft().currentScreen instanceof GUITransmutation) {
                ((GUITransmutation) Minecraft.getMinecraft().currentScreen).setEmcFromSync(emc);
            }
            // openContainer側も更新（スロット操作等で参照される場合）
            if (Minecraft.getMinecraft().thePlayer.openContainer instanceof TransmutationContainer) {
                TransmutationContainer container = (TransmutationContainer) Minecraft
                    .getMinecraft().thePlayer.openContainer;
                container.transmutationInventory.emc = emc;
                container.transmutationInventory.updateOutputs(true);
            }

            return null;
        }
    }
}
