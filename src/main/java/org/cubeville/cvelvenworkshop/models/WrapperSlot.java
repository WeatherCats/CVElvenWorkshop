package org.cubeville.cvelvenworkshop.models;

import org.bukkit.entity.Player;
import org.cubeville.cvgames.utils.GameUtils;

public class WrapperSlot {
    WrappedGift wrappedGift;
    Wrapper wrapper;
    Boolean isWrapping = false;
    Integer duration;
    Integer progress = 0;

    public WrapperSlot(Wrapper wrapper) {
        this.wrapper = wrapper;
    }

    public void incrementProgress() {
        if (isWrapping) {
            progress++;
        }
    }

    public Integer getProgress() {
        return progress;
    }

    public Integer getDuration() {
        return duration;
    }

    public WrappedGift getGift() {
        return wrappedGift;
    }

    public Boolean getWrapping() {
        return isWrapping;
    }

    public void setWrappedGift(WrappedGift wrappedGift) {
        this.wrappedGift = wrappedGift;
    }

    public void setWrapping(Boolean wrapping) {
        isWrapping = wrapping;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public Boolean isComplete() {
        return (progress >= getModifiedDuration());
    }

    public Integer getModifiedDuration() {
        return (int) Math.round(duration*(1+wrapper.getModifier()));
    }

    public String getProgressBar() {
        if (isWrapping) {
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

    public void claimWrappedGift(Player p) {
        p.getInventory().addItem(wrappedGift.getItem());
        isWrapping = false;
        duration = 0;
        progress = 0;
    }
}
