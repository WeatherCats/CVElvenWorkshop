package org.cubeville.cvelvenworkshop.guis;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitTask;
import org.cubeville.cvelvenworkshop.CVElvenWorkshop;
import org.cubeville.cvelvenworkshop.elvenworkshop.ElvenWorkshop;
import org.cubeville.cvelvenworkshop.models.Influencer;
import org.cubeville.cvelvenworkshop.models.InfluencerSlot;
import org.cubeville.cvelvenworkshop.utils.EWResourceUtils;
import org.cubeville.cvgames.utils.GameUtils;

import java.util.ArrayList;
import java.util.List;

import static org.bukkit.Bukkit.getServer;

public class InfluencerMenuGUI implements Listener {
    private final Inventory inv;
    private final ElvenWorkshop game;
    private final Influencer influencer;

    private final Player player;

    private List<Integer> mapSlots = new ArrayList<>();
    BukkitTask task;

    public InfluencerMenuGUI(Influencer influencer, Player player) {
        inv = Bukkit.createInventory(null, 27, "Influencer");
        this.influencer = influencer;
        this.player = player;
        this.game = influencer.getGame();
        initItems();
    }

    public void initItems() {
        for (int i = 0; i < 27; i++) {
            switch (i) {
                case 10:
                case 12:
                case 14:
                case 16: {
                    Integer slotNumber = (i / 2) - 5;
                    ItemStack item;
                    if (influencer.getSlots().size() > slotNumber) {
                        InfluencerSlot slot = influencer.getSlots().get(slotNumber);
                        if (slot.getIsActive()) {
                            item = slot.getGift().getItem();
                            ItemMeta meta = item.getItemMeta();
                            List<String> lore = new ArrayList<>();
                            lore.add(GameUtils.createColorString("&fAdded Weight: &a" + slot.getModifiedWeight()));
                            lore.add(GameUtils.createColorString("&7(Right click to deactivate)"));
                            meta.setLore(lore);
                            item.setItemMeta(meta);
                        } else {
                            item = new ItemStack(Material.LECTERN);
                            ItemMeta meta = item.getItemMeta();
                            meta.setDisplayName(GameUtils.createColorString("&7Inactive"));
                            item.setItemMeta(meta);
                        }
                    } else {
                        item = new ItemStack(Material.GRAY_DYE);
                        ItemMeta meta = item.getItemMeta();
                        meta.setDisplayName(GameUtils.createColorString("&cLocked!"));
                        List<String> lore = new ArrayList<>();
                        if (influencer.getSlots().size() == slotNumber) {
                            lore.add(GameUtils.createColorString("&7Click to unlock!"));
                            lore.add(GameUtils.createColorString("&7Price: " + EWResourceUtils.getSnowflakeDisplay(influencer.getNextSlotCost(), true)));
                            if (game.getSnowflakes() >= influencer.getNextSlotCost()) {
                                item.setType(Material.LIME_DYE);
                                meta.setDisplayName(GameUtils.createColorString("&eLocked!"));
                            }
                        } else {
                            lore.add(GameUtils.createColorString("&7Unlock previous slot first."));
                        }
                        meta.setLore(lore);
                        item.setItemMeta(meta);
                    }
                    inv.setItem(i, item);
                    break;
                }
                default: {
                    Integer floorMod = Math.floorMod(i, 3);
                    Material material;
                    if (floorMod == 0) {
                        material = Material.RED_STAINED_GLASS_PANE;
                    } else if (floorMod == 1) {
                        material = Material.GREEN_STAINED_GLASS_PANE;
                    } else {
                        material = Material.WHITE_STAINED_GLASS_PANE;
                    }
                    ItemStack item = new ItemStack(material);
                    ItemMeta meta = item.getItemMeta();
                    meta.setDisplayName(" ");
                    item.setItemMeta(meta);
                    inv.setItem(i, item);
                }
            }
        }
    }

    public void openInventory(Player p) {
        p.openInventory(inv);
        task = Bukkit.getScheduler().runTaskTimer(CVElvenWorkshop.getInstance(), () -> {
            initItems();
        }, 1, 1);
    }

    @EventHandler
    public void onInventoryClick(final InventoryClickEvent e) {
        if (!e.getInventory().equals(inv)) return;

        e.setCancelled(true);

        final ItemStack clickedItem = e.getCurrentItem();

        if (clickedItem == null || clickedItem.getType().isAir()) return;

        final Player p = (Player) e.getWhoClicked();

        switch (e.getSlot()) {
            case 10:
            case 12:
            case 14:
            case 16: {
                Integer slotNumber = (e.getSlot() / 2) - 5;
                if (influencer.getSlots().size() > slotNumber) {
                    InfluencerSlot slot = influencer.getSlots().get(slotNumber);
                    if (slot.getIsActive()) {
                        if (e.getClick() == ClickType.RIGHT) {
                            slot.reset(p);
                        } else {
                            InfluencerSelectionGUI selectionGUI = new InfluencerSelectionGUI(influencer, slot, p);
                            getServer().getPluginManager().registerEvents(selectionGUI, CVElvenWorkshop.getInstance());
                            selectionGUI.openInventory(p);
                        }
                    } else {
                        InfluencerSelectionGUI selectionGUI = new InfluencerSelectionGUI(influencer, slot, p);
                        getServer().getPluginManager().registerEvents(selectionGUI, CVElvenWorkshop.getInstance());
                        selectionGUI.openInventory(p);
                    }
                } else if (influencer.getSlots().size() == slotNumber) {
                    Integer cost = influencer.getNextSlotCost();
                    if (game.getSnowflakes() >= cost) {
                        influencer.addSlot();
                        game.addSnowflakes(cost * -1);
                        p.playSound(p, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.3f);
                        game.sendMessageToArena(GameUtils.createColorString("&e" + p.getName() + "&r &fhas bought an additional Influencer Slot for " + EWResourceUtils.getSnowflakeDisplay(cost, true) + "!"));
                    } else {
                        p.playSound(p, Sound.ENTITY_ENDERMAN_TELEPORT, 2f, 0.5f);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onInventoryClose(final InventoryCloseEvent e) {
        if (!e.getInventory().equals(inv)) return;
        HandlerList.unregisterAll(this);
        task.cancel();
    }

    @EventHandler
    public void onInventoryDrag(final InventoryDragEvent e) {
        if (e.getInventory().equals(inv)) {
            e.setCancelled(true);
        }
    }
}
