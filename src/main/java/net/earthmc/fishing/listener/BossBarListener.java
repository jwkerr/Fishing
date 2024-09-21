package net.earthmc.fishing.listener;

import com.palmergames.bukkit.towny.event.player.PlayerEntersIntoTownBorderEvent;
import com.palmergames.bukkit.towny.event.player.PlayerExitsFromTownBorderEvent;
import net.earthmc.fishing.api.EventManager;
import net.earthmc.fishing.object.FishingEvent;
import net.kyori.adventure.bossbar.BossBar;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class BossBarListener implements Listener {

    @EventHandler
    public void onPlayerEnterTown(PlayerEntersIntoTownBorderEvent event) {
        EventManager em = EventManager.getInstance();
        FishingEvent fe = em.getActiveEvent();
        if (fe == null) return;

        if (!event.getEnteredTown().equals(fe.getTown())) return;

        BossBar bossBar = fe.getBossBar();
        event.getPlayer().showBossBar(bossBar);
    }

    @EventHandler
    public void onPlayerExitTown(PlayerExitsFromTownBorderEvent event) {
        EventManager em = EventManager.getInstance();
        FishingEvent fe = em.getActiveEvent();
        if (fe == null) return;

        BossBar bossBar = fe.getBossBar();
        event.getPlayer().hideBossBar(bossBar);
    }
}
