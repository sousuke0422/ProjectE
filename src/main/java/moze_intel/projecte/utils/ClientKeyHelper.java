package moze_intel.projecte.utils;

import net.minecraft.client.settings.KeyBinding;

import org.lwjgl.input.Keyboard;

import com.google.common.collect.ImmutableBiMap;

import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import moze_intel.projecte.PECore;

/**
 * Clientside key helper - because PEKeybind cannot touch client classes or it will crash dedicated servers
 */
@SideOnly(Side.CLIENT)
public class ClientKeyHelper {

    public static ImmutableBiMap<KeyBinding, PEKeybind> mcToPe;
    public static ImmutableBiMap<PEKeybind, KeyBinding> peToMc;

    public static void registerMCBindings() {
        ImmutableBiMap.Builder<KeyBinding, PEKeybind> builder = ImmutableBiMap.builder();
        for (PEKeybind k : PEKeybind.values()) {
            KeyBinding mcK = new KeyBinding(k.keyName, k.defaultKeyCode, PECore.MODID);
            builder.put(mcK, k);
            ClientRegistry.registerKeyBinding(mcK);
        }
        mcToPe = builder.build();
        peToMc = mcToPe.inverse();
    }

    /**
     * Get the key name this PEKeybind is bound to.
     */
    public static String getKeyName(PEKeybind k) {
        int keyCode = peToMc.get(k)
            .getKeyCode();
        if (keyCode > Keyboard.getKeyCount() || keyCode < 0) {
            return "INVALID KEY";
        }
        return Keyboard.getKeyName(keyCode);
    }

    public static String getKeyName(KeyBinding k) {
        int keyCode = k.getKeyCode();
        if (keyCode > Keyboard.getKeyCount() || keyCode < 0) {
            return "INVALID KEY";
        }
        return Keyboard.getKeyName(keyCode);
    }
}
