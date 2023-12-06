package org.cubeville.cvelvenworkshop.models;

import org.bukkit.entity.Player;
import org.cubeville.cvgames.utils.GameUtils;

public class ForgeSlot {
    Forge forge;
    Gift gift;
    Boolean isCrafting = false;
    Integer duration;
    Integer progress = 0;

    public ForgeSlot(Forge forge) {
        this.forge = forge;
    }

    public void incrementProgress() {
        if (isCrafting) {
            progress++;
        }
    }

    public Integer getProgress() {
        return progress;
    }

    public Integer getDuration() {
        return duration;
    }

    public Gift getGift() {
        return gift;
    }

    public Boolean getCrafting() {
        return isCrafting;
    }

    public void setGift(Gift gift) {
        this.gift = gift;
    }

    public void setCrafting(Boolean crafting) {
        isCrafting = crafting;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public Boolean isComplete() {
        return (progress >= getModifiedDuration());
    }

    public Integer getModifiedDuration() {
        return (int) Math.round(duration*(1+forge.getModifier()));
    }

    public String getProgressBar() {
        if (isCrafting) {
            if (isComplete()) {
                return GameUtils.createColorString("&a&lCOMPLETE");
            } else {
                Float prop = getProgress()/((float) getModifiedDuration());
                Integer bars = (int) Math.floor(prop*20);
                String bar = "▋";
                return GameUtils.createColorString("&a" + bar.repeat(bars) + "&7" + bar.repeat(20-bars));
            }
        }
        else {
            return GameUtils.createColorString("&7Inactive");
        }
    }

    public void claimGift(Player p) {
        p.getInventory().addItem(gift.getItem());
        isCrafting = false;
        duration = 0;
        progress = 0;
    }
}
