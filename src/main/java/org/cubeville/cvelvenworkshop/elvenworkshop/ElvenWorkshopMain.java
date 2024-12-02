package org.cubeville.cvelvenworkshop.elvenworkshop;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.extension.factory.PatternFactory;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.function.operation.ForwardExtentCopy;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.function.pattern.BlockPattern;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.block.BlockTypes;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BoundingBox;
import org.cubeville.cvelvenworkshop.CVElvenWorkshop;
import org.cubeville.cvelvenworkshop.enums.ElvenWorkshopGameType;
import org.cubeville.cvelvenworkshop.guis.GameSelectorGUI;
import org.cubeville.cvelvenworkshop.utils.ChunkUtils;
import org.cubeville.cvgames.CVGames;
import org.cubeville.cvgames.managers.ArenaManager;
import org.cubeville.cvgames.managers.PlayerManager;
import org.cubeville.cvgames.models.Arena;
import org.cubeville.cvgames.models.Game;
import org.cubeville.cvgames.models.GameRegion;
import org.cubeville.cvgames.models.PlayerState;
import org.cubeville.cvgames.utils.GameUtils;
import org.cubeville.cvgames.vartypes.*;

import java.util.*;

import static org.bukkit.Bukkit.getServer;

public class ElvenWorkshopMain extends Game {
    HashMap<Integer, ElvenWorkshop> gamesList = new HashMap<>();
    List<Player> playersJoining = new ArrayList<>();
    public ElvenWorkshopMain(String id, String arenaName) {
        super(id, arenaName);
//        addGameVariableObjectList("materials", new HashMap<>(){{
//            put("display-name", new GameVariableString());
//            put("display-color", new GameVariableChatColor());
//            put("item", new GameVariableItem());
//            put("generator-location", new GameVariableLocation());
//            addGameVariableObjectList("upgrade-levels", new HashMap<>(){{
//                put("cost", new GameVariableInt());
//                put("speed", new GameVariableInt());
//                put("amount", new GameVariableInt());
//            }});
//        }});
        addGameVariable("paste-location", new GameVariableLocation("Initial paste location for first slot"));
        addGameVariable("slot-paste-vector", new GameVariableLocation("The location added to the base location for each slot"));
        addGameVariable("diamond-generator", new GameVariableLocation("The location of the diamond generator"));
        addGameVariable("iron-generator", new GameVariableLocation("The location of the iron generator"));
        addGameVariable("redstone-generator", new GameVariableLocation("The location of the redstone generator"));
        addGameVariable("leather-generator", new GameVariableLocation("The location of the leather generator"));
        addGameVariable("cloth-generator", new GameVariableLocation("The location of the cloth generator"));
        addGameVariable("forge-location", new GameVariableLocation("The location of the gift forge"));
        addGameVariable("wrapper-location", new GameVariableLocation("The location of the gift wrapper"));
        addGameVariable("influencer-location", new GameVariableLocation("The location of the influencer"));
        addGameVariable("upgrade-station-location", new GameVariableLocation("The location of the upgrade station"));
        addGameVariable("delivery-locations", new GameVariableList<>(GameVariableLocation.class, 5, 100, "The locations of delivery spots"));
        addGameVariable("snowfall-region", new GameVariableRegion("The region where snowfall can occur"));
        addGameVariable("workshop-region", new GameVariableRegion("The region where workshop movement speed applies"));
        addGameVariable("spawn", new GameVariableLocation("The location players spawn when the game begins"));
    }

    public HashMap<Integer, ElvenWorkshop> getGames() {
        return gamesList;
    }

    @Override
    public void onPlayerLeave(Player player) {
        state.remove(player);
    }

    @Override
    protected ElvenWorkshopMainState getState(Player player) {
        return (ElvenWorkshopMainState) state.get(player);
    }

    @Override
    public void onGameStart(Set<Player> set) {

    }

    @Override
    public void onGameFinish() {

    }

    @Override
    public void onPlayerJoinGame(Player p) {
        arena.getQueue().leave(p);
        finishGame();
        onPlayerAdd(p);
    }

    public void onPlayerAdd(Player p) {
        if (PlayerManager.getPlayerArena(p) != null || playersJoining.contains(p)) {
            p.sendMessage(GameUtils.createColorString("&cYou are already in a game."));
            return;
        }
        GameSelectorGUI gui = new GameSelectorGUI(this);
        getServer().getPluginManager().registerEvents(gui, CVElvenWorkshop.getInstance());
        Bukkit.getScheduler().runTaskLater(CVElvenWorkshop.getInstance(), o -> {
            gui.openInventory(p);
        }, 1);
    }

