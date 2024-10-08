package net.earthmc.fishing.object;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Town;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import net.earthmc.fishing.Fishing;
import net.earthmc.fishing.api.EventManager;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class FishingEvent {

    private final Town town;
    private final int duration;
    private final int delay;
    private final long createdAt;

    private Long startedAt;
    private BossBar bossBar;

    private ScheduledTask scheduledEnding;
    private ScheduledTask bossBarTask;

    public final Map<UUID, Integer> numFishCaught = new ConcurrentHashMap<>();

    public FishingEvent(Town town, int duration, int delay) {
        this.town = town;
        this.duration = duration;
        this.delay = delay;
        this.createdAt = Instant.now().getEpochSecond();
    }

    public void register() {
        EventManager.getInstance().setActiveEvent(this);
        Fishing instance = Fishing.getInstance();

        instance.getServer().getAsyncScheduler().runDelayed(instance, task -> {
            startedAt = Instant.now().getEpochSecond();
            scheduleEnding();
        }, delay, TimeUnit.MINUTES);

        bossBarTask = instance.getServer().getAsyncScheduler().runAtFixedRate(instance, task -> updateBossBar(), 1, 1, TimeUnit.SECONDS);
    }

    public void cleanup() {
        if (hasStarted()) scheduledEnding.cancel();
        bossBarTask.cancel();
    }

    private void scheduleEnding() {
        Fishing instance = Fishing.getInstance();
        scheduledEnding = instance.getServer().getAsyncScheduler().runDelayed(instance, task -> EventManager.getInstance().endEvent(true), duration, TimeUnit.MINUTES);
    }

    public void addCaughtFish(UUID uuid) {
        int numCaught = getNumFishCaught(uuid);
        numFishCaught.put(uuid, numCaught + 1);
    }

    /**
     * @return An audience of all players currently standing in the town
     */
    public Audience getAudience() {
        List<Player> playersInTown = new ArrayList<>();

        for (Player player : Bukkit.getOnlinePlayers()) {
            Town townAtLocation = TownyAPI.getInstance().getTown(player.getLocation());
            if (townAtLocation == null) continue;

            if (townAtLocation.equals(town)) playersInTown.add(player);
        }

        return Audience.audience(playersInTown);
    }

    public void updateBossBar() {
        Component name;
        float progress;

        if (!hasStarted()) {
            name = Component.text("Please wait for the fishing event to start!", Fishing.BLUE_COLOUR);

            long timePassed = Instant.now().getEpochSecond() - createdAt;
            int totalDelayinSeconds = delay * 60;

            progress = 1F - Math.min((float) timePassed / totalDelayinSeconds, 1F);
        } else {
            Component dash = Component.text(" - ", NamedTextColor.DARK_GRAY);

            TextComponent.Builder builder = Component.text();
            builder.append(Component.text(town.getName(), Fishing.BLUE_COLOUR));
            builder.append(dash);
            builder.append(Component.text("Total fish caught: " + getTotalFishCaught(), Fishing.BLUE_COLOUR));

            OfflinePlayer lead = getLeadPlayer();
            if (lead != null) {
                builder.append(dash);
                builder.append(Component.text("Lead: " + lead.getName() + " (" + getLeadEntry().getValue() + ")", Fishing.BLUE_COLOUR));
            }

            name = builder.build();
            progress = getProgress();
        }

        if (bossBar == null) {
            bossBar = BossBar.bossBar(name, progress, BossBar.Color.BLUE, BossBar.Overlay.NOTCHED_6);
            getAudience().forEachAudience(a -> a.showBossBar(bossBar));
            return;
        }

        bossBar.name(name);
        bossBar.progress(progress);
    }

    public @Nullable Map.Entry<UUID, Integer> getLeadEntry() {
        Map.Entry<UUID, Integer> currentLead = null;

        for (Map.Entry<UUID, Integer> entry : numFishCaught.entrySet()) {
            if (currentLead == null) {
                currentLead = entry;
                continue;
            }

            if (entry.getValue() > currentLead.getValue()) currentLead = entry;
        }

        return currentLead;
    }

    public @Nullable OfflinePlayer getLeadPlayer() {
        Map.Entry<UUID, Integer> leadEntry = getLeadEntry();
        if (leadEntry == null) return null;

        return Bukkit.getOfflinePlayer(leadEntry.getKey());
    }

    public float getProgress() {
        long timePassed = Instant.now().getEpochSecond() - startedAt;
        int totalDurationInSeconds = duration * 60;

        float progress = (float) timePassed / totalDurationInSeconds;

        return Math.min(progress, 1F);
    }

    public int getTotalFishCaught() {
        int totalFishCaught = 0;

        for (Map.Entry<UUID, Integer> entry : numFishCaught.entrySet()) {
            totalFishCaught += entry.getValue();
        }

        return totalFishCaught;
    }

    public boolean hasStarted() {
        return startedAt != null;
    }

    public int getNumFishCaught(@NotNull UUID uuid) {
        return numFishCaught.getOrDefault(uuid, 0);
    }

    public @NotNull Map<UUID, Integer> getNumFishCaught() {
        return numFishCaught;
    }

    public @NotNull BossBar getBossBar() {
        return bossBar;
    }

    public @NotNull Town getTown() {
        return town;
    }
}
