package org.cubeville.cvelvenworkshop.enums;

import org.bukkit.Material;

public enum QuickChatOption {
    START(Material.SPRUCE_SIGN, "Start"),
    EXIT(Material.BARRIER, "Exit"),
    GENERAL(Material.BOOK, "General"),
    GENERAL_YES(Material.LIME_DYE, "Yes"),
    GENERAL_NO(Material.RED_DYE, "No"),
    GENERAL_OMW(Material.LEATHER_BOOTS, "On my way"),
    GENERAL_NEEDHELP(Material.LEVER, "Need help"),
    JOBS(Material.LECTERN, "Jobs"),
    JOBS_FORGING(Material.BLAST_FURNACE, "I am forging"),
    JOBS_WRAPPING(Material.LOOM, "I am wrapping"),
    JOBS_FORGINGANDWRAPPING(Material.CRAFTING_TABLE, "I am foring & wrapping"),
    JOBS_GATHERING(Material.DECORATED_POT, "I am gathering"),
    JOBS_DELIVERING(Material.CHEST_MINECART, "I am delivering"),
    JOBS_AVAILABLE(Material.BOOK, "I am available"),
    MATERIALS(Material.DECORATED_POT, "Materials"),
    MATERIALS_ALL(Material.DECORATED_POT, "I have materials"),
    MATERIALS_CLOTH(Material.GUSTER_BANNER_PATTERN, "I have cloth"),
    MATERIALS_LEATHER(Material.LEATHER, "I have leather"),
    MATERIALS_IRON(Material.IRON_INGOT, "I have iron"),
    MATERIALS_REDSTONE(Material.REDSTONE, "I have redstone"),
    MATERIALS_DIAMOND(Material.DIAMOND, "I have diamonds");
    
    public final Material material;
    public final String display;
    
    private QuickChatOption(Material material, String display) {
        this.material = material;
        this.display = display;
    }
}