    public void createGame(Player p, ElvenWorkshopGameType gameType) {
        int slot = -1;
        // Find an available slot
        for ( int i = 0; i <= gamesList.size(); i++) {
            if (!gamesList.containsKey(i)) {
                slot = i;
                break;
            }
        }
        // If something goes wrong and no slot is found, just inform the player and cancel
        if (slot == -1) {
            p.sendMessage("No valid slot found! Please report this error to the staff team!");
            return;
        }

        playersJoining.add(p);

        // Create a new arena to use for this instance
        String arenaName = "elvenworkshop_" + UUID.randomUUID().toString();
        ArenaManager.addArena(arenaName);
        Arena arena = ArenaManager.getArena(arenaName);
        // Copy variables from main game's arena to this new arena
        Arena mainArena = this.getArena();
        for (String variableName : mainArena.getVariables()) {
            GameVariable gv = mainArena.getGameVariable(variableName);
            switch (gv.path) {
                case "lobby":
                case "spawn":
                case "forge-location":
                case "wrapper-location":
                case "influencer-location":
                case "upgrade-station-location":
                case "diamond-generator":
                case "iron-generator":
                case "redstone-generator":
                case "leather-generator":
                case "cloth-generator":
                case "spectator-spawn": {
                    Location preLoc = ((GameRegion) mainArena.getVariable("region")).getMin().clone();
                    Location loc = ((Location) mainArena.getVariable("paste-location")).clone();
                    Location vec = ((Location) mainArena.getVariable("slot-paste-vector")).clone();
                    Location locationVariable = ((Location) mainArena.getVariable(variableName)).clone();
                    Location relLoc = locationVariable.subtract(preLoc);
                    Location newLoc = loc.add(vec.multiply(slot)).add(relLoc);
                    String newLocString = GameUtils.playerLocToString(newLoc);
                    GameVariable ngv = new GameVariableLocation();
                    ngv.setItem(newLocString, arenaName);
                    arena.addGameVariable(variableName, ngv);
                    break;
                }
                case "delivery-locations": {
                    GameVariableList ngv = new GameVariableList(GameVariableLocation.class);
                    List<String> items = new ArrayList<>();
                    for (Location location : (List<Location>) getVariable("delivery-locations")) {
                        Location preLoc = ((GameRegion) mainArena.getVariable("region")).getMin().clone();
                        Location loc = ((Location) mainArena.getVariable("paste-location")).clone();
                        Location vec = ((Location) mainArena.getVariable("slot-paste-vector")).clone();
                        Location locationVariable = location.clone();
                        Location relLoc = locationVariable.subtract(preLoc);
                        Location newLoc = loc.add(vec.multiply(slot)).add(relLoc);
                        String newLocString = GameUtils.playerLocToString(newLoc);
                        items.add(newLocString);
                    }
                    ngv.setItems(items, arenaName);
                    arena.addGameVariable(variableName, ngv);
                    break;
                }
                case "paste-location": {
                    Location loc = ((Location) mainArena.getVariable("paste-location")).clone();
                    Location vec = ((Location) mainArena.getVariable("slot-paste-vector")).clone();
                    Location newLoc = loc.add(vec.multiply(slot)).clone();
                    String newLocString = GameUtils.playerLocToString(newLoc);
                    GameVariable ngv = new GameVariableLocation();
                    ngv.setItem(newLocString, arenaName);
                    arena.addGameVariable(variableName, ngv);
                    break;
                }
                case "region":
                case "snowfall-region":
                case "workshop-region": {
                    Location loc = ((Location) mainArena.getVariable("paste-location")).clone().getBlock().getLocation();
                    Location vec = ((Location) mainArena.getVariable("slot-paste-vector")).clone().getBlock().getLocation();
                    Location minLoc = ((GameRegion) mainArena.getVariable(variableName)).getMin().clone();
                    Location maxLoc = ((GameRegion) mainArena.getVariable(variableName)).getMax().clone();
                    Location relMaxLoc = maxLoc.subtract(minLoc).clone();
                    Location relMinLoc = minLoc.clone().subtract(((GameRegion) mainArena.getVariable("region")).getMin().clone());
                    Location newMinLoc = loc.add(vec.multiply(slot)).clone().add(relMinLoc);
                    Location newMaxLoc = newMinLoc.clone().add(relMaxLoc).clone();
                    String regionString = GameUtils.blockLocToString(newMinLoc.getBlock().getLocation()) + " ~ " + GameUtils.blockLocToString(newMaxLoc.getBlock().getLocation());
                    GameVariable ngv = new GameVariableRegion();
                    ngv.setItem(regionString, arenaName);
                    arena.addGameVariable(variableName, ngv);
                    break;
                }
                default: {
                    GameVariable ngv = gv;
                    if (gameType.equals(ElvenWorkshopGameType.TUTORIAL) && (gv.path.equals("countdown-length") || gv.path.equals("queue-max"))) {
                        ngv = new GameVariableInt();
                        ngv.setItem(1, arenaName);
                    }
                    arena.addGameVariable(variableName, ngv);
                }
            }
        }

        // Import instanced game, save it to main game list, insert variables, then join it.
        ArenaManager.importArenaGame(arenaName, "elvenworkshopinstance");
        ElvenWorkshop instance = (ElvenWorkshop) arena.getGame("elvenworkshopinstance");
        gamesList.put(slot, instance);
        instance.parentGame = this;
        instance.setGameType(gameType);
        instance.slot = slot;
        // Teleport to template's lobby, then start pasting it in.
//        p.teleport((Location) mainArena.getVariable("lobby"));
        GameRegion region = (GameRegion) mainArena.getVariable("region");
        Location min = region.getMin();
        Location max = region.getMax();
        ChunkUtils.loadChunks(min, max);
        p.sendTitle(GameUtils.createColorString("&aLoading game..."), GameUtils.createColorString("&7This could take a moment."), 10, 100, 10);
        Bukkit.getScheduler().runTaskAsynchronously(CVElvenWorkshop.getInstance(), o -> {
            pasteMap(mainArena, arena);
            Bukkit.getScheduler().runTask(CVElvenWorkshop.getInstance(), e -> {
                p.sendTitle("", "", 0, 0, 0);
                arena.getQueue().join(p, "elvenworkshopinstance");
                playersJoining.remove(p);
                ChunkUtils.unloadChunks(min, max);
            });
        });
    }

