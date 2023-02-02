package moze_intel.projecte.network.commands;

import moze_intel.projecte.config.CustomEMCParser;
import moze_intel.projecte.emc.EMCMapper;
import moze_intel.projecte.handlers.TileEntityHandler;
import moze_intel.projecte.network.PacketHandler;

import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentTranslation;

public class ReloadEmcCMD extends ProjectEBaseCMD {

    @Override
    public String getCommandName() {
        return "projecte_reloadEMC";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/projecte reloadEMC";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] params) {
        sender.addChatMessage(new ChatComponentTranslation("pe.command.reload.started"));

        EMCMapper.clearMaps();
        CustomEMCParser.readUserData();
        EMCMapper.map();
        TileEntityHandler.checkAllCondensers();

        sender.addChatMessage(new ChatComponentTranslation("pe.command.reload.success"));

        PacketHandler.sendFragmentedEmcPacketToAll();
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 4;
    }
}
