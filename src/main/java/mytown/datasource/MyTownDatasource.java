package mytown.datasource;

import mytown.api.events.*;
import mytown.core.utils.Log;
import mytown.entities.*;

import java.util.Hashtable;
import java.util.Map;
import java.util.UUID;

/**
 * @author Joe Goett
 */
public abstract class MyTownDatasource {
    protected Log log = null;

    protected Map<String, Resident> residents = new Hashtable<String, Resident>();
    protected Map<String, Town> towns = new Hashtable<String, Town>();
    protected Map<String, Nation> nations = new Hashtable<String, Nation>();
    protected Map<String, Block> blocks = new Hashtable<String, Block>();
    protected Map<String, Plot> plots = new Hashtable<String, Plot>();
    protected Map<String, Rank> ranks = new Hashtable<String, Rank>();

    public final Map<String, Resident> getResidentsMap() {
        return residents;
    }

    public final Map<String, Town> getTownsMap() {
        return towns;
    }

    public final Map<String, Nation> getNationsMap() {
        return nations;
    }

    public final Map<String, Block> getBlocksMap() {
        return blocks;
    }

    public final Map<String, Plot> getPlotsMap() {
        return plots;
    }

    public final Map<String, Rank> getRanksMap() {
        return ranks;
    }

    /**
     * Sets the Log the Datasource uses
     * @param log
     */
    public final void setLog(Log log) {
        this.log = log;
    }

    /**
     * Initialize the Datasource.
     * This should create a connection to the database.
     * @return If false is returned, MyTown is put into safe-mode
     */
    public abstract boolean initialize();

    /* ----- Create ----- */

    /**
     * Creates and returns a new Town, or null if it couldn't be created
     * @return
     */
    public final Town newTown(String name) {
        Town town = new Town(name);
        if (TownEvent.fire(new TownEvent.TownCreateEvent(town)))
            return null;
        return town;
    }

    /**
     * Creates and returns a new Block, or null if it couldn't be created
     * @return
     */
    public final Block newBlock(int dim, int x,int z, Town town) {
        Block block = new Block(dim, x, z, town);
        if (BlockEvent.fire(new BlockEvent.BlockCreateEvent(block)))
            return null;
        return block;
    }

    /**
     * Creates and returns a new Rank, or null if it couldn't be created
     * @return
     */
    public final Rank newRank(String name, Town town) {
        Rank rank = new Rank(name, town);
        if (RankEvent.fire(new RankEvent.RankCreateEvent(rank)))
            return null;
        return rank;
    }

    /**
     * Creates and returns a new Resident, or null if it couldn't be created
     * @return
     */
    public final Resident newResident(String uuid) {
        Resident resident = new Resident(uuid);
        if (ResidentEvent.fire(new ResidentEvent.ResidentCreateEvent(resident)))
            return null;
        return resident;
    }

    /**
     * Creates and returns a new Plot, or null if it couldn't be created
     * @return
     */
    public final Plot newPlot(String name, Town town, int dim, int x1, int y1, int z1, int x2, int y2, int z2) {
        Plot plot = new Plot(name, town, dim, x1, y1, z1, x2, y2, z2);
        if (PlotEvent.fire(new PlotEvent.PlotCreateEvent(plot)))
            return null;
        return plot;
    }

    /**
     * Creates and returns a new Nation, or null if it couldn't be created
     * @return
     */
    public final Nation newNation(String name) {
        Nation nation = new Nation(name);
        if (NationEvent.fire(new NationEvent.NationCreateEvent(nation)))
            return null;
        return nation;
    }

    /* ----- Read ----- */

    public boolean loadAll() { // TODO Change load order?
        return loadTowns() && loadBlocks() && loadRanks() && loadResidents() && loadPlots() && loadNations();
    }

    protected abstract boolean loadTowns();

    protected abstract boolean loadBlocks();

    protected abstract boolean loadRanks();

    protected abstract boolean loadResidents();

    protected abstract boolean loadPlots();

    protected abstract boolean loadNations();

    /* ----- Save ----- */

    public abstract boolean saveTown(Town town);

    public abstract boolean saveBlock(Block block);

    public abstract boolean saveRank(Rank rank);

    public abstract boolean saveResident(Resident resident);

    public abstract boolean savePlot(Plot plot);

    public abstract boolean saveNation(Nation nation);

    /* ----- Delete ----- */

    public abstract boolean deleteTown(Town town);

    public abstract boolean deleteBlock(Block block);

    public abstract boolean deleteRank(Rank rank);

    public abstract boolean deleteResident(Resident resident);

    public abstract boolean deletePlot(Plot plot);

    public abstract boolean deleteNation(Nation nation);

    /* ----- Has ----- */

    public final boolean hasTown(String townName) {
        return towns.containsKey(townName);
    }

    public final boolean hasBlock(int dim, int x, int z) {
        return blocks.containsKey(String.format(Block.keyFormat, dim, x, z));
    }


    /**
     * Checks if the TownBlock with the given coords and dim at the town specified exists
     *
     * @param dim
     * @param x
     * @param z
     * @param inChunkCoords
     *            true if x and z are in chunk coordinates, false otherwise
     * @return
     */
    public boolean hasTownBlock(int dim, int x, int z, boolean inChunkCoords, Town town) {
        String key;
        if (inChunkCoords) {
            key = String.format(Block.keyFormat, dim, x, z);
        } else {
            key = String.format(Block.keyFormat, dim, x >> 4, z >> 4);
        }

        Block b = blocks.get(key);
        if (town != null && b != null && b.getTown() == town)
            return true;

        return hasBlock(dim, x, z);
    }

    /* ----- Helper ----- */

    /**
     * Gets or makes a new Resident, optionally saving it. CAN return null!
     * @param uuid
     * @param save
     * @return
     */
    public Resident getOrMakeResident(UUID uuid, boolean save) {
        Resident res = residents.get(uuid.toString());
        if (res == null) {
            res = newResident(uuid.toString());
            if (save && res != null) { // Only save if a new Resident
                if (!saveResident(res)) { // If saving fails, return null
                    return null;
                }
            }
        }
        return res;
    }

    /**
     * Gets or makes a new Resident. Does NOT save, and CAN return null!
     * @param uuid
     * @return
     */
    public Resident getOrMakeResident(UUID uuid) {
        return getOrMakeResident(uuid, false);
    }

    /**
     * Returns the Block at the given location. Can return null if it doesn't exist!
     * @param dim
     * @param chunkX
     * @param chunkZ
     * @return
     */
    public Block getBlock(int dim, int chunkX, int chunkZ) {
        return blocks.get(String.format(Block.keyFormat, dim, chunkX, chunkZ));
    }
}