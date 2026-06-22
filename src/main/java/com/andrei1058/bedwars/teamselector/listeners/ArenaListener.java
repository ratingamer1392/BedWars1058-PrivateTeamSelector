package com.andrei1058.bedwars.teamselector.listeners;

import com.andrei1058.bedwars.api.arena.GameState;
import com.andrei1058.bedwars.api.arena.IArena;
import com.andrei1058.bedwars.api.events.gameplay.GameStateChangeEvent;
import com.andrei1058.bedwars.api.events.player.PlayerJoinArenaEvent;
import com.andrei1058.bedwars.api.events.player.PlayerLeaveArenaEvent;
import com.andrei1058.bedwars.api.events.server.ArenaEnableEvent;
import com.andrei1058.bedwars.teamselector.Main;
import com.andrei1058.bedwars.teamselector.teamselector.ArenaPreferences;
import com.andrei1058.bedwars.teamselector.teamselector.TeamManager;
import com.andrei1058.bedwars.teamselector.teamselector.TeamSelectorAssigner;
import com.andrei1058.bedwars.teamselector.teamselector.TeamSelectorGUI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

public class ArenaListener implements Listener {

    @EventHandler(priority = EventPriority.HIGH)
    public void onBwArenaJoin(PlayerJoinArenaEvent e) {

        IPrivatePlayer pp = PrivateGames.api.getPrivatePlayer(e.getPlayer());
        IPlayerSettings p = pp.getPlayerSettings();
        IArena a = e.getArena();
        if (e.isCancelled()) return;
        if (a.isSpectator((Player) pp.getPlayer())) return;
        if (a.getStatus() == GameState.playing || a.getStatus() == GameState.restarting) return;
        if (p.isPrivateGameEnabled()) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    TeamSelectorGUI.giveItem(e.getPlayer(), null);
                    Bukkit.getLogger().info("[PrivateGames-TeamSelector] Giving Team Selector to " + e.getPlayer().getName());
                    for (Player allPlayer : a.getPlayers()) {
                        TeamSelectorGUI.giveItem(allPlayer, null);
                        Bukkit.getLogger().info("[PrivateGames-TeamSelector] Giving Team Selector to " + e.getPlayer().getName());
                    }
                }
            }.runTaskLater(Main.plugin, 30L);
        }
    }

    @EventHandler
    //Remove player from team
    public void onBwArenaLeave(@NotNull PlayerLeaveArenaEvent e) {
        IArena a = e.getArena();
        if (a.getStatus() == GameState.waiting || a.getStatus() == GameState.starting) {
            TeamManager.getInstance().onQuit(a, e.getPlayer());
        }
    }

    @EventHandler
    public void onStatusChange(@NotNull GameStateChangeEvent e) {
        if (e.getNewState() == GameState.starting) {

            ArenaPreferences pref = TeamManager.getInstance().getArena(e.getArena());
            if (pref == null) return;

            // do not start with a single team
            int size = e.getArena().getPlayers().size();
            int teams = pref.getTeamsCount();
            int members = pref.getMembersCount();
            if (size - members <= 0 && teams == 1) {
                e.getArena().setStatus(GameState.waiting);
            }
        }
        if (e.getNewState() == GameState.playing || e.getNewState() == GameState.restarting) {
            TeamManager.getInstance().clearArenaCache(e.getArena());
        }
    }

    @EventHandler
    public void onArenaLoad(@NotNull ArenaEnableEvent event) {
        event.getArena().setTeamAssigner(new TeamSelectorAssigner());
    }
}
