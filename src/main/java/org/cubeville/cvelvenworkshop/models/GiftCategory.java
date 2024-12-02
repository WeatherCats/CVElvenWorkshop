package org.cubeville.cvelvenworkshop.models;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.cubeville.cvelvenworkshop.managers.EWMaterialManager;
import org.cubeville.cvelvenworkshop.utils.PersistentDataUtils;
import org.cubeville.cvgames.utils.GameUtils;

import java.util.ArrayList;
import java.util.List;

public class GiftCategory {
    ConfigurationSection config;
    Material material;
    Material glass;
    String colorCode;
    String displayName;
    EWMaterial specialty;
    Integer value;
    String internalName;
    List<Gift> standardGifts = new ArrayList<>();
    List<Gift> uniqueGifts = new ArrayList<>();
    
    public GiftCategory(ConfigurationSection config, String internalName) {
        this.config = config;
        this.internalName = internalName;
        displayName = config.getString("display-name");
        colorCode = config.getString("color");
        material = Material.matchMaterial(config.getString("item"));
        glass = Material.matchMaterial(config.getString("glass"));
        specialty = EWMaterialManager.getMaterial(config.getString("specialty"));
        value = config.getInt("value");
    }
    
    public ItemStack getItem() {
        ItemStack item = new ItemStack(material, 1);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(GameUtils.createColorString(colorCode + displayName));
        meta.setLore(List.of(GameUtils.createColorString("&7Specialty: " + specialty.getColorCode() + specialty.getPluralDisplayName()),
            GameUtils.createColorString("&7Value: &a" + "$".repeat(value))));
        meta.addItemFlags(ItemFlag.HIDE_ARMOR_TRIM, ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ADDITIONAL_TOOLTIP, ItemFlag.HIDE_ENCHANTS);
        item.setItemMeta(meta);
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
    
    public Material getGlass() {
        return glass;
    }
    
    public String getInternalName() {
        return internalName;
    }
    
    public void addGift(Gift gift, Boolean unique) {
        if (!unique) {
            standardGifts.add(gift);
        } else {
            uniqueGifts.add(gift);
        }
    }
    
    public List<Gift> getGifts() {
        List<Gift> gifts = new ArrayList<>(standardGifts);
        gifts.addAll(uniqueGifts);
        return gifts;
    }
    
    public List<Gift> getStandardGifts() {
        return standardGifts;
    }
    
    public List<Gift> getUniqueGifts() {
        return uniqueGifts;
    }
}
