package mytown.proxies.mod;

import cpw.mods.fml.common.event.FMLPostInitializationEvent;

/**
 * @author Joe Goett
 */
public abstract class ModProxy {

    public boolean isLoaded = false;
    /**
     * Returns the name of the ModProxy
     *
     * @return
     */
    public abstract String getName();

    /**
     * Returns the Mod ID of the mod this {@link ModProxy} interacts with.
     *
     * @return
     */
    public abstract String getModID();

    /**
     * Loads this {@link ModProxy}, its run during {@link FMLPostInitializationEvent}.
     */
    public abstract void load();
}