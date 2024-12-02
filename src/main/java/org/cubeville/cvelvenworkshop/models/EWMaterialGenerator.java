package org.cubeville.cvelvenworkshop.models;

import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.TextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.cubeville.cvelvenworkshop.CVElvenWorkshop;
import org.cubeville.cvelvenworkshop.elvenworkshop.ElvenWorkshop;
import org.cubeville.cvelvenworkshop.guis.ForgeMenuGUI;
import org.cubeville.cvelvenworkshop.guis.GeneratorUpgradesGUI;
import org.cubeville.cvgames.utils.GameUtils;

import java.awt.*;

import static org.bukkit.Bukkit.getServer;

public class EWMaterialGenerator implements Listener {
    EWMaterial material;
    Location location;
    ElvenWorkshop game;
    ItemDisplay itemDisplay;
    TextDisplay textDisplay;
    Interaction interaction;
    int speed;
    int amount;
    int level = 1;
    int progress;
    BukkitTask task;

    public EWMaterialGenerator(ElvenWorkshop game, EWMaterial material, Location location) {
        this.game = game;
        this.material = material;
        this.location = location;
        this.speed = material.getConfig().getInt("levels.1.speed");
        this.amount = material.getConfig().getInt("levels.1.amount");
    }

    public void activate() {
        spawnItemDisplay();
        spawnTextDisplay();
        spawnInteraction();
        task = Bukkit.getScheduler().runTaskTimer(CVElvenWorkshop.getInstance(), () -> {
            progress++;
            itemDisplay.setRotation(itemDisplay.getLocation().getYaw()+2, 0);
            if (progress >= speed*20) {
                Item spawnedItem = location.getWorld().spawn(location.clone().add(0, 2, 0), Item.class);
                ItemStack item = material.getItem();
                item.setAmount(amount);
                spawnedItem.setItemStack(item);
                spawnedItem.setVelocity(new Vector(0, 0, 0));
                progress = 0;
            }
            updateTextDisplay();
        }, 1, 1);
    }

    @EventHandler
    public void onInteract(PlayerInteractEntityEvent e) {
        if (e.getRightClicked() != interaction) return;
        if (!game.inGame(e.getPlayer())) return;
        if (e.getPlayer().isSneaking()) {
            game.sendQuickChat(e.getPlayer(), "I collected " + material.getColorCode() + material.getDisplayName() + " &fgenerator");
            return;
        }
        openMenu(e.getPlayer());
    }

    public void openMenu(Player p) {
        GeneratorUpgradesGUI gui = new GeneratorUpgradesGUI(this, p);
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
        itemDisplay.setItemStack(material.getItem());
        Transformation transform = itemDisplay.getTransformation();
        transform.getScale().set(5);
    }

    private void spawnTextDisplay() {
        textDisplay = location.getWorld().spawn(location.clone().add(0, 2.75, 0), TextDisplay.class);
        textDisplay.setLineWidth(250);
        updateTextDisplay();
        Transformation transform = textDisplay.getTransformation();
        transform.getScale().set(1.5);
        textDisplay.setBillboard(Display.Billboard.CENTER);
        textDisplay.setDefaultBackground(false);
        textDisplay.setShadowed(true);
    }

    private void updateTextDisplay() {
        textDisplay.setText(GameUtils.createColorString(material.getColorCode() +
                material.getDisplayName() +
                " Generator &f(Level " + level + ")&r\n&f"
                + getProgressBar() +
                "\n&f\uD83D\uDDE8&6Shift Right Click&f: \n&fCollected this generator"));
    }

    private String getProgressBar() {
        Float prop = progress/(20f*speed);
        Integer bars = (int) Math.floor(prop*20);
        String bar = "▋";
        return GameUtils.createColorString("&a" + bar.repeat(bars) + "&7" + bar.repeat(20-bars));
    }

    public void remove() {
        itemDisplay.remove();
        textDisplay.remove();
        interaction.remove();
        HandlerList.unregisterAll(this);
        task.cancel();
    }

    public void upgrade() {
        this.level += 1;
        this.speed = material.getConfig().getInt("levels." + level + ".speed");
        this.amount = material.getConfig().getInt("levels." + level + ".amount");
    }

    public EWMaterial getMaterial() {
        return material;
    }

    public ElvenWorkshop getGame() {
        return game;
    }

    public int getSpeed() {
        return speed;
    }

    public int getAmount() {
        return amount;
    }

    public int getLevel() {
        return level;
    }

    public int getProgress() {
        return progress;
    }
}
