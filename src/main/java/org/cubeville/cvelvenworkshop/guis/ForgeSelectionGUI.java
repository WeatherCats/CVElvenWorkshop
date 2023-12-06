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
import org.cubeville.cvelvenworkshop.models.Forge;
import org.cubeville.cvelvenworkshop.models.ForgeSlot;
import org.cubeville.cvelvenworkshop.models.Gift;
import org.cubeville.cvgames.utils.GameUtils;

import java.util.*;

public class ForgeSelectionGUI implements Listener {
    private final Inventory inv;
    private final Forge forge;
    private final ForgeSlot slot;
    private final Player player;

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
        Map<String, Gift> giftList = GiftManager.getGifts();
        for (int i = 0; i < giftList.size(); i++) {
            Map.Entry<String, Gift> giftEntry = giftList.entrySet().stream().toList().get(i);
            Gift gift = giftEntry.getValue();
            ItemStack item = gift.getCraftItem(player);
            inv.setItem(i, item);
        }
        for (int i = 0; i < 9; i++) {
            Integer slot = i+45;
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
            inv.setItem(slot, item);
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

        if (e.getSlot() < GiftManager.getGifts().size()) {
            Gift gift = GiftManager.getGifts().values().stream().toList().get(e.getSlot());
            if (gift.affordable(p)) {
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
