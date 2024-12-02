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
import org.cubeville.cvelvenworkshop.elvenworkshop.ElvenWorkshop;
import org.cubeville.cvelvenworkshop.elvenworkshop.ElvenWorkshopMain;
import org.cubeville.cvelvenworkshop.enums.ElvenWorkshopGameType;
import org.cubeville.cvgames.enums.ArenaStatus;
import org.cubeville.cvgames.utils.GameUtils;

import java.util.*;

public class GameSelectorGUI implements Listener {
    private final Inventory inv;
    private final ElvenWorkshopMain mainGame;
    private long lastRefresh = 0;
    
    private Map<Integer, ElvenWorkshop> mapSlots = new HashMap<>();

    public GameSelectorGUI(ElvenWorkshopMain mainGame) {
        inv = Bukkit.createInventory(null, 54, "Elven Workshop Games");
        this.mainGame = mainGame;
        initItems();
    }

    public void initItems() {
        HashMap<Integer, ElvenWorkshop> gameList = mainGame.getGames();
        Integer itemSlot = 0;
        for (Map.Entry<Integer, ElvenWorkshop> gameEntry : gameList.entrySet()) {
            Integer slot = gameEntry.getKey();
            ElvenWorkshop game = gameEntry.getValue();
            Set<Player> players = game.getArena().getQueue().getPlayerSet();
            Material material = Material.AIR;
            String statusDescription = "";
            switch (game.getArena().getStatus()) {
                case OPEN:
                    material = Material.LIME_DYE;
                    statusDescription = "§a§lOPEN";
                    break;
                case IN_QUEUE:
                    material = Material.YELLOW_DYE;
                    statusDescription = "§e§lIN QUEUE";
                    break;
                case IN_USE:
                    material = Material.GRAY_DYE;
                    statusDescription = "§7§lIN USE";
                    break;
                case HOSTING:
                    material = Material.LIGHT_BLUE_DYE;
                    statusDescription = "§b§lHOSTING";
                    break;
                case CLOSED:
                    material = Material.RED_DYE;
                    statusDescription = "§c§lCLOSED";
                    break;
            }
            ItemStack item = new ItemStack(material, 1);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(GameUtils.createColorString("&fGame ") + (slot+1));
            mapSlots.put(itemSlot, game);
            List<String> lore = new ArrayList<>();
            lore.add(statusDescription);
            lore.add("");
            for (Player player : players) {
                lore.add(GameUtils.createColorString("&7") + player.getName());
            }
            meta.setLore(lore);
            item.setItemMeta(meta);
            inv.setItem(itemSlot, item);
            itemSlot++;
        }
        for (int i = 0; i < 9; i++) {
            Integer slot = i+45;
            if (i == 4) {
                ItemStack item = new ItemStack(Material.CLOCK);
                ItemMeta meta = item.getItemMeta();
                meta.setDisplayName(GameUtils.createColorString("&l⟳ &eRefresh"));
                item.setItemMeta(meta);
                inv.setItem(slot, item);
            } else {
                ItemStack item = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
                ItemMeta meta = item.getItemMeta();
                meta.setDisplayName(" ");
                item.setItemMeta(meta);
                inv.setItem(slot, item);
            }
        }
        ItemStack item = new ItemStack(Material.LIME_DYE, 1);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(GameUtils.createColorString("&f+ New Game"));
        meta.setLore(List.of(
            GameUtils.createColorString("&6Left Click &7to create a new game"),
            GameUtils.createColorString("&6Shift Right Click &7to play tutorial")
        ));
        item.setItemMeta(meta);
        inv.setItem(itemSlot, item);
    }

    public void openInventory(Player p) {
        p.openInventory(inv);
    }
    
    @EventHandler
    public void onInventoryClick(final InventoryClickEvent e) {
        if (!e.getInventory().equals(inv)) return;
        
        e.setCancelled(true);
        
        final ItemStack clickedItem = e.getCurrentItem();
        
        if (clickedItem == null || clickedItem.getType().isAir()) return;
        
        final Player p = (Player) e.getWhoClicked();
        
        if (e.getSlot() == mapSlots.keySet().size()) {
            switch (e.getClick()) {
                case LEFT -> {
                    mainGame.createGame(p, ElvenWorkshopGameType.NORMAL);
                    p.closeInventory();
                }
                case SHIFT_RIGHT -> {
                    mainGame.createGame(p, ElvenWorkshopGameType.TUTORIAL);
                    p.closeInventory();
                }
            }
        } else if (e.getSlot() < mapSlots.keySet().size()) {
            ElvenWorkshop gameSlot = mapSlots.get(e.getSlot());
            if (gameSlot.getArena().getStatus() == ArenaStatus.OPEN) {
                p.sendMessage(GameUtils.createColorString("&cPlease wait to join that game!"));
                return;
            }
            mainGame.addPlayer(p, gameSlot);
            p.closeInventory();
        } else if (e.getSlot() == 49) {
            if (System.currentTimeMillis() < lastRefresh + 1000) {
                p.sendMessage(GameUtils.createColorString("&cPlease wait to refresh!"));
                return;
            }
            initItems();
            lastRefresh = System.currentTimeMillis();
            p.playSound(p, Sound.ENTITY_ENDERMAN_TELEPORT, 2f, 1.2f);
        }
    }

    @EventHandler
    public void onInventoryClose(final InventoryCloseEvent e) {
        if (!e.getInventory().equals(inv)) return;
        HandlerList.unregisterAll(this);
    }

    @EventHandler
    public void onInventoryDrag(final InventoryDragEvent e) {
        if (e.getInventory().equals(inv)) {
            e.setCancelled(true);
        }
    }
}
