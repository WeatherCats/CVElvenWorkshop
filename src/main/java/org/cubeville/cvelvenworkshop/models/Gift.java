package org.cubeville.cvelvenworkshop.models;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.cubeville.cvelvenworkshop.CVElvenWorkshop;
import org.cubeville.cvelvenworkshop.elvenworkshop.ElvenWorkshop;
import org.cubeville.cvelvenworkshop.managers.EWMaterialManager;
import org.cubeville.cvelvenworkshop.managers.GiftCategoryManager;
import org.cubeville.cvelvenworkshop.utils.EWInventoryUtils;
import org.cubeville.cvelvenworkshop.utils.EWResourceUtils;
import org.cubeville.cvelvenworkshop.utils.ItemUtils;
import org.cubeville.cvelvenworkshop.utils.PersistentDataUtils;
import org.cubeville.cvgames.utils.GameUtils;

import javax.annotation.Nullable;
import java.util.*;

public class Gift {
    ConfigurationSection config;
    Material material;
    String colorCode;
    String displayName;
    Integer value;
    Double speed;
    Integer weight;
    Map<EWMaterial, Integer> cost = new LinkedHashMap<>();
    String internalName;
    GiftCategory category;
    Boolean unique = false;


    public Gift(ConfigurationSection config, String internalName) {
        this.config = config;
        displayName = config.getString("display-name");
        colorCode = config.getString("color");
        material = Material.matchMaterial(config.getString("item"));
        value = config.getInt("value");
        speed = config.getDouble("speed");
        weight = config.getInt("weight");
        category = GiftCategoryManager.getCategory(config.getString("category"));
        if (config.contains("unique")) {
            unique = config.getBoolean("unique");
        }
        category.addGift(this, unique);
        for (String costMaterial : config.getConfigurationSection("cost").getKeys(false)) {
            EWMaterial material = EWMaterialManager.getMaterial(costMaterial);
            Integer costPrice = config.getInt("cost." + costMaterial);
            cost.put(material, costPrice);
        }
        this.internalName = internalName;
    }

    public ItemStack getItem() {
        ItemStack item = new ItemStack(material, 1);
        PersistentDataUtils.setPersistentDataString(item, "gift-type", internalName);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(GameUtils.createColorString(colorCode + displayName));
        List<String> lore = new ArrayList<>();
        lore.add(GameUtils.createColorString("&fValue: &#aaeeff" + value));
        lore.add(GameUtils.createColorString("&7Gift Item"));
        meta.setLore(lore);
        // Have to add an attribute to hide attributes in 1.21.
        NamespacedKey nsKey = new NamespacedKey(CVElvenWorkshop.getInstance(), "attack_speed");
        AttributeModifier attackSpeedModifier = new AttributeModifier(nsKey, 0.0, AttributeModifier.Operation.ADD_NUMBER);
        meta.addAttributeModifier(Attribute.GENERIC_ATTACK_SPEED, attackSpeedModifier);
        meta.addItemFlags(ItemFlag.HIDE_ARMOR_TRIM, ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ADDITIONAL_TOOLTIP, ItemFlag.HIDE_ENCHANTS);
        meta.setMaxStackSize(1);
        item.setItemMeta(meta);
        return item;
    }

    public ItemStack getCraftItem(ElvenWorkshop game, Player p) {
        ItemStack item = new ItemStack(material, 1);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(GameUtils.createColorString(colorCode + displayName));
        List<String> lore = new ArrayList<>();
        for (Map.Entry<EWMaterial, Integer> itemCost : cost.entrySet()) {
            EWMaterial mat = itemCost.getKey();
            Integer price = itemCost.getValue();
            Integer current = EWInventoryUtils.countMaterial(p, mat);
            if (current >= price) {
                lore.add(GameUtils.createColorString("&a✔ " + mat.getColorCode() + mat.getDisplayName() + ": &a" + current + "/" + price));
            } else {
                lore.add(GameUtils.createColorString("&c❌ " + mat.getColorCode() + mat.getDisplayName() + ": &c" + current + "/" + price));
            }
        }
        Integer trueValue = value;
        if (getCategory().equals((game.getInfluencer().getActiveCategory()))) {
            trueValue = (int) Math.floor(value * (1 + (0.01 * game.getInfluencer().getBonusValue(getCategory()))));
        }
        lore.add(GameUtils.createColorString("&fValue: " + EWResourceUtils.getSnowflakeDisplay(trueValue, true)));
        if (isUnique()) {
            lore.add(GameUtils.createColorString("&7&o(Unique)"));
        }
        lore.add("");
        if (!affordable(p)) {
            lore.add(GameUtils.createColorString("&f\uD83D\uDDE8&6Shift Right Click&f: I need materials!"));
        } else {
            lore.add(GameUtils.createColorString("&f\uD83D\uDDE8&6Shift Right Click&f: I am crafting gift!"));
        }
        meta.setLore(lore);
        // Have to add an attribute to hide attributes in 1.21.
        NamespacedKey nsKey = new NamespacedKey(CVElvenWorkshop.getInstance(), "attack_speed");
        AttributeModifier attackSpeedModifier = new AttributeModifier(nsKey, 0.0, AttributeModifier.Operation.ADD_NUMBER);
        meta.addAttributeModifier(Attribute.GENERIC_ATTACK_SPEED, attackSpeedModifier);
        meta.addItemFlags(ItemFlag.HIDE_ARMOR_TRIM, ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ADDITIONAL_TOOLTIP, ItemFlag.HIDE_ENCHANTS);
        item.setItemMeta(meta);
        return item;
    }

    public Boolean affordable(Player p) {
        Boolean canAfford = true;
        for (Map.Entry<EWMaterial, Integer> itemCost : cost.entrySet()) {
            EWMaterial mat = itemCost.getKey();
            Integer price = itemCost.getValue();
            Integer current = EWInventoryUtils.countMaterial(p, mat);
            if (current < price) {
                canAfford = false;
                break;
            }
        }
        return canAfford;
    }
    
    @Nullable
    public LinkedHashMap<EWMaterial, Integer> getRemainingMaterials(Player p) {
        LinkedHashMap<EWMaterial, Integer> map = new LinkedHashMap<>();
        for (Map.Entry<EWMaterial, Integer> itemCost : cost.entrySet()) {
            EWMaterial mat = itemCost.getKey();
            Integer price = itemCost.getValue();
            Integer current = EWInventoryUtils.countMaterial(p, mat);
            if (current < price) {
                map.put(mat, price - current);
            }
        }
        return map;
    }

    public void consumeMaterials(Player p) {
        for (Map.Entry<EWMaterial, Integer> itemCost : cost.entrySet()) {
            EWMaterial mat = itemCost.getKey();
            Integer price = itemCost.getValue();
            EWInventoryUtils.consumeMaterials(p, mat, price);
        }
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

    public Integer getValue() {
        return value;
    }

    public Double getSpeed() {
        return speed;
    }

    public Integer getWeight() {
        return weight;
    }

    public Map<EWMaterial, Integer> getCost() {
        return cost;
    }

    public String getInternalName() { return internalName; }
    
    public GiftCategory getCategory() {
        return category;
    }
    
    public Boolean isUnique() {
        return unique;
    }
}
