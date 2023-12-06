package org.cubeville.cvelvenworkshop.guis;

import org.bukkit.Bukkit;
import org.bukkit.Material;
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
import org.cubeville.cvelvenworkshop.elvenworkshop.ElvenWorkshop;
import org.cubeville.cvelvenworkshop.managers.GiftManager;
import org.cubeville.cvelvenworkshop.models.Influencer;
import org.cubeville.cvelvenworkshop.models.InfluencerSlot;
import org.cubeville.cvelvenworkshop.models.Gift;
import org.cubeville.cvgames.utils.GameUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class InfluencerSelectionGUI implements Listener {
    private final Inventory inv;
    private final Influencer influencer;
    private final InfluencerSlot slot;
    private final Player player;
    private final ElvenWorkshop game;

    private List<Integer> mapSlots = new ArrayList<>();
    private BukkitTask task;

    public InfluencerSelectionGUI(Influencer influencer, InfluencerSlot slot, Player player) {
        game = influencer.getGame();
        this.influencer = influencer;
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
            ItemMeta meta = item.getItemMeta();
            List<String> lore = new ArrayList<>();
            String percent = Math.round(((double) game.getWeight(gift) / game.getTotalWeight()) * 1000.0)/10.0 + "%";
            if (gift.getWeight() != game.getWeight(gift)) {
                lore.add(GameUtils.createColorString("&7Weight: &m" + gift.getWeight() + "&r &a" + game.getWeight(gift) + " &f(&b" + percent + "&f)"));
            } else {
                lore.add(GameUtils.createColorString("&7Weight: &a" + game.getWeight(gift) + " &f(&b" + percent + "&f)"));
            }
            meta.setLore(lore);
            item.setItemMeta(meta);
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
        }, 1, 1);
    }

    @EventHandler
    public void onInventoryClick(final InventoryClickEvent e) {
        if (!e.getInventory().equals(inv)) return;

        e.setCancelled(true);

        final ItemStack clickedItem = e.getCurrentItem();

        if (clickedItem == null || clickedItem.getType().isAir()) return;

        final Player p = (Player) e.getWhoClicked();

        if (e.getSlot() < GiftManager.getGifts().size()) {
            Gift gift = GiftManager.getGifts().values().stream().toList().get(e.getSlot());
            slot.selectGift(gift);
            influencer.openMenu(p);
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
