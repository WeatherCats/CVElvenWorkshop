package org.cubeville.cvelvenworkshop.guis;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitTask;
import org.cubeville.cvelvenworkshop.CVElvenWorkshop;
import org.cubeville.cvelvenworkshop.elvenworkshop.ElvenWorkshop;
import org.cubeville.cvelvenworkshop.managers.UpgradeManager;
import org.cubeville.cvelvenworkshop.models.Upgrade;
import org.cubeville.cvelvenworkshop.models.UpgradeStation;
import org.cubeville.cvelvenworkshop.utils.EWResourceUtils;
import org.cubeville.cvgames.utils.GameUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class UpgradeMenuGUI implements Listener {
    private final Inventory inv;
    private final ElvenWorkshop game;
    private final UpgradeStation station;

    private final Player player;

    private List<Integer> mapSlots = new ArrayList<>();
    BukkitTask task;

    public UpgradeMenuGUI(UpgradeStation station, Player player) {
        inv = Bukkit.createInventory(null, 27, "Upgrade Station");
        this.station = station;
        this.player = player;
        this.game = station.getGame();
        initItems();
    }

    public void initItems() {
        for (int i = 0; i < 27; i++) {
            switch (i) {
                case 10:
                case 11:
                case 12:
                case 14:
                case 15:
                case 16: {
                    Integer upgradeSlot;
                    if (i > 13) {
                        upgradeSlot = i-11;
                    } else upgradeSlot = i-10;
                    Upgrade upgrade = UpgradeManager.getUpgrades().values().stream().toList().get(upgradeSlot);
                    ItemStack item;
                    item = upgrade.getItem();
                    ConfigurationSection config = upgrade.getConfig();
                    String description = config.getString("description");
                    ItemMeta meta = item.getItemMeta();
                    List<String> lore = new ArrayList<>();
                    Integer level = station.getUpgradeLevels().get(upgrade);
                    lore.add(GameUtils.createColorString("&7" + description));
                    for (int l = 1; l < 4; l++) {
                        String roman = "I";
                        if (l == 2) {
                            roman = "II";
                        } else if (l == 3) {
                            roman = "III";
                        }
                        Integer cost = config.getInt("levels." + l + ".cost");
                        Integer amount = config.getInt("levels." + l + ".amount");
                        String sign = "";
                        if (amount >= 0) {
                            sign = "+";
                        }
                        if (l == level) {
                            lore.add(GameUtils.createColorString("&a&l> &a" + roman + ": &f" + sign + amount + "%"));
                        } else if (l <= level) {
                            lore.add(GameUtils.createColorString("&a" + roman + ": &f" + sign + amount + "%"));
                        } else {
                            if (game.getSnowflakes() >= cost && l == level+1) {
                                lore.add(GameUtils.createColorString("&e" + roman + ": &7" + sign + amount + "% &f(" + EWResourceUtils.getSnowflakeDisplay(cost, true) + "&f)"));
                            } else {
                                lore.add(GameUtils.createColorString("&7" + roman + ": &7" + sign + amount + "% &f(" + EWResourceUtils.getSnowflakeDisplay(cost, true) + "&f)"));
                            }
                        }
                    }
                    meta.setLore(lore);
                    if (level > 0) {
                        meta.addItemFlags(ItemFlag.HIDE_ARMOR_TRIM, ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_POTION_EFFECTS, ItemFlag.HIDE_ENCHANTS);
                        meta.addEnchant(Enchantment.LURE, 1, true);
                        item.setAmount(level);
                    }
                    item.setItemMeta(meta);
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
            case 11:
            case 12:
            case 14:
            case 15:
            case 16: {
                Integer upgradeSlot;
                if (e.getSlot() > 13) {
                    upgradeSlot = e.getSlot()-11;
                } else upgradeSlot = e.getSlot()-10;
                Upgrade upgrade = UpgradeManager.getUpgrades().values().stream().toList().get(upgradeSlot);
                Integer l = station.getUpgradeLevels().get(upgrade)+1;
                ConfigurationSection config = upgrade.getConfig();
                if (l > 3) return;
                Integer cost = config.getInt("levels." + l + ".cost");
                Integer amount = config.getInt("levels." + l + ".amount");
                if (game.getSnowflakes() >= cost) {
                    game.setUpgrade(upgrade, amount);
                    Map<Upgrade, Integer> upgradeLevels = station.getUpgradeLevels();
                    upgradeLevels.put(upgrade, l);
                    station.setUpgradeLevels(upgradeLevels);
                    game.addSnowflakes(cost * -1);
                    p.playSound(p, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.3f);
                    game.sendMessageToArena(GameUtils.createColorString("&e" + p.getName() + "&r &fhas upgraded " + upgrade.getDisplayName() + " for " + EWResourceUtils.getSnowflakeDisplay(cost, true) + "!"));
                    initItems();
                } else {
                    p.playSound(p, Sound.ENTITY_ENDERMAN_TELEPORT, 2f, 0.5f);
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
