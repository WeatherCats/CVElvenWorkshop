package org.cubeville.cvelvenworkshop.managers;

import org.cubeville.cvelvenworkshop.models.EWMaterial;
import org.cubeville.cvelvenworkshop.models.Upgrade;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class UpgradeManager {
    static Map<String, Upgrade> upgradeMap = new LinkedHashMap<>();

    public static void registerUpgrade(String internalName, Upgrade upgrade) {
        upgradeMap.put(internalName, upgrade);
    }

    public static Map<String, Upgrade> getUpgrades() {
        return upgradeMap;
    }

    public static Upgrade getUpgrade(String upgradeName) {
        return upgradeMap.get(upgradeName);
    }
}
