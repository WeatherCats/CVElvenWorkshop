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
import org.cubeville.cvelvenworkshop.managers.GiftCategoryManager;
import org.cubeville.cvelvenworkshop.managers.GiftManager;
import org.cubeville.cvelvenworkshop.models.*;
import org.cubeville.cvelvenworkshop.utils.ItemUtils;
import org.cubeville.cvgames.utils.GameUtils;

import java.util.*;

public class ForgeSelectionGUI implements Listener {
    private final Inventory inv;
    private final Forge forge;
    private final ForgeSlot slot;
    private final Player player;
    private final Map<Integer, Gift> giftSlots = new LinkedHashMap<>();

    private List<Integer> mapSlots = new ArrayList<>();
    private BukkitTask task;

    public ForgeSelectionGUI(Forge forge, ForgeSlot slot, Player player) {
        this.forge = forge;
        this.slot = slot;
        this.player = player;
        inv = Bukkit.createInventory(null, 54, "Select a Gift!");
        initItems();
    }

    public void initItems() {
        {
            int i = 0;
            for (GiftCategory category : GiftCategoryManager.getCategories().values()) {
                ItemStack item = new ItemStack(category.getGlass());
                ItemMeta meta = item.getItemMeta();
                meta.setDisplayName(GameUtils.createColorString(category.getColorCode() + category.getDisplayName()));
                item.setItemMeta(meta);
                inv.setItem(i, item);
                i++;
                for (Gift gift : category.getGifts()) {
                    item = gift.getCraftItem(forge.getGame(), player);
                    inv.setItem(i, item);
                    giftSlots.put(i, gift);
                    i++;
                }
                i += 9 - (i % 9);
            }
        }
        for (int i = 0; i < 9; i++) {
            Integer slot = i+45;
            inv.setItem(slot, ItemUtils.getChristmasBackground(slot));
        }
    }

    public void openInventory(Player p) {
        p.openInventory(inv);
        task = Bukkit.getScheduler().runTaskTimer(CVElvenWorkshop.getInstance(), () -> {
            initItems();
            if (slot.getCrafting()) {
                forge.openMenu(p);
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

        if (slot.getCrafting()) {
            forge.openMenu(p);
            return;
        }

        if (giftSlots.containsKey(e.getSlot())) {
            Gift gift = giftSlots.get(e.getSlot());
            if (e.getClick() == ClickType.SHIFT_RIGHT) {
                if (!gift.affordable(player)) {
                    String message = "I need ";
                    int i = 0;
                    for (Map.Entry<EWMaterial, Integer> materialEntry : gift.getRemainingMaterials(player).entrySet()) {
                        EWMaterial mat = materialEntry.getKey();
                        Integer amount = materialEntry.getValue();
                        String matDisplay = mat.getPluralDisplayName();
                        if (amount == 1) {
                            matDisplay = mat.getDisplayName();
                        }
                        if (i > 0) {
                            message += "&f, ";
                        }
                        message += mat.getColorCode() + amount + " " + matDisplay;
                        i++;
                    }
                    message += " &fto forge " + gift.getColorCode() + gift.getDisplayName();
                    forge.getGame().sendQuickChat(player, message);
                } else {
                    forge.getGame().sendQuickChat(player, "I am now forging " + gift.getColorCode() + gift.getDisplayName());
                }
                return;
            }
            if (gift.affordable(p)) {
                if (forge.getGame().isTutorial()) {
                    ElvenWorkshopTutorial tutorial = forge.getGame().getTutorial();
                    if (tutorial.getStage() == 7 && gift.getInternalName().equals("doll")) {
                        tutorial.progressTutorial();
                    } else {
                        p.playSound(p, Sound.ENTITY_ENDERMAN_TELEPORT, 2f, 0.5f);
                        return;
                    }
                }
                gift.consumeMaterials(p);
                p.playSound(p, Sound.ENTITY_EVOKER_CAST_SPELL, 2f, 1f);
            } else {
                p.playSound(p, Sound.ENTITY_ENDERMAN_TELEPORT, 2f, 0.5f);
                return;
            }
            slot.setGift(gift);
            slot.setDuration((int) Math.round(gift.getSpeed() * 20));
            slot.setCrafting(true);
            forge.openMenu(p);
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
