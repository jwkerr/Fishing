package net.earthmc.fishing.command;

import net.earthmc.fishing.Fishing;
import net.earthmc.fishing.api.EventManager;
import net.earthmc.fishing.object.FishingEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FishingEventTownCommand implements TabExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            sender.sendMessage(Component.text("No method provided", NamedTextColor.RED));
            return true;
        }

        String method = args[0];
        switch (method) {
            case "top" -> sendTopList(sender);
            default -> sender.sendMessage(Component.text("Invalid method provided", NamedTextColor.RED));
        }

        return true;
    }

    private void sendTopList(CommandSender sender) {
        EventManager em = EventManager.getInstance();

        FishingEvent activeEvent = em.getActiveEvent();
        if (activeEvent == null) {
            sender.sendMessage(Component.text("There is no fishing event currently", NamedTextColor.RED));
            return;
        }

        if (!activeEvent.hasStarted()) {
            sender.sendMessage(Component.text("The fishing event has not started yet", NamedTextColor.RED));
            return;
        }

        Map<UUID, Integer> topTen = em.getActiveEvent().getNumFishCaught().entrySet().stream()
                .sorted(Map.Entry.<UUID, Integer>comparingByValue().reversed())
                .limit(10)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));

        List<Component> components = new ArrayList<>();

        for (Map.Entry<UUID, Integer> entry : topTen.entrySet()) {
            OfflinePlayer player = Bukkit.getOfflinePlayer(entry.getKey());

            String name = player.getName();
            if (name == null) continue;

            components.add(Component.text(name + ": " + entry.getValue(), Fishing.BLUE_COLOUR));
        }

        sender.sendMessage(Component.join(JoinConfiguration.newlines(), components));
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Stream<String> stream = switch (args.length) {
            case 1 -> Stream.of("top");
            default -> null;
        };

        if (stream == null) return null;

        return stream
                .filter(s -> s.startsWith(args[args.length - 1].toLowerCase()))
                .toList();
    }
}
