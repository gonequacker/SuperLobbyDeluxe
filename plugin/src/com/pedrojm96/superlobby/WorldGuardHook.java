package com.pedrojm96.superlobby;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Location;


import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;

public class WorldGuardHook {

    /** True only when WorldGuard is installed and enabled on this server.
     * When WorlgGuard isn't on the server these classes aren't loaded */
    public static boolean isInstalled() {
        return Bukkit.getPluginManager().isPluginEnabled("WorldGuard");
    }


    /**
     * Returns lower-case ids of every WorldGuard region covering this
     * location. Empty set if WorldGuard is missing or anything goes wrong.
     */
    public Set<String> regionAt (Location loc) {
        if (loc == null || loc.getWorld() == null || !isInstalled()) {
            return Collections.emptySet();
        }
        try {
            RegionManager rm = WorldGuardPlugin.inst().getRegionManager(loc.getWorld());
            if (rm == null) {
                return Collections.emptySet();
            }
            ApplicableRegionSet set = rm.getApplicableRegions(loc);
            Set<String> ids = new HashSet<String>();

            for (ProtectedRegion region : set) {
                ids.add(region.getId().toLowerCase());
            }

            return ids;
        }catch (Throwable t ){
            //never lets WG/WE version issues break player movement
            return  Collections.emptySet();

        }
    }
}
