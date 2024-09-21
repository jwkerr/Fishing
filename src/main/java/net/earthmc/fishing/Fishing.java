package net.earthmc.fishing;

import com.palmergames.bukkit.towny.TownyCommandAddonAPI;
import net.earthmc.fishing.command.FishingEventTownyAdminCommand;
import net.earthmc.fishing.command.FishingEventTownyAdminTownCommand;
import net.earthmc.fishing.listener.BossBarListener;
import net.earthmc.fishing.listener.FishingListener;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class Fishing extends JavaPlugin {

    private static Fishing instance;

    public static final TextColor BLUE_COLOUR = TextColor.color(20, 200, 200);

    @Override
    public void onEnable() {
        registerCommands();
        registerListeners();
    }

    @Override
    public void onLoad() {
        instance = this;
    }

    public static Fishing getInstance() {
        return instance;
    }

    private void registerCommands() {
        TownyCommandAddonAPI.addSubCommand(TownyCommandAddonAPI.CommandType.TOWNYADMIN_TOWN, "fishingevent", new FishingEventTownyAdminTownCommand());
        TownyCommandAddonAPI.addSubCommand(TownyCommandAddonAPI.CommandType.TOWNYADMIN, "fishingevent", new FishingEventTownyAdminCommand());
    }

    private void registerListeners() {
        PluginManager pm = getServer().getPluginManager();

        pm.registerEvents(new BossBarListener(), this);
        pm.registerEvents(new FishingListener(), this);
    }
}
