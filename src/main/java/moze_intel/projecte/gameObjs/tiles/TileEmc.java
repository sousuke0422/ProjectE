package moze_intel.projecte.gameObjs.tiles;

import java.util.Arrays;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

import moze_intel.projecte.api.tile.IEmcAcceptor;
import moze_intel.projecte.api.tile.IEmcProvider;
import moze_intel.projecte.api.tile.TileEmcBase;
import moze_intel.projecte.utils.Constants;

public abstract class TileEmc extends TileEmcBase {

    private final TileEntity[] cachedNeighbors = new TileEntity[6];
    private long neighborCacheTick = Long.MIN_VALUE;

    public TileEmc() {
        setMaximumEMC(Constants.TILE_MAX_EMC);
    }

    public TileEmc(int maxAmount) {
        setMaximumEMC(maxAmount);
    }

    public boolean hasMaxedEmc() {
        return getStoredEmc() >= getMaximumEmc();
    }

    public void invalidateSpatialCaches() {
        this.neighborCacheTick = Long.MIN_VALUE;
    }

    protected TileEntity[] getCachedNeighbors() {
        if (this.worldObj == null) {
            return this.cachedNeighbors;
        }
        long currentTick = this.worldObj.getTotalWorldTime();
        if (this.neighborCacheTick != currentTick) {
            for (int i = 0; i < 6; i++) {
                ForgeDirection dir = ForgeDirection.VALID_DIRECTIONS[i];
                TileEntity candidate = this.worldObj
                    .getTileEntity(this.xCoord + dir.offsetX, this.yCoord + dir.offsetY, this.zCoord + dir.offsetZ);
                this.cachedNeighbors[i] = (candidate != null && !candidate.isInvalid()) ? candidate : null;
            }
            this.neighborCacheTick = currentTick;
        }
        return this.cachedNeighbors;
    }

    protected TileEntity getValidNeighbor(int dirOrdinal) {
        if (this.worldObj == null || dirOrdinal < 0 || dirOrdinal >= 6) {
            return null;
        }
        TileEntity[] neighbors = getCachedNeighbors();
        TileEntity neighbor = neighbors[dirOrdinal];
        if (neighbor != null && neighbor.isInvalid()) {
            ForgeDirection dir = ForgeDirection.VALID_DIRECTIONS[dirOrdinal];
            TileEntity refreshed = this.worldObj
                .getTileEntity(this.xCoord + dir.offsetX, this.yCoord + dir.offsetY, this.zCoord + dir.offsetZ);
            neighbor = (refreshed != null && !refreshed.isInvalid()) ? refreshed : null;
            neighbors[dirOrdinal] = neighbor;
        }
        return neighbor;
    }

    protected TileEntity getCachedNeighbor(ForgeDirection dir) {
        if (dir == null || dir == ForgeDirection.UNKNOWN) {
            return null;
        }
        return getValidNeighbor(dir.ordinal());
    }

    @Override
    public void invalidate() {
        super.invalidate();
        invalidateSpatialCaches();
        Arrays.fill(this.cachedNeighbors, null);
    }

    @Override
    public void onChunkUnload() {
        super.onChunkUnload();
        invalidateSpatialCaches();
        Arrays.fill(this.cachedNeighbors, null);
    }

    /**
     * The amount provided will be divided and evenly distributed as best as possible between adjacent IEMCAcceptors
     * Remainder or rejected EMC is added back to this provider
     *
     * @param emc The maximum combined emc to send to others
     */
    public void sendToAllAcceptors(double emc) {
        if (!(this instanceof IEmcProvider)) {
            // todo move this method somewhere
            throw new UnsupportedOperationException("sending without being a provider");
        }

        int acceptorCount = 0;
        for (int i = 0; i < 6; i++) {
            TileEntity te = getValidNeighbor(i);
            if (te instanceof IEmcAcceptor) {
                acceptorCount++;
            }
        }

        if (acceptorCount == 0) {
            return;
        }

        double emcPer = emc / acceptorCount;
        boolean isRelay = this instanceof RelayMK1Tile;
        for (int i = 0; i < 6; i++) {
            TileEntity te = getValidNeighbor(i);
            if (!(te instanceof IEmcAcceptor)) {
                continue;
            }
            if (isRelay && te instanceof RelayMK1Tile) {
                continue;
            }
            ForgeDirection dir = ForgeDirection.VALID_DIRECTIONS[i];
            double provide = ((IEmcProvider) this).provideEMC(dir.getOpposite(), emcPer);
            double remain = provide - ((IEmcAcceptor) te).acceptEMC(dir, provide);
            this.addEMC(remain);
        }
    }
}
