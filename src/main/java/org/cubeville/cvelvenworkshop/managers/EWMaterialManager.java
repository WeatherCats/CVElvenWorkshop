package org.cubeville.cvelvenworkshop.managers;

import org.bukkit.Material;
import org.cubeville.cvelvenworkshop.models.EWMaterial;

import java.util.HashMap;
import java.util.Map;

public class EWMaterialManager {
    static Map<String, EWMaterial> materialMap = new HashMap<>();

    public static void registerMaterial(String internalName, EWMaterial material) {
        materialMap.put(internalName, material);
    }

    public static Map<String, EWMaterial> getMaterials() {
        return materialMap;
    }

    public static EWMaterial getMaterial(String materialName) {
        return materialMap.get(materialName);
    }
}
