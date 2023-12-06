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
import org.cubeville.cvelvenworkshop.guis.WrapperMenuGUI;
import org.cubeville.cvelvenworkshop.guis.WrapperSelectionGUI;
import org.cubeville.cvelvenworkshop.managers.GiftManager;
import org.cubeville.cvelvenworkshop.utils.PersistentDataUtils;
import org.cubeville.cvgames.utils.GameUtils;

import java.util.ArrayList;
import java.util.List;

import static org.bukkit.Bukkit.getServer;

public class Wrapper implements Listener {
    Location location;
    ItemDisplay itemDisplay;
    TextDisplay textDisplay;
    Interaction interaction;
    List<WrapperSlot> slots = new ArrayList<>();
    ElvenWorkshop game;
    BukkitTask task;
    Double modifier = 0.0;

    public Wrapper(ElvenWorkshop game, Location location) {
        this.game = game;
        this.location = location;
        slots.add(new WrapperSlot(this));
    }

    public void activate() {
        spawnItemDisplay();
        spawnTextDisplay();
        spawnInteraction();
        task = Bukkit.getScheduler().runTaskTimer(CVElvenWorkshop.getInstance(), () -> {
            for (WrapperSlot slot : slots) {
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
        ItemStack item = e.getPlayer().getInventory().getItemInMainHand();
        if (item != null && item.getType() != Material.AIR && PersistentDataUtils.getPersistentDataString(item, "gift-type") != null && PersistentDataUtils.getPersistentDataString(item, "wrapping-color") == null) {
            Player p = e.getPlayer();
            for (WrapperSlot slot : slots) {
                if (slot.getWrapping()) continue;
                WrapperSelectionGUI selectionGUI = new WrapperSelectionGUI(this, slot, p);
                getServer().getPluginManager().registerEvents(selectionGUI, CVElvenWorkshop.getInstance());
                selectionGUI.openInventory(p);
                if (PersistentDataUtils.getPersistentDataString(item, "gift-type") == null) return;
                Gift gift = GiftManager.getGift(PersistentDataUtils.getPersistentDataString(item, "gift-type"));
                selectionGUI.setSelectedGift(gift);
                item.setAmount(item.getAmount()-1);
                p.getInventory().setItem(p.getInventory().getHeldItemSlot(), item);
                selectionGUI.initItems();
                return;
            }
            openMenu(p);
        } else openMenu(e.getPlayer());
    }

    public void openMenu(Player p) {
        WrapperMenuGUI gui = new WrapperMenuGUI(this, p);
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
        itemDisplay.setItemStack(new ItemStack(Material.LOOM, 1));
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
        String string = GameUtils.createColorString("&#ff4444&lGift Wrapper");
        for (int i = 0; i < slots.size(); i++) {
            string = string + "\n" + slots.get(i).getProgressBar();
        }
        textDisplay.setText(string);
    }

    private String getProgressBar(Integer i) {
        WrapperSlot slot = slots.get(i);
        if (slot.isWrapping) {
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
        return CVElvenWorkshop.getConfigData().getInt("wrapper-upgrades." + slots.size());
    }

    public void addSlot() {
        slots.add(new WrapperSlot(this));
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

    public List<WrapperSlot> getSlots() {
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
