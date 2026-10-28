package com.pedrojm96.superlobby.listener;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import com.pedrojm96.core.CoreVariables;
import com.pedrojm96.core.command.CoreExecuteComands;
import com.pedrojm96.superlobby.AllString;
import com.pedrojm96.superlobby.RegionRule;
import com.pedrojm96.superlobby.SuperLobby;
import com.pedrojm96.superlobby.WorldGuardHook;


public class RegionListener implements Listener{
    private final SuperLobby plugin;
    // Which regions each player was in last time we checked.
    private final Map<UUID, Set<String>> current = new HashMap<UUID, Set<String>>();

    public RegionListener(SuperLobby plugin) {
        this.plugin = plugin;
    }

    /** Only do work if there are rules AND WorldGuard is actually present. */
    private boolean active() {
        return !plugin.regionRules.isEmpty() && WorldGuardHook.isInstalled();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent e) {
        if (!active()) return;
        Location from = e.getFrom();
        Location to = e.getTo();
        if (to == null) return;
        // Only re-check when the player crosses into a new block (performance).
        if (from.getBlockX() == to.getBlockX() && from.getBlockY() == to.getBlockY()
                && from.getBlockZ() == to.getBlockZ() && from.getWorld() == to.getWorld()) {
            return;
        }
        update(e.getPlayer());
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent e) { scheduleUpdate(e.getPlayer()); }

    @EventHandler
    public void onChangeWorld(PlayerChangedWorldEvent e) { scheduleUpdate(e.getPlayer()); }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) { scheduleUpdate(e.getPlayer()); }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent e) { scheduleUpdate(e.getPlayer()); }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        UUID id = e.getPlayer().getUniqueId();
        current.remove(id);
        plugin.hubDisabled.remove(id);
    }

    /** recheck on the next tick once the player's final position is settled. */
    private void scheduleUpdate(final Player p) {
        if (!active()) return;
        plugin.getInstance().getServer().getScheduler().scheduleSyncDelayedTask(plugin.getInstance(), new Runnable() {
            public void run() {
                if (p.isOnline()) update(p);
            }
        }, 1L);
    }

    /** compare the player's previous region set to now, and fire enter/leave. */
    private void update(Player p) {
        if (!active()) return;
        UUID id = p.getUniqueId();
        Set<String> now = plugin.worldGuard.regionAt(p.getLocation());
        Set<String> prev = current.containsKey(id) ? current.get(id) : Collections.<String>emptySet();
        current.put(id, now);

        for (String region : now) {
            if (!prev.contains(region)) onEnter(p, region);
        }
        for (String region : prev) {
            if (!now.contains(region)) onLeave(p, region, now);
        }
    }

    private void onEnter(Player p, String region) {
        RegionRule rule = plugin.regionRules.get(region);
        if (rule == null) return;
        if (rule.isDisableHubMode()) plugin.hubDisabled.add(p.getUniqueId());
        if (rule.isRemoveHubItems()) plugin.removeLobbyItems(p);
        runCommands(p, rule.getEnterCommands());
    }

    private void onLeave(Player p, String region, Set<String> now) {
        RegionRule rule = plugin.regionRules.get(region);
        if (rule == null) return;
        // Only turn hub mode back on if no OTHER region they're still in also disables it.
        if (rule.isDisableHubMode() && !stillHubDisabled(now, region)) {
            plugin.hubDisabled.remove(p.getUniqueId());
        }
        if (rule.isClearInventoryOnLeave()) plugin.clearFullInventory(p);
        if (rule.isRestoreHubItemsOnLeave()) plugin.giveLobbyItems(p);
        runCommands(p, rule.getLeaveCommands());

    }

    private boolean stillHubDisabled(Set<String> now, String leaving) {
        for (String region : now) {
            if (region.equals(leaving)) continue;
            RegionRule other = plugin.regionRules.get(region);
            if (other != null && other.isDisableHubMode()) return true;
        }
        return false;
    }

    private void runCommands(Player p, List<String> commands) {
        if (commands == null || commands.isEmpty()) return;
        for (String command : commands) {
            if (command == null || command.trim().isEmpty()) continue;
            try {
                new CoreExecuteComands(p, CoreVariables.replace(command, p), plugin.getInstance(), AllString.prefix).execute();
            } catch (Throwable t) {
                plugin.log.error("Region command failed (" + command + "): " + t.getMessage());
            }
        }
    }
}
