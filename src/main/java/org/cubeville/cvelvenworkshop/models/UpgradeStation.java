package org.cubeville.cvelvenworkshop.models;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Transformation;
import org.cubeville.cvelvenworkshop.CVElvenWorkshop;
import org.cubeville.cvelvenworkshop.elvenworkshop.ElvenWorkshop;
import org.cubeville.cvelvenworkshop.guis.UpgradeMenuGUI;
import org.cubeville.cvelvenworkshop.managers.UpgradeManager;
import org.cubeville.cvgames.utils.GameUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.bukkit.Bukkit.getServer;

public class UpgradeStation implements Listener {
    Location location;
    ItemDisplay itemDisplay;
    TextDisplay textDisplay;
    Interaction interaction;
    Map<Upgrade, Integer> upgradeLevels = new HashMap<>();
    ElvenWorkshop game;
    BukkitTask task;

    public UpgradeStation(ElvenWorkshop game, Location location) {
        this.game = game;
        this.location = location;
        for (Upgrade upgrade : UpgradeManager.getUpgrades().values()) {
            upgradeLevels.put(upgrade, 0);
        }
    }

    public void activate() {
        spawnItemDisplay();
        spawnTextDisplay();
        spawnInteraction();
        task = Bukkit.getScheduler().runTaskTimer(CVElvenWorkshop.getInstance(), () -> {
            itemDisplay.setRotation(itemDisplay.getLocation().getYaw()+2, 0);
            updateTextDisplay();
        }, 1, 1);
    }

    @EventHandler
    public void onInteract(PlayerInteractEntityEvent e) {
        if (e.getRightClicked() != interaction) return;
        if (!game.inGame(e.getPlayer())) return;
        openMenu(e.getPlayer());
    }

    public void openMenu(Player p) {
        UpgradeMenuGUI gui = new UpgradeMenuGUI(this, p);
        getServer().getPluginManager().registerEvents(gui, CVElvenWorkshop.getInstance());
        gui.openInventory(p);
    }

    private void spawnInteraction() {
        interaction = location.getWorld().spawn(location.clone().add(0, 1.5, 0), Interaction.class);
        interaction.setInteractionHeight(1);
        interaction.setInteractionWidth(1);
    }

    private void spawnItemDisplay() {
        itemDisplay = location.getWorld().spawn(location.clone().add(0, 2, 0), ItemDisplay.class);
        itemDisplay.setItemStack(new ItemStack(Material.SMITHING_TABLE, 1));
        Transformation transform = itemDisplay.getTransformation();
        transform.getScale().set(5);
    }

    private void spawnTextDisplay() {
        textDisplay = location.getWorld().spawn(location.clone().add(0, 2.75, 0), TextDisplay.class);
        updateTextDisplay();
        Transformation transform = textDisplay.getTransformation();
        transform.getScale().set(1.5);
        textDisplay.setBillboard(Display.Billboard.CENTER);
        textDisplay.setDefaultBackground(false);
        textDisplay.setShadowed(true);
    }

    private void updateTextDisplay() {
        String string = GameUtils.createColorString("&#884444&lUpgrade Station");
        textDisplay.setText(string);
    }

    public void remove() {
        itemDisplay.remove();
        textDisplay.remove();
        interaction.remove();
        HandlerList.unregisterAll(this);
        task.cancel();
    }

    public Location getLocation() {
        return location;
    }

    public ItemDisplay getItemDisplay() {
        return itemDisplay;
    }

    public TextDisplay getTextDisplay() {
        return textDisplay;
    }

    public ElvenWorkshop getGame() {
        return game;
    }

    public BukkitTask getTask() {
        return task;
    }

    public Map<Upgrade, Integer> getUpgradeLevels() {
        return upgradeLevels;
    }

    public void setUpgradeLevels(Map<Upgrade, Integer> upgradeLevels) {
        this.upgradeLevels = upgradeLevels;
    }
}
