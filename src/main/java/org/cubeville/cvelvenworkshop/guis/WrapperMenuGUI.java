package org.cubeville.cvelvenworkshop.guis;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
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
import org.cubeville.cvelvenworkshop.models.*;
import org.cubeville.cvelvenworkshop.utils.EWResourceUtils;
import org.cubeville.cvelvenworkshop.utils.ItemUtils;
import org.cubeville.cvgames.utils.GameUtils;

import java.util.ArrayList;
import java.util.List;

import static org.bukkit.Bukkit.getServer;

public class WrapperMenuGUI implements Listener {
    private final Inventory inv;
    private final ElvenWorkshop game;
    private final Wrapper wrapper;

    private final Player player;

    private List<Integer> mapSlots = new ArrayList<>();
    BukkitTask task;

    public WrapperMenuGUI(Wrapper wrapper, Player player) {
        inv = Bukkit.createInventory(null, 27, "Gift Wrapper");
        this.wrapper = wrapper;
        this.player = player;
        this.game = wrapper.getGame();
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
                    if (wrapper.getSlots().size() > slotNumber) {
                        WrapperSlot slot = wrapper.getSlots().get(slotNumber);
                        if (slot.getWrapping()) {
                            item = slot.getGift().getItem();
                            ItemMeta meta = item.getItemMeta();
                            List<String> lore = new ArrayList<>();
                            lore.add(slot.getProgressBar());
                            if (slot.isComplete()) {
                                lore.add(GameUtils.createColorString("&f\uD83D\uDDE8&6Shift Right Click&f: Taking item"));
                            } else {
                                lore.add(GameUtils.createColorString("&f\uD83D\uDDE8&6Shift Right Click&f: Being wrapped"));
                            }
                            meta.setLore(lore);
                            if (slot.isComplete()) {
                                ItemUtils.setGlowing(item);
                            }
                            item.setItemMeta(meta);
                        } else {
                            item = new ItemStack(Material.LOOM);
                            ItemMeta meta = item.getItemMeta();
                            meta.setDisplayName(GameUtils.createColorString("&7Inactive"));
                            item.setItemMeta(meta);
                        }
                    } else {
                        item = new ItemStack(Material.GRAY_DYE);
                        ItemMeta meta = item.getItemMeta();
                        meta.setDisplayName(GameUtils.createColorString("&cLocked!"));
                        List<String> lore = new ArrayList<>();
                        if (wrapper.getSlots().size() == slotNumber) {
                            lore.add(GameUtils.createColorString("&7Click to unlock!"));
                            lore.add(GameUtils.createColorString("&7Price: " + EWResourceUtils.getSnowflakeDisplay(wrapper.getNextSlotCost(), true)));
                            if (game.getSnowflakes() >= wrapper.getNextSlotCost()) {
                                item.setType(Material.LIME_DYE);
                                meta.setDisplayName(GameUtils.createColorString("&eLocked!"));
                                lore.add(GameUtils.createColorString("&f\uD83D\uDDE8&6Shift Right Click&f: Should I buy?"));
                            } else {
                                lore.add(GameUtils.createColorString("&f\uD83D\uDDE8&6Shift Right Click&f: Need snowflakes"));
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
                    inv.setItem(i, ItemUtils.getChristmasBackground(i));
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
                if (wrapper.getSlots().size() > slotNumber) {
                    WrapperSlot slot = wrapper.getSlots().get(slotNumber);
                    if (slot.getWrapping()) {
                        if (e.getClick() == ClickType.SHIFT_RIGHT) {
                            WrappedGift gift = slot.getGift();
                            if (slot.isComplete()) {
                                game.sendQuickChat(player, "&fI am taking " + gift.getOrderName() + " &ffrom the wrapper");
                            } else {
                                game.sendQuickChat(player, gift.getOrderName() + " &fis being wrapped");
                            }
                            return;
                        }
                        if (slot.isComplete()) {
                            if (wrapper.getGame().isTutorial()) {
                                ElvenWorkshopTutorial tutorial = wrapper.getGame().getTutorial();
                                if (tutorial.getStage() == 10 && slot.getGift().getColor().getInternalName().equals("purple")) {
                                    tutorial.progressTutorial();
                                }
                            }
                            slot.claimWrappedGift(p);
                            p.playSound(p, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.3f);
                        }
                    } else {
                        WrapperSelectionGUI selectionGUI = new WrapperSelectionGUI(wrapper, slot, p);
                        getServer().getPluginManager().registerEvents(selectionGUI, CVElvenWorkshop.getInstance());
                        selectionGUI.openInventory(p);
                    }
                } else if (wrapper.getSlots().size() == slotNumber) {
                    Integer cost = wrapper.getNextSlotCost();
                    if (game.getSnowflakes() >= cost) {
                        if (e.getClick().equals(ClickType.SHIFT_RIGHT)) {
                            game.sendQuickChat(p, GameUtils.createColorString("Should I buy an additional Wrapper Slot &ffor " + EWResourceUtils.getSnowflakeDisplay(cost, true) + "&f?"));
                            return;
                        }
                        wrapper.addSlot();
                        game.addSnowflakes(cost * -1);
                        p.playSound(p, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.3f);
                        game.sendMessageToArena(GameUtils.createColorString("&e" + p.getName() + "&r &fhas bought an additional Gift Wrapper Slot for " + EWResourceUtils.getSnowflakeDisplay(cost, true) + "&f!"));
                    } else {
                        if (e.getClick().equals(ClickType.SHIFT_RIGHT)) {
                            game.sendQuickChat(p, GameUtils.createColorString("I need " + EWResourceUtils.getSnowflakeDisplay(cost - game.getSnowflakes(), true) + " &fto buy an additional Wrapper Slot"));
                            return;
                        }
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
