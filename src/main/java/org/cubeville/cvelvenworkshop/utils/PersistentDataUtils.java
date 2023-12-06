package org.cubeville.cvelvenworkshop.utils;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.cubeville.cvelvenworkshop.CVElvenWorkshop;

import javax.annotation.Nullable;

public class PersistentDataUtils {
    public static void setPersistentDataString(ItemStack item, String key, String value) {
        NamespacedKey nsKey = new NamespacedKey(CVElvenWorkshop.getInstance(), key);
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(nsKey, PersistentDataType.STRING, value);
        item.setItemMeta(meta);
    }

    @Nullable
    public static String getPersistentDataString(ItemStack item, String key) {
        if (item == null) return null;
        if (item.getItemMeta() == null) return null;
        NamespacedKey nsKey = new NamespacedKey(CVElvenWorkshop.getInstance(), key);
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();

        if (container.has(nsKey, PersistentDataType.STRING)) {
            return container.get(nsKey, PersistentDataType.STRING);
        } else {
            return null;
        }
    }
}
