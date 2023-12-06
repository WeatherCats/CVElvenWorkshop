package org.cubeville.cvelvenworkshop.models;

import org.bukkit.entity.Player;

public class InfluencerSlot {
    Gift gift;
    Influencer influencer;
    Boolean isActive = false;

    public InfluencerSlot(Influencer influencer) {
        this.influencer = influencer;
    }


    public Integer getAddedWeight() {
        return gift.getWeight();
    }

    public Gift getGift() {
        return gift;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setGift(Gift gift) {
        this.gift = gift;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public Integer getModifiedWeight() {
        return (int) Math.round(getAddedWeight()*(1+influencer.getModifier()));
    }

    public void reset(Player p) {
        gift = null;
        isActive = false;
    }

    public void selectGift(Gift g) {
        gift = g;
        isActive = true;
    }
}
