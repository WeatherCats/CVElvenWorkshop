package org.cubeville.cvelvenworkshop.models;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Display;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Transformation;
import org.cubeville.cvelvenworkshop.CVElvenWorkshop;
import org.cubeville.cvelvenworkshop.elvenworkshop.ElvenWorkshop;
import org.cubeville.cvelvenworkshop.managers.GiftManager;
import org.cubeville.cvelvenworkshop.managers.WrappingColorManager;
import org.cubeville.cvelvenworkshop.utils.EWResourceUtils;
import org.cubeville.cvelvenworkshop.utils.EWInventoryUtils;
import org.cubeville.cvgames.utils.GameUtils;

import java.util.ArrayList;
import java.util.List;

public class Order {
    WrappedGift wrappedGift;
    Location location;
    ElvenWorkshop game;
    Integer ticksLapsed = 0;
    BossBar bar;
    BukkitTask task;
    ItemDisplay itemDisplay;
    TextDisplay textDisplay;

    public Order(ElvenWorkshop game) {
        this.game = game;
        this.wrappedGift = new WrappedGift(game.getRandomGift(), WrappingColorManager.getRandomColor());
        this.location = game.getRandomDeliveryLocation();
    }

    public void activate() {
        bar = Bukkit.createBossBar(wrappedGift.getOrderName(), BarColor.GREEN, BarStyle.SOLID);
        List<Player> players = new ArrayList<>(game.getPlayers().stream().toList());
        for (Player p : players) {
            bar.addPlayer(p);
        }
        game.sendMessageToArena(GameUtils.createColorString("&a&lNEW ORDER! " + wrappedGift.getOrderName() + "&r &f(Value: " + EWResourceUtils.getSnowflakeDisplay(wrappedGift.getGiftItem().getValue(), true) + "&f)"));
        spawnItemDisplay();
        spawnTextDisplay();
        task = Bukkit.getScheduler().runTaskTimer(CVElvenWorkshop.getInstance(), () -> {
            ticksLapsed += 1;
            Boolean glowing = false;
            Double prop;
            Integer timeBonus1 = CVElvenWorkshop.getConfigData().getInt("time-bonuses.1.time") * 20;
            Integer timeBonus2 = CVElvenWorkshop.getConfigData().getInt("time-bonuses.2.time") * 20;
            if (ticksLapsed <= timeBonus1) {
                prop = (timeBonus1-ticksLapsed) / (double) timeBonus1;
                bar.setColor(BarColor.GREEN);
            } else if (ticksLapsed <= timeBonus2) {
                prop = (timeBonus2-ticksLapsed) / (double) timeBonus2;
                bar.setColor(BarColor.YELLOW);
            } else {
                prop = 1.0;
                bar.setColor(BarColor.RED);
            }
            String string = GameUtils.createColorString(wrappedGift.getOrderName() + "&r &f(" + EWResourceUtils.getSnowflakeDisplay(wrappedGift.getGiftItem().getValue(), true) + "&f)");
            if (ticksLapsed <= timeBonus2) {
                string = GameUtils.createColorString(string + " &a(+" + getSpeedBonus() + " Bonus)");
            }
            bar.setTitle(string);
            bar.setProgress(prop);
            updateTextDisplay();
            itemDisplay.setRotation(itemDisplay.getLocation().getYaw()+2, 0);
            for (Player p : game.getPlayers()) {
                ItemStack item = p.getInventory().getItemInMainHand();
                if (EWInventoryUtils.matchesWrappedGift(item, wrappedGift)) {
                    glowing = true;
                }
                itemDisplay.setGlowing(EWInventoryUtils.matchesWrappedGift(item, wrappedGift));
                if (p.getLocation().distance(location) <= 3) {
                    if (EWInventoryUtils.hasWrappedGift(p, wrappedGift)) game.claimGift(this, p);
                    break;
                }
                itemDisplay.setGlowing(glowing);
            }
        }, 1, 1);
    }

    private void spawnItemDisplay() {
        itemDisplay = location.getWorld().spawn(location.clone().add(0, 15, 0), ItemDisplay.class);
        itemDisplay.setItemStack(wrappedGift.giftItem.getItem());
        Transformation transform = itemDisplay.getTransformation();
        transform.getScale().set(5);
        itemDisplay.setTransformation(transform);
    }

    private void spawnTextDisplay() {
        textDisplay = location.getWorld().spawn(location.clone().add(0, 18, 0), TextDisplay.class);
        updateTextDisplay();
        Transformation transform = textDisplay.getTransformation();
        transform.getScale().set(4);
        textDisplay.setTransformation(transform);
        textDisplay.setBillboard(Display.Billboard.CENTER);
        textDisplay.setDefaultBackground(false);
        textDisplay.setShadowed(true);
    }

    private void updateTextDisplay() {
        String string = GameUtils.createColorString(wrappedGift.getOrderName() + "&r &f(" + EWResourceUtils.getSnowflakeDisplay(wrappedGift.getGiftItem().getValue(), true) + "&f)");
        Integer timeBonus2 = CVElvenWorkshop.getConfigData().getInt("time-bonuses.2.time") * 20;
        if (ticksLapsed <= timeBonus2) {
            string = GameUtils.createColorString(string + " &a(+" + getSpeedBonus() + " Bonus)");
        }
        textDisplay.setText(string);
    }

    public void remove() {
        task.cancel();
        bar.removeAll();
        bar = null;
        itemDisplay.remove();
        textDisplay.remove();
    }

    public void removePlayer(Player p) {
        if (bar != null) {
            bar.removePlayer(p);
        }
    }

    public WrappedGift getWrappedGift() {
        return wrappedGift;
    }

    public Location getLocation() {
        return location;
    }

    public ElvenWorkshop getGame() {
        return game;
    }

    public Integer getTicksLapsed() {
        return ticksLapsed;
    }

    public BossBar getBar() {
        return bar;
    }

    public Integer getSpeedBonus() {
        Integer timeBonus1 = CVElvenWorkshop.getConfigData().getInt("time-bonuses.1.time") * 20;
        Integer bonusScore1 = CVElvenWorkshop.getConfigData().getInt("time-bonuses.1.bonus");
        Integer timeBonus2 = CVElvenWorkshop.getConfigData().getInt("time-bonuses.2.time") * 20;
        Integer bonusScore2 = CVElvenWorkshop.getConfigData().getInt("time-bonuses.2.bonus");
        if (ticksLapsed <= timeBonus1) {
            return bonusScore1;
        } else if (ticksLapsed <= timeBonus2) {
            return bonusScore2;
        } else return 0;
    }
}
