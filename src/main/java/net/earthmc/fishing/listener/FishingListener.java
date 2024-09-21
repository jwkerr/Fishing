package net.earthmc.fishing.listener;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Town;
import net.earthmc.fishing.Fishing;
import net.earthmc.fishing.api.EventManager;
import net.earthmc.fishing.object.FishingEvent;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Set;

public class FishingListener implements Listener {

    public static final Set<Material> VALID_FISH = Set.of(Material.COD, Material.SALMON, Material.PUFFERFISH, Material.TROPICAL_FISH);

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerFish(PlayerFishEvent event) {
        EventManager em = EventManager.getInstance();
        FishingEvent fe = em.getActiveEvent();
        if (fe == null || !fe.hasStarted()) return;

        if (!event.getState().equals(PlayerFishEvent.State.CAUGHT_FISH)) return;

        Player player = event.getPlayer();

        Town town = TownyAPI.getInstance().getTown(player.getLocation());
        if (town == null) return;

        if (!town.equals(fe.getTown())) return;

        Item item = (Item) event.getCaught();
        if (item == null) return;

        ItemStack itemStack = item.getItemStack();
        boolean isFish = VALID_FISH.contains(itemStack.getType());

        if (isFish) fe.addCaughtFish(player.getUniqueId());

        player.sendActionBar(Component.text("You have caught " + fe.getNumFishCaught(player.getUniqueId()) + " fish", Fishing.BLUE_COLOUR));
    }
}
