package moze_intel.projecte.events;

import moze_intel.projecte.network.PacketHandler;
import moze_intel.projecte.network.packets.KeyPressPKT;
import moze_intel.projecte.utils.ClientKeyHelper;

import net.minecraft.client.settings.KeyBinding;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.InputEvent.KeyInputEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class KeyPressEvent {

    @SubscribeEvent
    public void keyPress(KeyInputEvent event) {
        for (KeyBinding k : ClientKeyHelper.mcToPe.keySet()) {
            if (k.isPressed()) {
                PacketHandler.sendToServer(new KeyPressPKT(ClientKeyHelper.mcToPe.get(k)));
            }
        }
    }
}
