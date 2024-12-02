package org.cubeville.cvelvenworkshop.utils;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class ItemUtils {
    public static ItemStack setGlowing(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        meta.setEnchantmentGlintOverride(true);
        item.setItemMeta(meta);
        return item;
    }
    
    public static ItemStack createBackgroundItem(Material material) {
        ItemStack item = createGUIItem(material, " ", new ArrayList<>());
        ItemMeta meta = item.getItemMeta();
        meta.setHideTooltip(true);
        item.setItemMeta(meta);
        return item;
    }
    
    public static ItemStack createGUIItem(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }
    
    public static ItemStack getChristmasBackground(Integer i) {
        Integer floorMod = Math.floorMod(i, 3);
        Material material;
        if (floorMod == 0) {
            material = Material.RED_STAINED_GLASS_PANE;
        } else if (floorMod == 1) {
            material = Material.GREEN_STAINED_GLASS_PANE;
        } else {
            material = Material.WHITE_STAINED_GLASS_PANE;
        }
        ItemStack item = createBackgroundItem(material);
        return item;
    }
    
    public static ItemStack addLore(ItemStack item, String newLore) {
        ItemMeta meta = item.getItemMeta();
        List<String> lore = new ArrayList<>();
        if (meta.hasLore()) {
            lore = meta.getLore();
        }
        lore.add(newLore);
        item.setItemMeta(meta);
        return item;
    }
}
