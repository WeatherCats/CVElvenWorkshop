package org.cubeville.cvelvenworkshop.managers;

import org.cubeville.cvelvenworkshop.models.Gift;
import org.cubeville.cvelvenworkshop.models.WrappingColor;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class WrappingColorManager {
    static Map<String, WrappingColor> colorMap = new HashMap<>();

    public static void registerColor(String internalName, WrappingColor color) {
        colorMap.put(internalName, color);
    }

    public static Map<String, WrappingColor> getColors() {
        return colorMap;
    }

    public static WrappingColor getColor(String colorName) {
        return colorMap.get(colorName);
    }

    @Nullable
    public static WrappingColor getRandomColor() {
        Integer weight = 0;
        Collection<WrappingColor> colorList = colorMap.values();
        Random rand = new Random();
        Integer value = rand.nextInt(colorList.size());
        return colorList.stream().toList().get(value);
    }
}