    public void endGame(Integer slot) {
        Arena arena = gamesList.get(slot).getArena();
        Bukkit.getScheduler().runTaskAsynchronously(CVElvenWorkshop.getInstance(), o -> {
                clearMap(arena);
            });
        gamesList.remove(slot);

    }

    public void pasteMap(Arena parentArena, Arena instanceArena) {
        GameRegion gameRegion = (GameRegion) parentArena.getVariable("region");
        Location pasteLocation = (Location) instanceArena.getVariable("paste-location");
        BlockVector3 min = BukkitAdapter.adapt(gameRegion.getMin()).toVector().toBlockPoint();
        BlockVector3 max = BukkitAdapter.adapt(gameRegion.getMax()).toVector().toBlockPoint();
        BlockVector3 pasteMin = BukkitAdapter.adapt(pasteLocation).toVector().toBlockPoint();
        CuboidRegion region = new CuboidRegion(min, max);
        BlockArrayClipboard clipboard = new BlockArrayClipboard(region);
        try (EditSession session = WorldEdit.getInstance().newEditSession(new BukkitWorld(gameRegion.getMax().getWorld()))) {
            ForwardExtentCopy forwardExtentCopy = new ForwardExtentCopy(session, region, clipboard, region.getMinimumPoint());
            forwardExtentCopy.setCopyingBiomes(true);
            forwardExtentCopy.setCopyingEntities(true);
            forwardExtentCopy.setRemovingEntities(false);
            Operations.complete(forwardExtentCopy);
            Operation operation = new ClipboardHolder(clipboard).createPaste(session).to(pasteMin).copyEntities(true).copyBiomes(true).build();
            Operations.complete(operation);
        } catch (WorldEditException e) {
            throw new RuntimeException(e);
        }
    }

    public void clearMap(Arena arena) {
        GameRegion gameRegion = (GameRegion) arena.getVariable("region");
        BlockVector3 min = BukkitAdapter.adapt(gameRegion.getMin()).toVector().toBlockPoint();
        BlockVector3 max = BukkitAdapter.adapt(gameRegion.getMax()).toVector().toBlockPoint();
        CuboidRegion region = new CuboidRegion(min, max);
        BlockArrayClipboard clipboard = new BlockArrayClipboard(region);
        try (EditSession session = WorldEdit.getInstance().newEditSession(new BukkitWorld(gameRegion.getMax().getWorld()))) {
            BlockType blockType = BlockTypes.AIR;
            session.setBlocks(region, (Pattern) blockType);
        } catch (WorldEditException e) {
            throw new RuntimeException(e);
        }
    }

    public void addPlayer(Player p, ElvenWorkshop instance) {
        instance.getArena().getQueue().join(p, "elvenworkshopinstance");
    }
}
