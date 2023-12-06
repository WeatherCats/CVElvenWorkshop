package org.cubeville.cvelvenworkshop.utils;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.cubeville.cvelvenworkshop.models.EWMaterial;
import org.cubeville.cvelvenworkshop.models.WrappedGift;

import java.util.Objects;

public class EWInventoryUtils {
    public static Integer countMaterial(Player p, EWMaterial material) {
        Integer count = 0;
        for (ItemStack item : p.getInventory().getContents()) {
            if (Objects.equals(PersistentDataUtils.getPersistentDataString(item, "material-type"), material.getInternalName())) {
                count += item.getAmount();
            }
        }
        return count;
    }

    public static void consumeMaterials(Player p, EWMaterial material, Integer count) {
        ItemStack item = material.getItem();
        item.setAmount(count);
        p.getInventory().removeItem(item);
    }

    public static void clearTaggedItems(Player p, String key, String value) {
        for (ItemStack item : p.getInventory().getContents()) {
            if (Objects.equals(PersistentDataUtils.getPersistentDataString(item, key), value)) {
                p.getInventory().removeItem(item);
            }
        }
    }

    public static Boolean hasWrappedGift(Player p, WrappedGift wrappedGift) {
        for (ItemStack item : p.getInventory().getContents()) {
            if (matchesWrappedGift(item, wrappedGift)) {
                return true;
            }
        }
        return false;
    }

    public static Boolean matchesWrappedGift(ItemStack item, WrappedGift wrappedGift) {
        return Objects.equals(PersistentDataUtils.getPersistentDataString(item, "gift-type"), wrappedGift.getGiftItem().getInternalName()) && Objects.equals(PersistentDataUtils.getPersistentDataString(item, "wrapping-color"), wrappedGift.getColor().getInternalName());
    }

    public static void consumeWrappedGift(Player p, WrappedGift wrappedGift) {
        ItemStack item = wrappedGift.getItem();
        item.setAmount(1);
        p.getInventory().removeItem(item);
    }
}
