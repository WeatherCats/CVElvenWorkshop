package org.cubeville.cvelvenworkshop.models;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerProfile;
import org.cubeville.cvgames.utils.GameUtils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;

public class WrappingColor {
    ConfigurationSection config;
    String colorCode;
    String displayName;
    ItemStack giftItem;
    String internalName;

    public WrappingColor(ConfigurationSection config, String internalName) throws MalformedURLException {
        this.config = config;
        colorCode = config.getString("color");
        displayName = config.getString("display-name");
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        PlayerProfile profile = Bukkit.getServer().createPlayerProfile(UUID.randomUUID());
        profile.getTextures().setSkin(new URL(config.getString("head")));
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        meta.setOwnerProfile(profile);
        head.setItemMeta(meta);
        giftItem = head;
        this.internalName = internalName;
    }

    public ConfigurationSection getConfig() {
        return config;
    }

    public String getColorCode() {
        return colorCode;
    }

    public ItemStack getGiftItem() {
        return giftItem;
    }

    public String getInternalName() { return internalName; }

    public String getDisplayName() { return displayName; }

    public ItemStack getCraftItem() {
        ItemStack item = giftItem.clone();
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(GameUtils.createColorString(colorCode + displayName + " Wrapping"));
        item.setItemMeta(meta);
        return item;
    }

}
