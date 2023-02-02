package moze_intel.projecte.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import moze_intel.projecte.network.commands.ChangelogCMD;
import moze_intel.projecte.utils.PELogger;

import net.minecraft.client.Minecraft;
import net.minecraft.event.ClickEvent;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.StatCollector;

import xyz.akirin.fmpe.FMPEMain;

import com.google.common.collect.Lists;

public class ThreadCheckUpdate extends Thread {

    private static boolean hasRunServer = false;
    private static boolean hasRunClient = false;
    private final String changelogURL = "https://raw.githubusercontent.com/TeamBlackCrystal/FMProjectE/MC17/Changelog.txt";
    private final String changelogDevURL = "https://raw.githubusercontent.com/TeamBlackCrystal/FMProjectE/MC17/ChangelogDev.txt";
    private final String githubURL = "https://github.com/TeamBlackCrystal/FMProjectE";
    private final String curseURL = "https://bit.ly/2G4KC4j";
    private boolean isServerSide;

    public ThreadCheckUpdate(boolean isServer) {
        this.isServerSide = isServer;
        this.setName("ProjectE Update Checker " + (isServer ? "Server" : "Client"));
    }

    @Override
    public void run() {
        HttpURLConnection connection = null;
        BufferedReader reader = null;

        try {
            connection = (HttpURLConnection) new URL(changelogURL).openConnection();

            connection.connect();

            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            String line = reader.readLine();

            if (line == null) {
                PELogger.logFatal("Update check failed!");
                throw new IOException("No data from github changelog!");
            }

            String latestVersion;
            List<String> changes = Lists.newArrayList();

            latestVersion = line.substring(11);
            latestVersion = latestVersion.trim();

            while ((line = reader.readLine()) != null) {
                if (line.startsWith("###Version")) {
                    break;
                }

                if (!line.isEmpty()) {
                    line = line.substring(1).trim();
                    changes.add(line);
                }
            }

            if (!FMPEMain.RVersion.equals(latestVersion)) {
                PELogger.logInfo(
                        "Mod is outdated! Check " + curseURL + " to get the latest version (" + latestVersion + ").");

                for (String s : changes) {
                    PELogger.logInfo(s);
                }

                if (isServerSide) {
                    ChangelogCMD.changelog.addAll(changes);
                } else {
                    Minecraft.getMinecraft().thePlayer.addChatMessage(
                            new ChatComponentText(
                                    String.format(
                                            EnumChatFormatting.GOLD
                                                    + StatCollector.translateToLocal("pe.update.available"),
                                            latestVersion)));
                    Minecraft.getMinecraft().thePlayer.addChatMessage(
                            new ChatComponentText(
                                    EnumChatFormatting.GREEN + StatCollector.translateToLocal("pe.update.getit")));

                    IChatComponent link = new ChatComponentText(EnumChatFormatting.AQUA + curseURL);
                    link.getChatStyle().setChatClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, curseURL));
                    Minecraft.getMinecraft().thePlayer.addChatMessage(link);

                    Minecraft.getMinecraft().thePlayer.addChatMessage(
                            new ChatComponentText(
                                    EnumChatFormatting.GREEN + StatCollector.translateToLocal("pe.update.changelog")));
                }
            } else {
                PELogger.logInfo("Mod is updated.");
            }
        } catch (Exception e) {
            PELogger.logFatal("Caught exception in Update Checker thread!");
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    PELogger.logFatal("Caught exception in Update Checker thread!");
                    e.printStackTrace();
                }
            }

            if (connection != null) {
                connection.disconnect();
            }

            if (isServerSide) {
                hasRunServer = true;
            } else {
                hasRunClient = true;
            }
        }
    }

    public static boolean hasRunServer() {
        return hasRunServer;
    }

    public static boolean hasRunClient() {
        return hasRunClient;
    }
}
