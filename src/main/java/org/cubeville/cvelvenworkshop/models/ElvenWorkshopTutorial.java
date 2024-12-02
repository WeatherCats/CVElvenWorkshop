package org.cubeville.cvelvenworkshop.models;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.scheduler.BukkitTask;
import org.cubeville.cvelvenworkshop.CVElvenWorkshop;
import org.cubeville.cvelvenworkshop.elvenworkshop.ElvenWorkshop;
import org.cubeville.cvelvenworkshop.managers.EWMaterialManager;
import org.cubeville.cvelvenworkshop.managers.GiftManager;
import org.cubeville.cvelvenworkshop.managers.WrappingColorManager;
import org.cubeville.cvelvenworkshop.utils.EWInventoryUtils;
import org.cubeville.cvgames.utils.GameUtils;

public class ElvenWorkshopTutorial implements Listener {
    ElvenWorkshop game;
    int stage = 0;
    final String PREFIX = GameUtils.createColorString("&e&lTUTORIAL: ");
    BukkitTask task;
    
    public ElvenWorkshopTutorial(ElvenWorkshop game) {
        this.game = game;
        Bukkit.getPluginManager().registerEvents(this, CVElvenWorkshop.getInstance());
    }
    
    public void start() {
        progressTutorial();
    }
    
    public int getStage() {
        return stage;
    }
    
    public void progressTutorial() {
        switch (stage) {
            case 0 -> {
                game.sendMessageToArena(GameUtils.createColorString(
                    PREFIX + "&fOh good, some fresh hands. " +
                    "It's &bnearly Christmas &fbut none of the elves have shown up for work... " +
                    "&bwould you mind pitching in? &fThis &bsnowstorm &fhas been really rough."));
                task = Bukkit.getScheduler().runTaskLater(CVElvenWorkshop.getInstance(), () -> {
                    progressTutorial();
                }, 200);
            }
            case 1 -> {
                task = null;
                game.sendMessageToArena(GameUtils.createColorString(
                    PREFIX + "&fWell, since you’re still here I’m going to take that as a yes. " +
                        "Oh, look at that! The &bsnowstorm &fis making more &bsnowballs &fappear than normal! " +
                        "Go ahead and &apick up 5 snowballs&f!"));
            }
            case 2 -> {
                game.sendMessageToArena(GameUtils.createColorString(
                    PREFIX + "&fAs you pick up &bsnowballs&f, " +
                        "you can see you are gaining &bsnowflakes &fon the right hand side of your screen. " +
                        "These are your &bcurrency&f. You can upgrade various aspects of the workshop with them. " +
                        "Do keep in mind that these are shared if you are working with others."));
                task = Bukkit.getScheduler().runTaskLater(CVElvenWorkshop.getInstance(), () -> {
                    progressTutorial();
                }, 200);
            }
            case 3 -> {
                task = null;
                game.sendMessageToArena(GameUtils.createColorString(
                    PREFIX + "&fYou might’ve noticed some &bgenerators &fscattered about outside the workshop, " +
                        "go ahead and &acollect 10 cloth&f."));
            }
            case 4 -> {
                task = null;
                game.sendMessageToArena(GameUtils.createColorString(
                    PREFIX + "&fGreat job!"));
                task = Bukkit.getScheduler().runTaskLater(CVElvenWorkshop.getInstance(), () -> {
                    progressTutorial();
                }, 80);
            }
            case 5 -> {
                task = null;
                game.addSpecialOrder(GiftManager.getGift("doll"), WrappingColorManager.getColor("purple"));
                game.sendMessageToArena(GameUtils.createColorString(
                    PREFIX + "&fOh, look at that! You got an &border&f! " +
                        "You’ll need to &bcraft &fthe requested item using your collected &bmaterials&f, " +
                        "&bwrap &fit in the color requested, and then &bdeliver &fit. " +
                        "Looks like this order is for a &bdoll&f, so that cloth you collected will come in handy!"));
                task = Bukkit.getScheduler().runTaskLater(CVElvenWorkshop.getInstance(), () -> {
                    progressTutorial();
                }, 200);
            }
            case 6 -> {
                task = null;
                game.sendMessageToArena(GameUtils.createColorString(
                    PREFIX + "&fGo ahead and enter the &bworkshop&f. " +
                        "Inside, you will find the &bforge &fin one of the corners. " +
                        "&aRight click on it, select the first forge slot, then select the doll&f."));
            }
            case 7 -> {
                game.sendMessageToArena(GameUtils.createColorString(
                    PREFIX + "&fVery good! The &bforge &fis now crafting the &bdoll&f! " +
                        "&aOnce it is done, you can collect the doll by clicking on the first forge slot&f."));
            }
            case 8 -> {
                game.sendMessageToArena(GameUtils.createColorString(
                    PREFIX + "&fWonderful! Now, to &bwrap &fit. " +
                        "Opposite the forge is the &bwrapper&f. &aClick on it, and select the first wrapper slot, " +
                        "click on the item you wish to wrap in your inventory, then select the color (in this case purple)&f. " +
                        "To speed this up, you can also click on the item in your inventory when you open the menu initially to put in the item."));
            }
            case 9 -> {
                game.sendMessageToArena(GameUtils.createColorString(
                    PREFIX + "&fGreat! Now once the &bwrapper &fis done, " +
                    "you can &atake out the item much like you did with the forge&f."));
            }
            case 10 -> {
                game.sendMessageToArena(GameUtils.createColorString(
                    PREFIX + "&fNow, it’s time to &bdeliver &fit. " +
                    "Hold the item and look around. You should see a large floating item, glowing in the sky. " +
                    "That’ll be where you need to &bdeliver &fto! All you need to do is &awalk over there to deliver&f."));
            }
            case 11 -> {
                game.sendMessageToArena(GameUtils.createColorString(
                    PREFIX + "&fAmazing! That’s the basics on how to play! " +
                        "As the game progresses, the speed of orders being placed increases. " +
                        "As I mentioned earlier, you can use the &bsnowflakes &fearned by picking them up or completing orders to &bpurchase upgrades&f, " +
                        "and basically everything can be upgraded. As you deliver orders, you also gain &bJoy&f, which represents your score. " +
                        "&bGood luck & have fun out there&f!"));
                task = Bukkit.getScheduler().runTaskLater(CVElvenWorkshop.getInstance(), () -> {
                    Bukkit.getScheduler().runTask(CVElvenWorkshop.getInstance(), () -> {
                        task = null;
                        end();
                        game.finishGame();
                    });
                }, 200);
            }
        }
        stage++;
    }
    
    public void end() {
        if (task != null) {
            task.cancel();
        }
        HandlerList.unregisterAll(this);
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerPickup(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        if (!game.inGame(player)) {
            return;
        }
        if (!game.isTutorial()) {
            return;
        }
        if (stage != 4) {
            return;
        }
        Bukkit.getScheduler().runTaskLater(CVElvenWorkshop.getInstance(), () -> {
            if (EWInventoryUtils.countMaterial(player, EWMaterialManager.getMaterial("cloth")) < 10) {
                return;
            }
            progressTutorial();
        }, 1);
    }
}
