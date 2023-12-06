package org.cubeville.cvelvenworkshop.managers;

import org.bukkit.Bukkit;
import org.cubeville.cvelvenworkshop.models.Gift;

import javax.annotation.Nullable;
import java.util.*;

public class GiftManager {
    static Map<String, Gift> giftMap = new LinkedHashMap<>();

    public static void registerGift(String internalName, Gift gift) {
        giftMap.put(internalName, gift);
    }

    public static void organizeGifts() {
        List<String> gifts = new ArrayList<>(giftMap.keySet());
        gifts.sort(Comparator.comparingInt(o -> giftMap.get(o).getValue()));
        Map<String, Gift> tempMap = new HashMap<>();
        for (String giftString : gifts) {
            tempMap.put(giftString, giftMap.get(giftString));
        }
        giftMap = tempMap;
    }

    public static Map<String, Gift> getGifts() {
        return giftMap;
    }

    public static Gift getGift(String materialName) {
        return giftMap.get(materialName);
    }

    @Nullable
    public static Gift getRandomGift() {
        Integer weight = 0;
        Collection<Gift> giftList = giftMap.values();
        for (Gift gift : giftList) {
            weight += gift.getWeight();
        }
        Random rand = new Random();
        Integer value = rand.nextInt(weight);
        value += 1;
        for (Gift gift : giftList) {
            if (value <= gift.getWeight()) {
                return gift;
            }
            else {
                value -= gift.getWeight();
            }
        }
        return null;
    }
}
