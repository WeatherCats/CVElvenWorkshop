package org.cubeville.cvelvenworkshop;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.cubeville.cvelvenworkshop.managers.*;
import org.cubeville.cvelvenworkshop.models.*;
import org.cubeville.cvelvenworkshop.utils.EWInventoryUtils;
import org.cubeville.cvelvenworkshop.utils.EWResourceUtils;
import org.cubeville.cvgames.CVGames;
import org.cubeville.cvelvenworkshop.elvenworkshop.ElvenWorkshopMain;
import org.cubeville.cvelvenworkshop.elvenworkshop.ElvenWorkshop;
import org.cubeville.cvgames.managers.ArenaManager;
import org.cubeville.cvgames.managers.GameManager;
import org.cubeville.cvgames.managers.PlayerManager;
import org.cubeville.cvgames.models.Arena;
import org.cubeville.cvgames.utils.GameUtils;

import java.net.MalformedURLException;

public final class CVElvenWorkshop extends JavaPlugin {

    private static CVElvenWorkshop instance;
    private static FileConfiguration config;

    @Override
    public void onEnable() {
        // Plugin startup logic
        instance = this;
        CVGames.gameManager().registerGame("elvenworkshop", ElvenWorkshopMain::new);
        CVGames.gameManager().registerGame("elvenworkshopinstance", ElvenWorkshop::new);
        config = this.getConfig();
        config.options().copyDefaults(true);
        this.saveConfig();
        loadMaterials();
        loadCategories();
        loadGifts();
        loadColors();
        loadUpgrades();
    }

    private void loadMaterials() {
        EWMaterialManager.registerMaterial("diamond", new EWMaterial(config.getConfigurationSection("materials.diamond"), "diamond"));
        EWMaterialManager.registerMaterial("iron", new EWMaterial(config.getConfigurationSection("materials.iron"), "iron"));
        EWMaterialManager.registerMaterial("redstone", new EWMaterial(config.getConfigurationSection("materials.redstone"), "redstone"));
        EWMaterialManager.registerMaterial("leather", new EWMaterial(config.getConfigurationSection("materials.leather"), "leather"));
        EWMaterialManager.registerMaterial("cloth", new EWMaterial(config.getConfigurationSection("materials.cloth"), "cloth"));
    }

    private void loadGifts() {
        for (String key : config.getConfigurationSection("gifts").getKeys(false)) {
            GiftManager.registerGift(key, new Gift(config.getConfigurationSection("gifts." + key), key));
        }
    }

    private void loadColors() {
        for (String key : config.getConfigurationSection("wrapping-colors").getKeys(false)) {
            try {
                WrappingColorManager.registerColor(key, new WrappingColor(config.getConfigurationSection("wrapping-colors." + key), key));
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void loadUpgrades() {
        for (String key : config.getConfigurationSection("upgrades").getKeys(false)) {
            UpgradeManager.registerUpgrade(key, new Upgrade(config.getConfigurationSection("upgrades." + key), key));
        }
    }
    
    private void loadCategories() {
        for (String key : config.getConfigurationSection("categories").getKeys(false)) {
            GiftCategoryManager.registerCategory(key, new GiftCategory(config.getConfigurationSection("categories." + key), key));
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public static CVElvenWorkshop getInstance() { return instance; }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equals("elvenworkshop")) {
            switch (args[0]) {
                case "addplayer" -> {
                    if (args.length < 3) {
                        sender.sendMessage(GameUtils.createColorString("&cNot enough arguments!"));
                        return false;
                    }
                    if (args.length > 3) {
                        sender.sendMessage(GameUtils.createColorString("&cToo many arguments!"));
                        return false;
                    }
                    Player player = Bukkit.getPlayer(args[1]);
                    if (player == null) {
                        sender.sendMessage(GameUtils.createColorString("&cInvalid player!"));
                        return false;
                    }
                    Arena arena = ArenaManager.getArena(args[2]);
                    if (arena == null) {
                        sender.sendMessage(GameUtils.createColorString("&cInvalid arena!"));
                        return false;
                    }
                    ElvenWorkshopMain game = (ElvenWorkshopMain) arena.getGame("elvenworkshop");
                    game.onPlayerAdd(player);
                }
                case "debug" -> {
                    Player player = (Player) sender;
                    if (!player.hasPermission("elvenworkshop.debug")) {
                        return false;
                    }
                    ElvenWorkshop game = (ElvenWorkshop) PlayerManager.getPlayerArena(player).getGame("elvenworkshopinstance");
                    game.setDebugGame();
                    switch (args[1]) {
                        case "addsnowflakes" -> {
                            game.addSnowflakes(Integer.valueOf(args[2]));
                        }
                        case "setgamespeed" -> {
                            game.setGameSpeed(Integer.valueOf(args[2]));
                        }
                        case "getmaterials" -> {
                            for (EWMaterial material : EWMaterialManager.getMaterials().values()) {
                                ItemStack item = material.getItem();
                                item.setAmount(495);
                                player.getInventory().addItem(item);
                            }
                        }
                    }
                }
                default -> {
                    sender.sendMessage(GameUtils.createColorString("&cInvalid subcommand!"));
                    return false;
                }
            }
        }
        return false;
    }

    public static FileConfiguration getConfigData() {
        return config;
    }
}
