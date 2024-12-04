package org.cubeville.cvelvenworkshop.guis;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitTask;
import org.cubeville.cvelvenworkshop.CVElvenWorkshop;
import org.cubeville.cvelvenworkshop.managers.GiftManager;
import org.cubeville.cvelvenworkshop.managers.WrappingColorManager;
import org.cubeville.cvelvenworkshop.models.*;
import org.cubeville.cvelvenworkshop.models.Wrapper;
import org.cubeville.cvelvenworkshop.utils.ItemUtils;
import org.cubeville.cvelvenworkshop.utils.PersistentDataUtils;
import org.cubeville.cvgames.utils.GameUtils;

import java.util.ArrayList;
import java.util.List;

public class WrapperSelectionGUI implements Listener {
    private final Inventory inv;
    private final Wrapper wrapper;
    private final WrapperSlot slot;
    private final Player player;
    private Gift selectedGift;

    private List<Integer> mapSlots = new ArrayList<>();
    private BukkitTask task;

    public WrapperSelectionGUI(Wrapper wrapper, WrapperSlot slot, Player player) {
        this.wrapper = wrapper;
        this.slot = slot;
        this.player = player;
        inv = Bukkit.createInventory(null, 27, "Select a Gift and a Color!");
        initItems();
    }

    public void initItems() {
        for (int i = 0; i < 27; i++) {
            String wrappingColor;
            switch (i) {
                case 19:
                case 20:
                case 21:
                case 23:
                case 24:
                case 25: {
                    if (selectedGift != null) {
                        wrappingColor = switch (i) {
                            case 20 -> "yellow";
                            case 21 -> "green";
                            case 23 -> "blue";
                            case 24 -> "lilac";
                            case 25 -> "purple";
                            default -> "red";
                        };
                        WrappingColor color = WrappingColorManager.getColor(wrappingColor);
                        inv.setItem(i, color.getCraftItem());
                    } else {
                        ItemStack item = new ItemStack(Material.GRAY_DYE);
                        ItemMeta meta = item.getItemMeta();
                        meta.setDisplayName(GameUtils.createColorString("&cSelect gift first!"));
                        item.setItemMeta(meta);
                        inv.setItem(i, item);
                    }
                    break;
                }
                case 13: {
                    if (selectedGift != null) {
                        inv.setItem(i, selectedGift.getItem());
                    } else {
                        ItemStack item = new ItemStack(Material.LOOM);
                        ItemMeta meta = item.getItemMeta();
                        meta.setDisplayName(GameUtils.createColorString("&cSelect gift to wrap!"));
                        List<String> lore = new ArrayList<>();
                        lore.add(GameUtils.createColorString("&7Click a gift in your inventory to select it."));
                        meta.setLore(lore);
                        item.setItemMeta(meta);
                        inv.setItem(i, item);
                    }
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
            if (slot.getWrapping()) {
                wrapper.openMenu(p);
            }
        }, 1, 1);
    }

    @EventHandler
    public void onInventoryClick(final InventoryClickEvent e) {
        if (!e.getInventory().equals(inv)) return;

        e.setCancelled(true);

        final ItemStack clickedItem = e.getCurrentItem();

        if (clickedItem == null || clickedItem.getType().isAir()) return;

        final Player p = (Player) e.getWhoClicked();

        if (slot.getWrapping()) {
            wrapper.openMenu(p);
            return;
        }

        if (e.getClickedInventory() == inv) {
            switch (e.getSlot()) {
                case 13: {
                    if (selectedGift != null) {
                        player.getInventory().addItem(selectedGift.getItem());
                        selectedGift = null;
                        initItems();
                    }
                }
                case 19:
                case 20:
                case 21:
                case 23:
                case 24:
                case 25: {
                    if (selectedGift != null) {
                        String wrappingColor = switch (e.getSlot()) {
                            case 20 -> "yellow";
                            case 21 -> "green";
                            case 23 -> "blue";
                            case 24 -> "lilac";
                            case 25 -> "purple";
                            default -> "red";
                        };
                        if (wrapper.getGame().isTutorial()) {
                            ElvenWorkshopTutorial tutorial = wrapper.getGame().getTutorial();
                            if (tutorial.getStage() == 9 && wrappingColor.equals("purple")) {
                                tutorial.progressTutorial();
                            } else {
                                p.playSound(p, Sound.ENTITY_ENDERMAN_TELEPORT, 2f, 0.5f);
                                return;
                            }
                        }
                        p.playSound(p, Sound.UI_LOOM_SELECT_PATTERN, 0.5f, 0.8f);
                        WrappingColor color = WrappingColorManager.getColor(wrappingColor);
                        WrappedGift wrappedGift = new WrappedGift(selectedGift, color);
                        selectedGift = null;
                        slot.setWrappedGift(wrappedGift);
                        slot.setDuration((int) Math.round(5 * 20));
                        slot.setWrapping(true);
                        wrapper.openMenu(p);
                    }
                }
            }
        } else if (e.getClickedInventory() == p.getInventory()) {
            if (selectedGift != null) return;
            Integer clickSlot = e.getSlot();
            ItemStack item = e.getCurrentItem();
            if (PersistentDataUtils.getPersistentDataString(item, "gift-type") == null) return;
            Gift gift = GiftManager.getGift(PersistentDataUtils.getPersistentDataString(item, "gift-type"));
            selectedGift = gift;
            item.setAmount(item.getAmount()-1);
            p.getInventory().setItem(e.getSlot(), item);
            initItems();
        }
    }

    @EventHandler
    public void onInventoryClose(final InventoryCloseEvent e) {
        if (!e.getInventory().equals(inv)) return;
        HandlerList.unregisterAll(this);
        task.cancel();
        if (selectedGift == null) return;
        player.getInventory().addItem(selectedGift.getItem());
        selectedGift = null;
    }

    @EventHandler
    public void onInventoryDrag(final InventoryDragEvent e) {
        if (e.getInventory().equals(inv)) {
            e.setCancelled(true);
        }
    }

    public Gift getSelectedGift() {
        return selectedGift;
    }

    public void setSelectedGift(Gift selectedGift) {
        this.selectedGift = selectedGift;
    }
}
