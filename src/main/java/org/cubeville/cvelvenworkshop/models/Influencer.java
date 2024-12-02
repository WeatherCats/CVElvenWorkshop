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
import org.cubeville.cvelvenworkshop.guis.InfluencerMenuGUI;
import org.cubeville.cvelvenworkshop.managers.GiftCategoryManager;
import org.cubeville.cvgames.utils.GameUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.bukkit.Bukkit.getServer;

public class Influencer implements Listener {
    Location location;
    ItemDisplay itemDisplay;
    TextDisplay textDisplay;
    Interaction interaction;
    GiftCategory activeCategory = null;
    HashMap<GiftCategory, Integer> categoryLevels = new HashMap<>();
    ElvenWorkshop game;
    BukkitTask task;
    Double modifier = 0.0;

    public Influencer(ElvenWorkshop game, Location location) {
        this.game = game;
        this.location = location;
        for (GiftCategory giftCategory : GiftCategoryManager.getCategories().values()) {
            categoryLevels.put(giftCategory, 1);
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
        InfluencerMenuGUI gui = new InfluencerMenuGUI(this, p);
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
        itemDisplay.setItemStack(new ItemStack(Material.LECTERN, 1));
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
        String string = GameUtils.createColorString("&#ccaa77&lInfluencer");
        if (activeCategory == null) {
            string = GameUtils.createColorString(string + "\n" + "&7Inactive");
        } else {
            string = GameUtils.createColorString(string + "\n" + activeCategory.getColorCode() + activeCategory.getDisplayName());
        }
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

    public GiftCategory getActiveCategory() {
        return activeCategory;
    }
    
    public Integer getCategoryLevel(GiftCategory category) {
        return categoryLevels.getOrDefault(category, 1);
    }
    
    public Integer getNextLevelCost(GiftCategory category) {
        Integer level = getCategoryLevel(category);
        level++;
        if (level > 5) return null;
        return CVElvenWorkshop.getConfigData().getInt("influencer-upgrades." + level + ".cost");
    }
    
    public Integer getBonusValue(GiftCategory category) {
        Integer level = getCategoryLevel(category);
        return CVElvenWorkshop.getConfigData().getInt("influencer-upgrades." + level + ".value");
    }
    
    public List<Gift> getUniqueGifts() {
        List<Gift> gifts = new ArrayList<>();
        if (activeCategory == null) return gifts;
        Boolean unique = CVElvenWorkshop.getConfigData().getBoolean("influencer-upgrades." + getCategoryLevel(activeCategory) + ".unique", false);
        if (!unique) return gifts;
        gifts.addAll(activeCategory.getUniqueGifts());
        return gifts;
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
    
    public void setActiveCategory(GiftCategory category) {
        this.activeCategory = category;
    }
    
    public void upgradeCategory(GiftCategory category) {
        categoryLevels.put(category, categoryLevels.get(category) + 1);
    }
}
