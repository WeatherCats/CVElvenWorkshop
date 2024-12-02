package org.cubeville.cvelvenworkshop.models;

import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.cubeville.cvelvenworkshop.managers.GiftManager;
import org.cubeville.cvelvenworkshop.managers.WrappingColorManager;
import org.cubeville.cvelvenworkshop.utils.PersistentDataUtils;
import org.cubeville.cvgames.utils.GameUtils;

import java.util.ArrayList;
import java.util.List;

public class WrappedGift {
    Gift giftItem;
    WrappingColor color;

    public WrappedGift(Gift gift, WrappingColor color) {
        this.giftItem = gift;
        this.color = color;
    }

    public static WrappedGift fromNames(String giftString, String colorString) {
        return new WrappedGift(GiftManager.getGift(giftString), WrappingColorManager.getColor(colorString));
    }

    public ItemStack getItem() {
        ItemStack item = color.getGiftItem().clone();
        PersistentDataUtils.setPersistentDataString(item, "gift-type", giftItem.getInternalName());
        PersistentDataUtils.setPersistentDataString(item, "wrapping-color", color.getInternalName());
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(GameUtils.createColorString(getOrderName()));
        List<String> lore = new ArrayList<>();
        lore.add(GameUtils.createColorString(GameUtils.createColorString("&fValue: &#aaeeff" + giftItem.getValue())));
        lore.add(GameUtils.createColorString(GameUtils.createColorString("&7Wrapped Gift")));
        meta.setLore(lore);
        meta.addItemFlags(ItemFlag.HIDE_ARMOR_TRIM, ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ADDITIONAL_TOOLTIP, ItemFlag.HIDE_ENCHANTS);
        item.setItemMeta(meta);
        return item;
    }

    public String getOrderName() {
        return GameUtils.createColorString("&f[" + color.getColorCode() + "\uD83C\uDF81 " + color.getDisplayName() + "&f] " + giftItem.getColorCode() + giftItem.getDisplayName());
    }

    public Gift getGiftItem() {
        return giftItem;
    }

    public WrappingColor getColor() {
        return color;
    }
}
