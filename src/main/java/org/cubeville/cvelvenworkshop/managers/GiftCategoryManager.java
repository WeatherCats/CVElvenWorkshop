package org.cubeville.cvelvenworkshop.managers;

import org.cubeville.cvelvenworkshop.models.EWMaterial;
import org.cubeville.cvelvenworkshop.models.GiftCategory;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class GiftCategoryManager {
    static Map<String, GiftCategory> categoryMap = new LinkedHashMap<>();
    
    public static void registerCategory(String internalName, GiftCategory category) {
        categoryMap.put(internalName, category);
    }
    
    public static Map<String, GiftCategory> getCategories() {
        return categoryMap;
    }
    
    public static GiftCategory getCategory(String categoryName) {
        return categoryMap.get(categoryName);
    }
}
