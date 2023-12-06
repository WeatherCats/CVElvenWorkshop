package org.cubeville.cvelvenworkshop.models;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.cubeville.cvelvenworkshop.utils.PersistentDataUtils;
import org.cubeville.cvgames.utils.GameUtils;

import java.util.ArrayList;
import java.util.List;

public class EWMaterial {
    ConfigurationSection config;
    Material material;
    String colorCode;
    String displayName;
    String pluralDisplayName;
    String internalName;

    public EWMaterial(ConfigurationSection config, String internalName) {
        this.config = config;
        this.internalName = internalName;
        displayName = config.getString("display-name");
        pluralDisplayName = config.getString("plural-display-name");
        colorCode = config.getString("color");
        material = Material.matchMaterial(config.getString("item"));
    }

    public ItemStack getItem() {
        ItemStack item = new ItemStack(material, 1);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(GameUtils.createColorString(colorCode + displayName));
        List<String> lore = new ArrayList<>();
        lore.add(GameUtils.createColorString("&7Material"));
        meta.setLore(lore);
        item.setItemMeta(meta);
        PersistentDataUtils.setPersistentDataString(item, "material-type", internalName);
        return item;
    }

    public ConfigurationSection getConfig() {
        return config;
    }

    public Material getMaterial() {
        return material;
    }

    public String getColorCode() {
        return colorCode;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getPluralDisplayName() {
        return pluralDisplayName;
    }

    public String getInternalName() {
        return internalName;
    }
}
