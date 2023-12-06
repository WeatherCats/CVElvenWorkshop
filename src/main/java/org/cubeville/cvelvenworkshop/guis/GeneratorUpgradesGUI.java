package org.cubeville.cvelvenworkshop.guis;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
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
import org.cubeville.cvelvenworkshop.models.EWMaterial;
import org.cubeville.cvelvenworkshop.models.EWMaterialGenerator;
import org.cubeville.cvelvenworkshop.utils.EWResourceUtils;
import org.cubeville.cvgames.utils.GameUtils;

import java.util.ArrayList;
import java.util.List;

public class GeneratorUpgradesGUI implements Listener {
    private final Inventory inv;
    private final ElvenWorkshop game;
    private final EWMaterialGenerator generator;

    private final Player player;

    private List<Integer> mapSlots = new ArrayList<>();
    BukkitTask task;

    public GeneratorUpgradesGUI(EWMaterialGenerator generator, Player player) {
        inv = Bukkit.createInventory(null, 27, generator.getMaterial().getDisplayName() + " Generator");
        this.generator = generator;
        this.player = player;
        this.game = generator.getGame();
        initItems();
    }

    public void initItems() {
        for (int i = 0; i < 27; i++) {
            switch (i) {
                case 11:
                case 12:
                case 13:
                case 14:
                case 15: {
                    Integer level = i - 10;
                    ItemStack item;
                    EWMaterial material = generator.getMaterial();
                    ConfigurationSection config = generator.getMaterial().getConfig().getConfigurationSection("levels." + level);
                    Integer speed = config.getInt("speed");
                    Integer amount = config.getInt("amount");
                    if (generator.getLevel() >= level) {
                        item = material.getItem();
                        item.setAmount(level);
                        ItemMeta meta = item.getItemMeta();
                        meta.setDisplayName(GameUtils.createColorString("&fLevel ") + level);
                        List<String> lore = new ArrayList<>();
                        lore.add(GameUtils.createColorString("&7Generates " + material.getColorCode() + amount + " " + material.getDisplayName() + "/" + speed + "s"));
                        meta.setLore(lore);
                        item.setItemMeta(meta);
                    } else {
                        item = new ItemStack(Material.GRAY_DYE);
                        item.setAmount(level);
                        ItemMeta meta = item.getItemMeta();
                        meta.setDisplayName(GameUtils.createColorString("&cLevel ") + level);
                        List<String> lore = new ArrayList<>();
                        Integer cost = config.getInt("cost");
                        if (generator.getLevel() == level-1) {
                            if (game.getSnowflakes() >= cost) {
                                item.setType(Material.LIME_DYE);
                                meta.setDisplayName(GameUtils.createColorString("&eLevel ") + level);
                                lore.add(GameUtils.createColorString("&7Click to unlock!"));
                            }
                        } else {
                            lore.add(GameUtils.createColorString("&7Unlock previous level first."));
                        }
                        lore.add(GameUtils.createColorString("&7Price: " + EWResourceUtils.getSnowflakeDisplay(cost, true)));
                        lore.add(GameUtils.createColorString("&7Generates " + material.getColorCode() + amount + " " + material.getDisplayName() + "/" + speed + "s"));
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
            case 11:
            case 12:
            case 13:
            case 14:
            case 15: {
                Integer level = e.getSlot() - 10;
                ConfigurationSection config = generator.getMaterial().getConfig().getConfigurationSection("levels." + level);
                Integer cost = config.getInt("cost");
                if (generator.getLevel() == level-1) {
                    if (game.getSnowflakes() >= cost) {
                        generator.upgrade();
                        game.addSnowflakes(cost * -1);
                        p.playSound(p, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.3f);
                        game.sendMessageToArena(GameUtils.createColorString("&e" + p.getName() + "&r &fhas upgraded the " + generator.getMaterial().getDisplayName() + " Generator for " + EWResourceUtils.getSnowflakeDisplay(cost, true) + "!"));
                        initItems();
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
