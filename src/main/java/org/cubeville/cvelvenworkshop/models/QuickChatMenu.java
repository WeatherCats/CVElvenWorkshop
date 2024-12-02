package org.cubeville.cvelvenworkshop.models;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.scheduler.BukkitTask;
import org.cubeville.cvelvenworkshop.CVElvenWorkshop;
import org.cubeville.cvelvenworkshop.elvenworkshop.ElvenWorkshop;
import org.cubeville.cvelvenworkshop.enums.QuickChatOption;
import org.cubeville.cvelvenworkshop.managers.EWMaterialManager;
import org.cubeville.cvelvenworkshop.utils.EWInventoryUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.bukkit.Bukkit.getServer;

public class QuickChatMenu {
    Player player;
    List<QuickChatButton> buttons = new ArrayList<>();
    ElvenWorkshop game;
    float yaw = 0;
    
    public QuickChatMenu(ElvenWorkshop game, Player player) {
        this.game = game;
        this.player = player;
        yaw = player.getYaw();
    }
    
    public void activate() {
        activateOption(QuickChatOption.START);
    }
    
    public void activateOption(QuickChatOption option) {
        switch (option) {
            case EXIT -> {
                close();
            }
            case START -> {
                openMenu(QuickChatOption.EXIT, QuickChatOption.GENERAL, QuickChatOption.JOBS, QuickChatOption.MATERIALS);
            }
            case GENERAL -> {
                openMenu(QuickChatOption.START, QuickChatOption.GENERAL_YES, QuickChatOption.GENERAL_NO, QuickChatOption.GENERAL_OMW, QuickChatOption.GENERAL_NEEDHELP);
            }
            case GENERAL_YES -> {
                game.sendQuickChat(player, "Yes");
                close();
            }
            case GENERAL_NO -> {
                game.sendQuickChat(player, "No");
                close();
            }
            case GENERAL_OMW -> {
                game.sendQuickChat(player, "On my way");
                close();
            }
            case GENERAL_NEEDHELP -> {
                game.sendQuickChat(player, "I need help");
                close();
            }
            case JOBS -> {
                openMenu(QuickChatOption.START, QuickChatOption.JOBS_FORGING, QuickChatOption.JOBS_WRAPPING, QuickChatOption.JOBS_FORGINGANDWRAPPING, QuickChatOption.JOBS_GATHERING, QuickChatOption.JOBS_DELIVERING, QuickChatOption.JOBS_AVAILABLE);
            }
            case JOBS_FORGING -> {
                game.sendQuickChat(player, "I am working on forging gifts");
                close();
            }
            case JOBS_WRAPPING -> {
                game.sendQuickChat(player, "I am working on wrapping gifts");
                close();
            }
            case JOBS_FORGINGANDWRAPPING -> {
                game.sendQuickChat(player, "I am working on forging & wrapping gifts");
                close();
            }
            case JOBS_GATHERING -> {
                game.sendQuickChat(player, "I am working on gathering materials");
                close();
            }
            case JOBS_DELIVERING -> {
                game.sendQuickChat(player, "I am working on delivering gifts");
                close();
            }
            case JOBS_AVAILABLE -> {
                game.sendQuickChat(player, "I am available to help");
                close();
            }
            case MATERIALS -> {
                openMenu(QuickChatOption.START, QuickChatOption.MATERIALS_ALL, QuickChatOption.MATERIALS_CLOTH, QuickChatOption.MATERIALS_LEATHER, QuickChatOption.MATERIALS_IRON, QuickChatOption.MATERIALS_REDSTONE, QuickChatOption.MATERIALS_DIAMOND);
            }
            case MATERIALS_ALL -> {
                int i = 0;
                String message = "";
                for (Map.Entry<String, EWMaterial> materialEntry : EWMaterialManager.getMaterials().entrySet()) {
                    EWMaterial mat = materialEntry.getValue();
                    Integer amount = EWInventoryUtils.countMaterial(player, mat);
                    String matDisplay = mat.getPluralDisplayName();
                    if (amount == 1) {
                        matDisplay = mat.getDisplayName();
                    } else if (amount == 0) {
                        continue;
                    }
                    if (i > 0) {
                        message += "&f, ";
                    }
                    message += mat.getColorCode() + amount + " " + matDisplay;
                    i++;
                }
                if (message.isEmpty()) {
                    message = "no materials";
                }
                game.sendQuickChat(player, "I have " + message);
                close();
            }
            case MATERIALS_CLOTH -> {
                EWMaterial mat = EWMaterialManager.getMaterial("cloth");
                Integer amount = EWInventoryUtils.countMaterial(player, mat);
                String matDisplay = mat.getPluralDisplayName();
                if (amount == 1) {
                    matDisplay = mat.getDisplayName();
                }
                String message = mat.getColorCode() + amount + " " + matDisplay;
                game.sendQuickChat(player, "I have " + message);
                close();
            }
            case MATERIALS_LEATHER -> {
                EWMaterial mat = EWMaterialManager.getMaterial("leather");
                Integer amount = EWInventoryUtils.countMaterial(player, mat);
                String matDisplay = mat.getPluralDisplayName();
                if (amount == 1) {
                    matDisplay = mat.getDisplayName();
                }
                String message = mat.getColorCode() + amount + " " + matDisplay;
                game.sendQuickChat(player, "I have " + message);
                close();
            }
            case MATERIALS_IRON -> {
                EWMaterial mat = EWMaterialManager.getMaterial("iron");
                Integer amount = EWInventoryUtils.countMaterial(player, mat);
                String matDisplay = mat.getPluralDisplayName();
                if (amount == 1) {
                    matDisplay = mat.getDisplayName();
                }
                String message = mat.getColorCode() + amount + " " + matDisplay;
                game.sendQuickChat(player, "I have " + message);
                close();
            }
            case MATERIALS_REDSTONE -> {
                EWMaterial mat = EWMaterialManager.getMaterial("redstone");
                Integer amount = EWInventoryUtils.countMaterial(player, mat);
                String matDisplay = mat.getPluralDisplayName();
                if (amount == 1) {
                    matDisplay = mat.getDisplayName();
                }
                String message = mat.getColorCode() + amount + " " + matDisplay;
                game.sendQuickChat(player, "I have " + message);
                close();
            }
            case MATERIALS_DIAMOND -> {
                EWMaterial mat = EWMaterialManager.getMaterial("diamond");
                Integer amount = EWInventoryUtils.countMaterial(player, mat);
                String matDisplay = mat.getPluralDisplayName();
                if (amount == 1) {
                    matDisplay = mat.getDisplayName();
                }
                String message = mat.getColorCode() + amount + " " + matDisplay;
                game.sendQuickChat(player, "I have " + message);
                close();
            }
        }
    }
    
    public void openMenu(QuickChatOption centerOption, QuickChatOption... options) {
        clear();
        QuickChatButton button = new QuickChatButton(this, player, centerOption, yaw, 0, true);
        getServer().getPluginManager().registerEvents(button, CVElvenWorkshop.getInstance());
        button.activate();
        buttons.add(button);
        float increment = 360.0f / options.length;
        float degrees = 0;
        for (QuickChatOption option : options) {
            button = new QuickChatButton(this, player, option, yaw, degrees, false);
            getServer().getPluginManager().registerEvents(button, CVElvenWorkshop.getInstance());
            button.activate();
            buttons.add(button);
            degrees += increment;
        }
    }
    
    public void close() {
        clear();
        game.getState(player).quickChatMenu = null;
    }
    
    public void clear() {
        for (QuickChatButton button : buttons) {
            button.close();
            Bukkit.getScheduler().runTask(CVElvenWorkshop.getInstance(), o -> {
                buttons.remove(button);
            });
        }
    }
    
    
}
