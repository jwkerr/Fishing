package net.earthmc.fishing.api;

import net.earthmc.fishing.Fishing;
import net.earthmc.fishing.object.FishingEvent;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class EventManager {

    private static EventManager instance;

    private FishingEvent activeEvent;

    private EventManager() {}

    public static EventManager getInstance() {
        if (instance == null) instance = new EventManager();
        return instance;
    }

    /**
     * @param ceremoniously If false, the event will end unceremoniously with no celebrations
     */
    public void endEvent(boolean ceremoniously) {
        if (activeEvent == null) return;

        activeEvent.cleanup();

        for (Player player : Bukkit.getOnlinePlayers()) {
            player.hideBossBar(activeEvent.getBossBar());
        }

        if (ceremoniously && activeEvent.hasStarted()) {
            OfflinePlayer lead = activeEvent.getLeadPlayer();
            if (lead == null) {
                activeEvent.getAudience().sendMessage(Component.text("The fishing event has concluded, nobody won :(", Fishing.BLUE_COLOUR));
            } else {
                activeEvent.getAudience().sendMessage(Component.text("The fishing event has concluded, the winner was " + lead.getName() + " with " + activeEvent.getNumFishCaught(lead.getUniqueId()) + " fish!", Fishing.BLUE_COLOUR));
            }
        }

        this.activeEvent = null;
    }

    public void setActiveEvent(@NotNull FishingEvent activeEvent) {
        this.activeEvent = activeEvent;
    }

    public @Nullable FishingEvent getActiveEvent() {
        return activeEvent;
    }
}
