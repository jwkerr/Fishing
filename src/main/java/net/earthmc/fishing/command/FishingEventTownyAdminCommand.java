package net.earthmc.fishing.command;

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

public class FishingEventTownyAdminCommand implements TabExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            sender.sendMessage(Component.text("No method provided", NamedTextColor.RED));
            return true;
        }

        String method = args[0];
        switch (method) {
            case "cancel" -> cancelFishingEvent(sender, args);
            default -> sender.sendMessage(Component.text("Invalid method provided", NamedTextColor.RED));
        }

        return true;
    }

    private void cancelFishingEvent(CommandSender sender, String[] args) {
        EventManager em = EventManager.getInstance();

        FishingEvent activeEvent = em.getActiveEvent();
        if (activeEvent == null) {
            sender.sendMessage(Component.text("There are no fishing events currently", NamedTextColor.RED));
            return;
        }

        boolean ceremoniously;
        try {
            ceremoniously = Boolean.parseBoolean(args[1]);
        } catch (IndexOutOfBoundsException e) {
            ceremoniously = true;
        }

        em.endEvent(ceremoniously);
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Stream<String> stream = switch (args.length) {
            case 1 -> Stream.of("cancel");
            case 2 -> switch (args[0]) {
                case "cancel" -> Stream.of("true", "false");
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
