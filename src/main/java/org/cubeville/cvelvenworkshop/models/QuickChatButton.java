package org.cubeville.cvelvenworkshop.models;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.cubeville.cvelvenworkshop.CVElvenWorkshop;
import org.cubeville.cvelvenworkshop.elvenworkshop.ElvenWorkshop;
import org.cubeville.cvelvenworkshop.enums.QuickChatOption;
import org.cubeville.cvelvenworkshop.guis.ForgeMenuGUI;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.Objects;
import java.util.function.Predicate;

import static org.bukkit.Bukkit.getServer;

public class QuickChatButton implements Listener {
    QuickChatOption option;
    ItemDisplay itemDisplay;
    TextDisplay textDisplay;
    Interaction interaction;
    BukkitTask task;
    QuickChatMenu menu;
    Player player;
    Location startLocation;
    float yaw = 0;
    float degrees = 0;
    boolean isCenter = false;
    
    public QuickChatButton(QuickChatMenu menu, Player player, QuickChatOption option, float yaw, float degrees, boolean center) {
        this.menu = menu;
        this.player = player;
        this.option = option;
        this.yaw = yaw;
        this.degrees = degrees;
        this.isCenter = center;
        
        startLocation = getLocation();
    }
    
    public void close() {
        itemDisplay.remove();
        textDisplay.remove();
        interaction.remove();
        HandlerList.unregisterAll(this);
        task.cancel();
    }
    
    public void activate() {
        spawnItemDisplay();
        spawnTextDisplay();
        spawnInteraction();
        for (Player hidePlayer : Bukkit.getOnlinePlayers()) {
            if (player.equals(hidePlayer)) continue;
            hidePlayer.hideEntity(CVElvenWorkshop.getInstance(), itemDisplay);
            hidePlayer.hideEntity(CVElvenWorkshop.getInstance(), textDisplay);
            hidePlayer.hideEntity(CVElvenWorkshop.getInstance(), interaction);
        }
        task = Bukkit.getScheduler().runTaskTimer(CVElvenWorkshop.getInstance(), () -> {
            updateLocations();
            Transformation itemTransform = itemDisplay.getTransformation();
            RayTraceResult result = player.getWorld().rayTraceEntities(player.getEyeLocation(), player.getEyeLocation().getDirection(), 4, Predicate.isEqual(interaction));
            if (result != null && Objects.equals(interaction, result.getHitEntity())) {
                if (itemDisplay.isGlowing()) return;
                itemTransform.getScale().set(0.3f);
                itemDisplay.setGlowing(true);
                textDisplay.setInterpolationDelay(0);
                textDisplay.setInterpolationDuration(1);
            } else {
                if (!itemDisplay.isGlowing()) return;
                itemTransform.getScale().set(0.2f);
                itemDisplay.setGlowing(false);
                textDisplay.setInterpolationDelay(0);
                textDisplay.setInterpolationDuration(1);
            }
            itemDisplay.setTransformation(itemTransform);
        }, 1, 1);
    }
    
    @EventHandler
    public void onInteract(PlayerInteractEntityEvent e) {
        if (e.getRightClicked() != interaction) return;
        if (!menu.game.inGame(e.getPlayer())) return;
        menu.activateOption(option);
    }
    
    private Location getLocation() {
        Location playerLoc = player.getEyeLocation();
        playerLoc.setYaw(yaw);
        playerLoc.setPitch(0);
        Vector vector = playerLoc.getDirection();
        vector = vector.multiply(1.5);
        Location middleLoc = playerLoc.add(vector);
        float angle = degrees;
        
        Location finalVectorLoc = middleLoc.clone();
        finalVectorLoc.setYaw(0);
        finalVectorLoc.setPitch(0);
        Vector finalVector = finalVectorLoc.toVector();
        if (isCenter) {
            middleLoc.setYaw(0);
            middleLoc.setPitch(0);
            return middleLoc;
        }
        
        if (angle <= 180.0f) {
            middleLoc.setYaw(middleLoc.getYaw() + 90);
            middleLoc.setPitch(-90 + angle);
        } else {
            middleLoc.setYaw(middleLoc.getYaw() - 90);
            middleLoc.setPitch(270 - angle);
        }
        
        vector = middleLoc.getDirection();
        Location finalLoc = middleLoc.add(vector);
        finalLoc.setYaw(0);
        finalLoc.setPitch(0);
        return finalLoc;
    }
    
    private void updateLocations() {
        interaction.teleport(getLocation().add(0, -0.2, 0));
        
        Location location = getLocation().clone();
        location.setYaw(0);
        location.setPitch(0);
        location = location.subtract(startLocation);
        location.setPitch(0);
        location.setYaw(0);
        
        Transformation transform = itemDisplay.getTransformation();
        if (transform.getTranslation().equals((float) location.getX(), (float) location.getY(), (float) location.getZ())) {
            return;
        }
        transform.getTranslation().set(location.getX(), location.getY(), location.getZ());
        itemDisplay.setTransformation(transform);
        itemDisplay.setInterpolationDelay(0);
        itemDisplay.setInterpolationDuration(1);
        
        transform = textDisplay.getTransformation();
        transform.getTranslation().set(location.getX(), location.getY(), location.getZ());
        textDisplay.setTransformation(transform);
        textDisplay.setInterpolationDelay(0);
        textDisplay.setInterpolationDuration(1);
    }
    
    private void spawnInteraction() {
        interaction = getLocation().getWorld().spawn(getLocation().clone().add(0, -0.2, 0), Interaction.class);
        interaction.setInteractionHeight(0.4f);
        interaction.setInteractionWidth(0.4f);
    }
    
    private void spawnItemDisplay() {
        itemDisplay = getLocation().getWorld().spawn(getLocation().clone().add(0, 0, 0), ItemDisplay.class);
        itemDisplay.setItemStack(new ItemStack(option.material, 1));
        itemDisplay.setBrightness(new Display.Brightness(15, 15));
        Transformation transform = itemDisplay.getTransformation();
        transform.getScale().set(0.2f);
        transform.getLeftRotation().set(new Quaternionf(0, 0, 0, 1).rotateAxis((float) Math.toRadians(yaw * -1), new Vector3f(0, 1, 0)));
        itemDisplay.setTransformation(transform);
//        itemDisplay.setBillboard(Display.Billboard.CENTER);
    }
    
    private void spawnTextDisplay() {
        textDisplay = getLocation().getWorld().spawn(getLocation().clone().add(0, 0.25, 0), TextDisplay.class);
        Transformation transform = textDisplay.getTransformation();
        transform.getScale().set(0.2f);
        transform.getLeftRotation().set(new Quaternionf(0, 0, 0, 1).rotateAxis((float) Math.toRadians((yaw * -1) - 180), new Vector3f(0, 1, 0)));
        textDisplay.setTransformation(transform);
//        textDisplay.setBillboard(Display.Billboard.CENTER);
        textDisplay.setDefaultBackground(false);
        textDisplay.setShadowed(true);
        textDisplay.setText(option.display);
        textDisplay.setBrightness(new Display.Brightness(15, 15));
    }
}
