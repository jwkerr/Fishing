package net.earthmc.fishing.command;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Town;
import net.earthmc.fishing.api.EventManager;
import net.earthmc.fishing.object.FishingEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Stream;

public class FishingEventTownyAdminTownCommand implements TabExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Town town = TownyAPI.getInstance().getTown(args[0]);
        if (town == null) {
            sender.sendMessage(Component.text("Specified town is invalid", NamedTextColor.RED));
            return true;
        }

        if (args.length < 3) {
            sender.sendMessage(Component.text("No method provided", NamedTextColor.RED));
            return true;
        }

        String method = args[2];
        switch (method) {
            case "start" -> startFishingEvent(sender, town, args);
            default -> sender.sendMessage(Component.text("Invalid method provided", NamedTextColor.RED));
        }

        return true;
    }

    private void startFishingEvent(CommandSender sender, Town town, String[] args) {
        if (EventManager.getInstance().getActiveEvent() != null) {
            sender.sendMessage(Component.text("A fishing event is already running"));
            return;
        }

        if (args.length < 5) {
            sender.sendMessage(Component.text("Duration and delay not provided", NamedTextColor.RED));
            return;
        }

        int duration;
        int delay;
        try {
            duration = Integer.parseInt(args[3]);
            delay = Integer.parseInt(args[4]);
        } catch (NumberFormatException e) {
            sender.sendMessage(Component.text("Invalid duration or delay provided", NamedTextColor.RED));
            return;
        }

        FishingEvent fe = new FishingEvent(town, duration, delay);
        fe.register();
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Stream<String> stream = switch (args.length) {
            case 2 -> Stream.of("start");
            case 3 -> switch (args[1]) {
                case "start" -> Stream.of("{duration_ticks}");
                default -> null;
            };
            case 4 -> switch (args[1]) {
                case "start" -> Stream.of("{delay_ticks}");
                default -> null;
            };
            default -> null;
        };

        if (stream == null) return null;

        return stream
                .filter(s -> s.startsWith(args[args.length - 1].toLowerCase()))
                .toList();
    }
}
