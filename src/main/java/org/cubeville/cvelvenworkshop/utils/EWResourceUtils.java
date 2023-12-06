package org.cubeville.cvelvenworkshop.utils;

import org.cubeville.cvelvenworkshop.CVElvenWorkshop;
import org.cubeville.cvgames.utils.GameUtils;

public class EWResourceUtils {
    public static String getSnowflakeColor(Boolean hex) {
        if (hex) {
            return CVElvenWorkshop.getConfigData().getString("snowflake-color.hex");
        } else {
            return CVElvenWorkshop.getConfigData().getString("snowflake-color.sidebar");
        }
    }

    public static String getJoyColor(Boolean hex) {
        if (hex) {
            return CVElvenWorkshop.getConfigData().getString("joy-color.hex");
        } else {
            return CVElvenWorkshop.getConfigData().getString("joy-color.sidebar");
        }
    }

    public static String getSnowflakeDisplay(Integer amount, Boolean hex) {
        return GameUtils.createColorString(getSnowflakeColor(hex) + amount + "❄");
    }

    public static String getJoyDisplay(Integer amount, Boolean hex) {
        return GameUtils.createColorString(getJoyColor(hex) + amount + "\uD83D\uDE04");
    }
}
