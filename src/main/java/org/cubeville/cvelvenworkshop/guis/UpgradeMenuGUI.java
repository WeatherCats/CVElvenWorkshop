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
import org.bukkit.event.inventory.ClickType;
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
import org.cubeville.cvelvenworkshop.utils.ItemUtils;
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
                        String amountText = upgrade.getLevelDisplay(l);
                        if (l == level) {
                            lore.add(GameUtils.createColorString("&a&l> &a" + roman + ": &f" + amountText));
                        } else if (l <= level) {
                            lore.add(GameUtils.createColorString("&a" + roman + ": &f" + amountText));
                        } else {
                            if (game.getSnowflakes() >= cost && l == level+1) {
                                lore.add(GameUtils.createColorString("&e" + roman + ": &7" + amountText + " &f(" + EWResourceUtils.getSnowflakeDisplay(cost, true) + "&f)"));
                            } else {
                                lore.add(GameUtils.createColorString("&7" + roman + ": &7" + amountText + " &f(" + EWResourceUtils.getSnowflakeDisplay(cost, true) + "&f)"));
                            }
                        }
                    }
                    if (config.getInt("levels." + String.valueOf((level + 1)) + ".cost") < game.getSnowflakes()) {
                        lore.add(GameUtils.createColorString("&f\uD83D\uDDE8&6Shift Right Click&f: Should I buy?"));
                    } else {
                        lore.add(GameUtils.createColorString("&f\uD83D\uDDE8&6Shift Right Click&f: Need snowflakes"));
                    }
                    meta.setLore(lore);
                    if (level > 0) {
                        meta.addItemFlags(ItemFlag.HIDE_ARMOR_TRIM, ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ADDITIONAL_TOOLTIP, ItemFlag.HIDE_ENCHANTS);
                        meta.setMaxStackSize(level);
                        ItemUtils.setGlowing(item);
                        item.setAmount(level);
                    }
                    item.setItemMeta(meta);
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
                    if (e.getClick().equals(ClickType.SHIFT_RIGHT)) {
                        game.sendQuickChat(p, GameUtils.createColorString("Should I buy " + upgrade.getDisplayName() + " Level " + l + "&f for " + EWResourceUtils.getSnowflakeDisplay(cost, true) + "&f?"));
                        return;
                    }
                    game.setUpgrade(upgrade, l, config);
                    Map<Upgrade, Integer> upgradeLevels = station.getUpgradeLevels();
                    upgradeLevels.put(upgrade, l);
                    station.setUpgradeLevels(upgradeLevels);
                    game.addSnowflakes(cost * -1);
                    p.playSound(p, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.3f);
                    game.sendMessageToArena(GameUtils.createColorString("&e" + p.getName() + "&r &fhas upgraded " + upgrade.getDisplayName() + " for " + EWResourceUtils.getSnowflakeDisplay(cost, true) + "&f!"));
                    initItems();
                } else {
                    if (e.getClick().equals(ClickType.SHIFT_RIGHT)) {
                        game.sendQuickChat(p, GameUtils.createColorString("I need " + EWResourceUtils.getSnowflakeDisplay(cost - game.getSnowflakes(), true) + " &fto buy " + upgrade.getDisplayName() + " Level " + l));
                        return;
                    }
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
