package org.cubeville.cvelvenworkshop.models;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.cubeville.cvelvenworkshop.utils.PersistentDataUtils;
import org.cubeville.cvgames.utils.GameUtils;

import java.util.ArrayList;
import java.util.List;

public class Upgrade {
    ConfigurationSection config;
    Material material;
    String displayName;
    String description;
    String internalName;

    public Upgrade(ConfigurationSection config, String internalName) {
        this.config = config;
        this.internalName = internalName;
        displayName = config.getString("display-name");
        description = config.getString("description");
        material = Material.matchMaterial(config.getString("item"));
    }

    public ItemStack getItem() {
        ItemStack item = new ItemStack(material, 1);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(GameUtils.createColorString("&f" + displayName));
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
        return item;
    }

    public ConfigurationSection getConfig() {
        return config;
    }

    public Material getMaterial() {
        return material;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public String getInternalName() {
        return internalName;
    }
}
