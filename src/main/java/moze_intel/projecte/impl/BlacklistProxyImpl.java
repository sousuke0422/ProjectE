package moze_intel.projecte.impl;

import moze_intel.projecte.api.proxy.IBlacklistProxy;
import moze_intel.projecte.gameObjs.items.TimeWatch;
import moze_intel.projecte.utils.NBTWhitelist;
import moze_intel.projecte.utils.PELogger;
import moze_intel.projecte.utils.WorldHelper;

import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

import com.google.common.base.Preconditions;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.LoaderState;

public class BlacklistProxyImpl implements IBlacklistProxy {

    public static final IBlacklistProxy instance = new BlacklistProxyImpl();

    private BlacklistProxyImpl() {}

    @Override
    public void blacklistInterdiction(Class<? extends Entity> clazz) {
        Preconditions.checkNotNull(clazz);
        Preconditions.checkState(
                Loader.instance().isInState(LoaderState.POSTINITIALIZATION),
                "Mod %s registering interdiction blacklist at incorrect time!",
                Loader.instance().activeModContainer().getModId());
        doBlacklistInterdiction(clazz, Loader.instance().activeModContainer().getModId());
    }

    @Override
    public void blacklistSwiftwolf(Class<? extends Entity> clazz) {
        Preconditions.checkNotNull(clazz);
        Preconditions.checkState(
                Loader.instance().isInState(LoaderState.POSTINITIALIZATION),
                "Mod %s registering SWRG repel at incorrect time!",
                Loader.instance().activeModContainer().getModId());
        doBlacklistSwiftwolf(clazz, Loader.instance().activeModContainer().getModId());
    }

    @Override
    public void blacklistTimeWatch(Class<? extends TileEntity> clazz) {
        Preconditions.checkNotNull(clazz);
        Preconditions.checkState(
                Loader.instance().isInState(LoaderState.POSTINITIALIZATION),
                "Mod %s registering TimeWatch blacklist at incorrect time!",
                Loader.instance().activeModContainer().getModId());
        doBlacklistTimewatch(clazz, Loader.instance().activeModContainer().getModId());
    }

    @Override
    public void whitelistNBT(ItemStack stack) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkState(
                Loader.instance().isInState(LoaderState.POSTINITIALIZATION),
                "Mod %s registering NBT whitelist at incorrect time!",
                Loader.instance().activeModContainer().getModId());
        doWhitelistNBT(stack, Loader.instance().activeModContainer().getModId());
    }

    /**
     * Split actual doing of whitelisting/blacklisting apart in order to log it properly from IMC
     */

    protected void doBlacklistInterdiction(Class<? extends Entity> clazz, String modName) {
        WorldHelper.blacklistInterdiction(clazz);
        PELogger.logInfo("Mod %s blacklisted %s for interdiction torch", modName, clazz.getCanonicalName());
    }

    protected void doBlacklistSwiftwolf(Class<? extends Entity> clazz, String modName) {
        WorldHelper.blacklistSwrg(clazz);
        PELogger.logInfo("Mod %s blacklisted %s for SWRG repel", modName, clazz.getCanonicalName());
    }

    protected void doBlacklistTimewatch(Class<? extends TileEntity> clazz, String modName) {
        TimeWatch.blacklist(clazz);
        PELogger.logInfo("Mod %s blacklisted %s for Time Watch acceleration", modName, clazz.getCanonicalName());
    }

    protected void doWhitelistNBT(ItemStack s, String modName) {
        NBTWhitelist.register(s);
        PELogger.logInfo("Mod %s whitelisted %s for NBT duping", modName, s.toString());
    }
}
