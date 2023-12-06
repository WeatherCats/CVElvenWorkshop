package org.cubeville.cvelvenworkshop.models;

import net.md_5.bungee.api.chat.TextComponent;
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
import org.bukkit.util.Vector;
import org.cubeville.cvelvenworkshop.CVElvenWorkshop;
import org.cubeville.cvelvenworkshop.elvenworkshop.ElvenWorkshop;
import org.cubeville.cvelvenworkshop.guis.ForgeMenuGUI;
import org.cubeville.cvgames.utils.GameUtils;

import java.util.ArrayList;
import java.util.List;

import static org.bukkit.Bukkit.getServer;

public class Forge implements Listener {
    Location location;
    ItemDisplay itemDisplay;
    TextDisplay textDisplay;
    Interaction interaction;
    List<ForgeSlot> slots = new ArrayList<>();
    ElvenWorkshop game;
    BukkitTask task;
    Double modifier = 0.0;

    public Forge(ElvenWorkshop game, Location location) {
        this.game = game;
        this.location = location;
        slots.add(new ForgeSlot(this));
    }

    public void activate() {
        spawnItemDisplay();
        spawnTextDisplay();
        spawnInteraction();
        task = Bukkit.getScheduler().runTaskTimer(CVElvenWorkshop.getInstance(), () -> {
            for (ForgeSlot slot : slots) {
                slot.incrementProgress();
            }
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
        ForgeMenuGUI gui = new ForgeMenuGUI(this, p);
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
        itemDisplay.setItemStack(new ItemStack(Material.BLAST_FURNACE, 1));
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
        String string = GameUtils.createColorString("&#555555&lGift Forge");
        for (int i = 0; i < slots.size(); i++) {
            string = string + "\n" + slots.get(i).getProgressBar();
        }
        textDisplay.setText(string);
    }

    private String getProgressBar(Integer i) {
        ForgeSlot slot = slots.get(i);
        if (slot.isCrafting) {
            Float prop = slot.getProgress()/((float) slot.getModifiedDuration());
            Integer bars = (int) Math.floor(prop*20);
            String bar = "▋";
            return GameUtils.createColorString("&a" + bar.repeat(bars) + "&7" + bar.repeat(20-bars));
        }
        else {
            return GameUtils.createColorString("&7Inactive");
        }
    }

    public void remove() {
        itemDisplay.remove();
        textDisplay.remove();
        interaction.remove();
        HandlerList.unregisterAll(this);
        task.cancel();
    }

    public Integer getNextSlotCost() {
        return CVElvenWorkshop.getConfigData().getInt("forge-upgrades." + slots.size());
    }

    public void addSlot() {
        slots.add(new ForgeSlot(this));
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

    public List<ForgeSlot> getSlots() {
        return slots;
    }

    public ElvenWorkshop getGame() {
        return game;
    }

    public BukkitTask getTask() {
        return task;
    }

    public Double getModifier() {
        return modifier;
    }

    public void setModifier(Double modifier) {
        this.modifier = modifier;
    }
}
