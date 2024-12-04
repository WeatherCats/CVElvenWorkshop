package org.cubeville.cvelvenworkshop.guis;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
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
import org.cubeville.cvelvenworkshop.managers.GiftCategoryManager;
import org.cubeville.cvelvenworkshop.models.EWMaterial;
import org.cubeville.cvelvenworkshop.models.GiftCategory;
import org.cubeville.cvelvenworkshop.models.Influencer;
import org.cubeville.cvelvenworkshop.models.InfluencerSlot;
import org.cubeville.cvelvenworkshop.utils.EWResourceUtils;
import org.cubeville.cvelvenworkshop.utils.ItemUtils;
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
        inv = Bukkit.createInventory(null, 45, "Influencer");
        this.influencer = influencer;
        this.player = player;
        this.game = influencer.getGame();
        initItems();
    }

    public void initItems() {
        for (int i = 0; i < 45; i++) {
            switch (i) {
                case 11:
                case 12:
                case 13:
                case 14:
                case 15: {
                    Integer categoryID = i - 11;
                    ItemStack item;
                    GiftCategory category = GiftCategoryManager.getCategories().values().stream().toList().get(categoryID);
                    item = category.getItem();
                    ItemMeta meta = item.getItemMeta();
                    List<String> lore = meta.getLore();
                    if (category.equals(influencer.getActiveCategory())) {
                        ItemUtils.setGlowing(item);
                        lore.add(0, GameUtils.createColorString("&a(Selected)"));
                    } else {
                        lore.add(GameUtils.createColorString("&f\uD83D\uDDE8&6Shift Right Click&f: Should I set?"));
                    }
                    meta.setLore(lore);
                    item.setItemMeta(meta);
                    inv.setItem(i, item);
                    break;
                }
                case 38:
                case 39:
                case 40:
                case 41:
                case 42: {
                    if (influencer.getActiveCategory() == null) {
                        inv.setItem(i, ItemUtils.getChristmasBackground(i));
                        continue;
                    }
                    Integer level = i - 37;
                    ItemStack item;
                    GiftCategory category = influencer.getActiveCategory();
                    ConfigurationSection config = CVElvenWorkshop.getConfigData().getConfigurationSection("influencer-upgrades." + level);
                    Integer weight = config.getInt("weight", -1);
                    Integer value = config.getInt("value", -1);
                    Boolean unique = config.getBoolean("unique", false);
                    if (influencer.getCategoryLevel(category) >= level) {
                        item = category.getItem();
                        item.setAmount(level);
                        ItemMeta meta = item.getItemMeta();
                        meta.setDisplayName(GameUtils.createColorString("&fLevel ") + level);
                        List<String> lore = new ArrayList<>();
                        if (weight != -1) {
                            lore.add(GameUtils.createColorString("&7Bonus Weight: &a+" + weight + "%"));
                        }
                        if (unique) {
                            lore.add(GameUtils.createColorString("&7Unlocks &aunique &7gifts!"));
                        }
                        if (value != -1) {
                            lore.add(GameUtils.createColorString("&7Bonus Value: &a+" + value + "%"));
                        }
                        meta.setLore(lore);
                        item.setItemMeta(meta);
                    } else {
                        item = new ItemStack(Material.GRAY_DYE);
                        item.setAmount(level);
                        ItemMeta meta = item.getItemMeta();
                        meta.setDisplayName(GameUtils.createColorString("&cLevel ") + level);
                        List<String> lore = new ArrayList<>();
                        Integer cost = config.getInt("cost");
                        String endLore = "";
                        if (influencer.getCategoryLevel(category) == level-1) {
                            if (game.getSnowflakes() >= cost) {
                                item.setType(Material.LIME_DYE);
                                meta.setDisplayName(GameUtils.createColorString("&eLevel ") + level);
                                lore.add(GameUtils.createColorString("&7Click to unlock!"));
                                endLore = GameUtils.createColorString("&f\uD83D\uDDE8&6Shift Right Click&f: Should I buy?");
                            } else {
                                endLore = GameUtils.createColorString("&f\uD83D\uDDE8&6Shift Right Click&f: Need snowflakes");
                            }
                        } else {
                            lore.add(GameUtils.createColorString("&7Unlock previous level first."));
                        }
                        lore.add(GameUtils.createColorString("&7Price: " + EWResourceUtils.getSnowflakeDisplay(cost, true)));
                        if (weight != -1) {
                            lore.add(GameUtils.createColorString("&7Bonus Weight: &a+" + weight + "%"));
                        }
                        if (unique) {
                            lore.add(GameUtils.createColorString("&7Unlocks &aunique &7gifts!"));
                        }
                        if (value != -1) {
                            lore.add(GameUtils.createColorString("&7Bonus Value: &a+" + value + "%"));
                        }
                        if (!endLore.isBlank()) {
                            lore.add(endLore);
                        }
                        meta.setLore(lore);
                        item.setItemMeta(meta);
                    }
                    inv.setItem(i, item);
                    break;
                }
                case 36:
                case 37:
                case 43:
                case 44: {
                    if (influencer.getActiveCategory() != null) {
                        ItemStack item = new ItemStack(influencer.getActiveCategory().getGlass());
                        ItemMeta meta = item.getItemMeta();
                        meta.setDisplayName(" ");
                        item.setItemMeta(meta);
                        inv.setItem(i, item);
                        break;
                    }
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
            case 11:
            case 12:
            case 13:
            case 14:
            case 15: {
                Integer categoryID = e.getSlot() - 11;
                GiftCategory category = GiftCategoryManager.getCategories().values().stream().toList().get(categoryID);
                if (category.equals(influencer.getActiveCategory())) {
                    p.playSound(p, Sound.ITEM_BOOK_PAGE_TURN, 2f, 0.5f);
                    influencer.setActiveCategory(null);
                    game.sendMessageToArena(GameUtils.createColorString("&e" + p.getName() + "&r &fhas reset the influencer's selected &fcategory!"));
                } else {
                    if (e.getClick().equals(ClickType.SHIFT_RIGHT)) {
                        game.sendQuickChat(p, GameUtils.createColorString("Should I set the Influencer to the " + category.getColorCode() + category.getDisplayName() + " &fcategory?"));
                        return;
                    }
                    p.playSound(p, Sound.ITEM_BOOK_PAGE_TURN, 2f, 1f);
                    influencer.setActiveCategory(category);
                    game.sendMessageToArena(GameUtils.createColorString("&e" + p.getName() + "&r &fhas set the influencer to the " + category.getColorCode() + category.getDisplayName() + " &fcategory!"));
                }
                break;
            }
            case 38:
            case 39:
            case 40:
            case 41:
            case 42: {
                Integer level = e.getSlot() - 37;
                GiftCategory category = influencer.getActiveCategory();
                ConfigurationSection config = CVElvenWorkshop.getConfigData().getConfigurationSection("influencer-upgrades." + level);
                Integer cost = config.getInt("cost");
                if (influencer.getCategoryLevel(category) == level-1) {
                    if (game.getSnowflakes() >= cost) {
                        if (e.getClick().equals(ClickType.SHIFT_RIGHT)) {
                            game.sendQuickChat(p, GameUtils.createColorString("Should I buy " + category.getColorCode() + category.getDisplayName() + " Influencer Upgrade Level " + level + "&f for " + EWResourceUtils.getSnowflakeDisplay(cost, true) + "&f?"));
                            return;
                        }
                        influencer.upgradeCategory(category);
                        game.addSnowflakes(cost * -1);
                        p.playSound(p, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.3f);
                        game.sendMessageToArena(GameUtils.createColorString("&e" + p.getName() + "&r &fhas upgraded the " + category.getColorCode() + category.getDisplayName() + " &fcategory in the Influencer for " + EWResourceUtils.getSnowflakeDisplay(cost, true) + "&f!"));
                        initItems();
                    } else {
                        if (e.getClick().equals(ClickType.SHIFT_RIGHT)) {
                            game.sendQuickChat(p, GameUtils.createColorString("I need " + EWResourceUtils.getSnowflakeDisplay(cost - game.getSnowflakes(), true) + " &fto buy " + category.getColorCode() + category.getDisplayName() + " Influencer Upgrade Level " + level));
                            return;
                        }
                        p.playSound(p, Sound.ENTITY_ENDERMAN_TELEPORT, 2f, 0.5f);
                    }
                }
                break;
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
