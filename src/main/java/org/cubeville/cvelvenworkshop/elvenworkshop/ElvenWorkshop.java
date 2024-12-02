package org.cubeville.cvelvenworkshop.elvenworkshop;

import com.google.common.collect.ArrayListMultimap;
import io.papermc.paper.event.player.PlayerInventorySlotChangeEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.*;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.cubeville.cvelvenworkshop.CVElvenWorkshop;
import org.cubeville.cvelvenworkshop.enums.ElvenWorkshopGameType;
import org.cubeville.cvelvenworkshop.managers.EWMaterialManager;
import org.cubeville.cvelvenworkshop.managers.GiftManager;
import org.cubeville.cvelvenworkshop.models.*;
import org.cubeville.cvelvenworkshop.utils.ChunkUtils;
import org.cubeville.cvelvenworkshop.utils.EWResourceUtils;
import org.cubeville.cvelvenworkshop.utils.EWInventoryUtils;
import org.cubeville.cvelvenworkshop.utils.PersistentDataUtils;
import org.cubeville.cvgames.managers.ArenaManager;
import org.cubeville.cvgames.models.Game;
import org.cubeville.cvgames.models.GameRegion;
import org.cubeville.cvgames.utils.GameUtils;
import com.google.common.collect.Multimap;

import java.util.*;

import static org.bukkit.Bukkit.getServer;

public class ElvenWorkshop extends Game {
    ElvenWorkshopMain parentGame;
    List<Player> teamPlayers = new ArrayList<>();
    Integer slot;
    List<EWMaterialGenerator> materialGenerators = new ArrayList<>();
    Forge forge;
    Wrapper wrapper;
    Influencer influencer;
    UpgradeStation upgradeStation;
    BukkitTask task;
    Integer timeLapsed;
    Integer sinceOrder = 0;
    List<Order> orders = new ArrayList<>();
    Double orderModifier = 0.0;
    Integer snowflakes = 0;
    Integer joy = 0;
    AttributeModifier movementSpeed;
    AttributeModifier workshopMovementSpeed;
    Double bonusValueModifier = 0.0;
    Double bonusTimeModifier = 0.0;
    boolean debugGame = false;
    int gameSpeed = 1;
    ElvenWorkshopGameType gameType = ElvenWorkshopGameType.NORMAL;
    ElvenWorkshopTutorial tutorial;

    public ElvenWorkshop(String id, String arenaName) {
        super(id, arenaName);
    }
    public ElvenWorkshop(String id, String arenaName, ElvenWorkshopMain parentGame, Integer slot) {
        super(id, arenaName);
        this.parentGame = parentGame;
        this.slot = slot;
    }

    @Override
    public void onGameStart(Set<Player> set) {
        movementSpeed = new AttributeModifier(UUID.randomUUID(), "elvenworkshop-movement-speed", 0, AttributeModifier.Operation.ADD_SCALAR, EquipmentSlot.FEET);
        workshopMovementSpeed = new AttributeModifier(UUID.randomUUID(), "elvenworkshop-workshop-movement-speed", 0, AttributeModifier.Operation.ADD_SCALAR, EquipmentSlot.FEET);
        for (Player p : set) {
            state.put(p, new ElvenWorkshopState());
            teamPlayers.add(p);
            p.getInventory().setBoots(getBootItem(p));
            p.getInventory().setItem(8, getQuickChatItem());
            p.teleport((Location) getVariable("spawn"));
            Random rand = new Random();
            Integer value = rand.nextInt(100);
            if (value == 0) {
                getState(p).luckyBoots = true;
            }
        }
        cleanEntities(true);
        teamPlayers.sort(Comparator.comparing((Player o) -> o.getUniqueId().toString()));
        materialGenerators.add(new EWMaterialGenerator(this, EWMaterialManager.getMaterial("diamond"), (Location) getVariable("diamond-generator")));
        materialGenerators.add(new EWMaterialGenerator(this, EWMaterialManager.getMaterial("iron"), (Location) getVariable("iron-generator")));
        materialGenerators.add(new EWMaterialGenerator(this, EWMaterialManager.getMaterial("redstone"), (Location) getVariable("redstone-generator")));
        materialGenerators.add(new EWMaterialGenerator(this, EWMaterialManager.getMaterial("leather"), (Location) getVariable("leather-generator")));
        materialGenerators.add(new EWMaterialGenerator(this, EWMaterialManager.getMaterial("cloth"), (Location) getVariable("cloth-generator")));
        for (EWMaterialGenerator materialGenerator : materialGenerators) {
            getServer().getPluginManager().registerEvents(materialGenerator, CVElvenWorkshop.getInstance());
            materialGenerator.activate();
        }
        forge = new Forge(this, (Location) getVariable("forge-location"));
        wrapper = new Wrapper(this, (Location) getVariable("wrapper-location"));
        influencer = new Influencer(this, (Location) getVariable("influencer-location"));
        upgradeStation = new UpgradeStation(this, (Location) getVariable("upgrade-station-location"));
        getServer().getPluginManager().registerEvents(forge, CVElvenWorkshop.getInstance());
        getServer().getPluginManager().registerEvents(wrapper, CVElvenWorkshop.getInstance());
        getServer().getPluginManager().registerEvents(influencer, CVElvenWorkshop.getInstance());
        getServer().getPluginManager().registerEvents(upgradeStation, CVElvenWorkshop.getInstance());
        forge.activate();
        wrapper.activate();
        influencer.activate();
        upgradeStation.activate();
        if (isTutorial()) {
            tutorial = new ElvenWorkshopTutorial(this);
            tutorial.start();
        }
        timeLapsed = 0;
        activateRepeatingTask();
    }
    
    public Forge getForge() {
        return forge;
    }
    
    public Wrapper getWrapper() {
        return wrapper;
    }
    
    public Influencer getInfluencer() {
        return influencer;
    }
    
    public UpgradeStation getUpgradeStation() {
        return upgradeStation;
    }
    
    private ItemStack getBootItem(Player p) {
        ItemStack boots = new ItemStack(Material.LEATHER_BOOTS);
        List<String> lore = new ArrayList<>();
        if (getState(p).luckyBoots) {
            boots.setType(Material.GOLDEN_BOOTS);
            ItemMeta meta = boots.getItemMeta();
            meta.setDisplayName(GameUtils.createColorString("&#ccaa00Lucky Boots"));
            lore.add(GameUtils.createColorString("&71/100 odds..."));
            lore.add(GameUtils.createColorString("&7Aren't you lucky!"));
            meta.setLore(lore);
            boots.setItemMeta(meta);
        } else {
            LeatherArmorMeta meta = (LeatherArmorMeta) boots.getItemMeta();
            if (Objects.equals(p.getUniqueId().toString(), "50b04546-d30c-4e08-94f9-494933c8c8ff")) {
                meta.setColor(Color.WHITE);
                meta.setDisplayName(GameUtils.createColorString("&fBoots of the Creator"));
                lore.add(GameUtils.createColorString("&7The flame of creation never dies,"));
                lore.add(GameUtils.createColorString("&7for it lives on in those who dream."));
            } else if (Objects.equals(p.getUniqueId().toString(), "0ea72b90-459d-4e44-923f-67676e9ecab8") || Objects.equals(p.getUniqueId().toString(), "5d23569f-ccf2-4d27-a7a9-f19e3b943574") || Objects.equals(p.getUniqueId().toString(), "68818bb8-92bf-44d2-a6f7-d3d60a4ce714")) {
                meta.setColor(Color.GREEN);
                meta.setDisplayName(GameUtils.createColorString("&#008000Builder's Boots"));
                lore.add(GameUtils.createColorString("&7&o2023 Edition"));
                lore.add(GameUtils.createColorString("&7To turn a dream into a reality"));
                lore.add(GameUtils.createColorString("&7is a wonderful thing."));
                lore.add(GameUtils.createColorString("&7Thank you."));
            } else if (Objects.equals(p.getUniqueId().toString(), "361acc52-b5ba-403d-921b-33bb0bd910ac")) {
                meta.setColor(Color.LIME);
                meta.setDisplayName(GameUtils.createColorString("&aBoots of Thanks"));
                lore.add(GameUtils.createColorString("&7&o2023 Edition"));
                lore.add(GameUtils.createColorString("&7It is said that no person is an island."));
                lore.add(GameUtils.createColorString("&7Thank you for your help!"));
            } else {
                meta.setColor(Color.fromRGB(200, 40, 40));
                meta.setDisplayName(GameUtils.createColorString("&#C82828Elven Boots"));
            }
            meta.setLore(lore);
            boots.setItemMeta(meta);
        }
        ItemMeta meta = boots.getItemMeta();
        meta.addItemFlags(ItemFlag.HIDE_DYE, ItemFlag.HIDE_ATTRIBUTES);
        Multimap<Attribute, AttributeModifier> attributes = ArrayListMultimap.create();
        attributes.put(Attribute.GENERIC_MOVEMENT_SPEED, movementSpeed);
        if (((GameRegion) getVariable("workshop-region")).containsPlayer(p)) {
            attributes.put(Attribute.GENERIC_MOVEMENT_SPEED, workshopMovementSpeed);
        }
        meta.setAttributeModifiers(attributes);
        boots.setItemMeta(meta);
        PersistentDataUtils.setPersistentDataString(boots, "elf-boots", "true");
        return boots;
    }
    
    private ItemStack getQuickChatItem() {
        ItemStack item = new ItemStack(Material.SPRUCE_SIGN);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(GameUtils.createColorString("&f\uD83D\uDDE8&6Quick Chat"));
        meta.setLore(List.of(GameUtils.createColorString("&6Right Click &7to use")));
        item.setItemMeta(meta);
        return item;
    }

    private void updateBoots() {
        for (Player p : getPlayers()) {
            ItemStack item = p.getInventory().getBoots();
            if (PersistentDataUtils.getPersistentDataString(item, "elf-boots") == null) {
                EWInventoryUtils.clearTaggedItems(p, "elf-boots", "true");
                p.getInventory().addItem(item);
            }
            p.getInventory().setBoots(getBootItem(p));
        }
    }

    private void checkArmor() {
        for (Player p : getPlayers()) {
            if (p.getInventory().getLeggings() != null) {
                p.getInventory().addItem(p.getInventory().getLeggings());
                p.getInventory().setLeggings(null);
            }
            if (p.getInventory().getChestplate() != null) {
                p.getInventory().addItem(p.getInventory().getChestplate());
                p.getInventory().setChestplate(null);
            }
            if (p.getInventory().getHelmet() != null) {
                p.getInventory().addItem(p.getInventory().getHelmet());
                p.getInventory().setHelmet(null);
            }
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (getPlayers().contains((Player) e.getWhoClicked())) {
            if (e.getSlotType() == InventoryType.SlotType.ARMOR || e.getSlot() == 8) e.setCancelled(true);
        }
    }
    
    @EventHandler
    public void onItemDrop(PlayerDropItemEvent e) {
        if (getPlayers().contains(e.getPlayer())) {
            if (e.getItemDrop().getItemStack().equals(getQuickChatItem())) e.setCancelled(true);
        }
    }
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        if (getPlayers().contains(e.getPlayer())) {
            if (e.getPlayer().getInventory().getHeldItemSlot() == 8 && getState(e.getPlayer()).quickChatMenu == null) {
                QuickChatMenu menu = new QuickChatMenu(this, e.getPlayer());
                menu.activate();
                getState(e.getPlayer()).quickChatMenu = menu;
            }
            if (e.getAction().isRightClick()) e.setUseItemInHand(Event.Result.DENY);
        }
    }

    private void addOrder() {
        if (isTutorial()) {
            return;
        }
        sinceOrder = 0;
        if (orders.size() < 5) {
            Order order = new Order(this);
            orders.add(order);
            order.activate();
        }
    }
    
    public void addSpecialOrder(Gift gift, WrappingColor color) {
        sinceOrder = 0;
        if (orders.size() < 5) {
            Order order = new Order(this);
            order.setWrappedGift(new WrappedGift(gift, color));
            orders.add(order);
            order.activate();
        }
    }

    private void activateRepeatingTask() {
        if (!isTutorial()) {
            sendMessageToArena(GameUtils.createColorString("&b❄&f❄&b❄ &f&lThe Snowstorm Began! &b❄&f❄&b❄\n"
                + "&7The snowstorm has begun! Orders will be fairly infrequent, while falling snowflakes will be very frequent!" +
                "\n&fBase Order Rate: &a60s\n&fSnowfall Rate: &a3s"));
        }
        for (Player player : teamPlayers) {
            player.setPlayerWeather(WeatherType.DOWNFALL);
        }
        task = Bukkit.getScheduler().runTaskTimer(CVElvenWorkshop.getInstance(), () -> {
            timeLapsed += gameSpeed;
            sinceOrder += gameSpeed;
            if (timeLapsed <= 6000) {
                if (sinceOrder >= (1200*(1+orderModifier))) {
                    addOrder();
                }
                if (Math.floorMod(timeLapsed, 60) == 0) {
                    spawnSnowflake();
                }
                if (timeLapsed == 6000 && !isTutorial()) {
                    sendMessageToArena(GameUtils.createColorString("&b❄&f❄&b❄ &f&lThe Snowstorm Ended! &b❄&f❄&b❄\n"
                            + "&7The snowstorm has ended! Orders will now become more frequent, while falling snowflakes will become less frequent!" +
                            "\n&fBase Order Rate: &a30s\n&fSnowfall Rate: &a6s"));
                    for (Player player : teamPlayers) {
                        player.setPlayerWeather(WeatherType.CLEAR);
                    }
                }
            } else if (timeLapsed <= 18000) {
                if (sinceOrder >= (600*(1+orderModifier))) {
                    addOrder();
                }
                if (Math.floorMod(timeLapsed, 120) == 0) {
                    spawnSnowflake();
                }
                if (timeLapsed == 18000 && !isTutorial()) {
                    sendMessageToArena(GameUtils.createColorString("&c\uD83C\uDF81&2\uD83C\uDF81&f\uD83C\uDF81 &c&lChristmas Eve Began! &f\uD83C\uDF81&2\uD83C\uDF81&c\uD83C\uDF81\n"
                            + "&7Orders will now be even more frequent!" +
                            "\n&fBase Order Rate: &a20s\n&fSnowfall Rate: &a6s"));
                }
            } else if (timeLapsed < 24000) {
                if (sinceOrder >= (400*(1+orderModifier))) {
                    addOrder();
                }
                if (Math.floorMod(timeLapsed, 120) == 0) {
                    spawnSnowflake();
                }
            } else {
//                cleanEntities(false);
                if (isTutorial()) {
                    return;
                }
                finishGame();
                return;
            }
            updateBoots();
            if (Math.floorMod(timeLapsed, 20) == 0) {
                displayScoreboard();
                checkArmor();
            }
        }, 1, 1);
    }

    private void spawnSnowflake() {
        Location loc = getRandomSnowfallLocation();
        Item entity = loc.getWorld().spawn(loc, Item.class);
        ItemStack item = new ItemStack(Material.SNOWBALL);
        entity.setItemStack(item);
        entity.setGlowing(true);
        entity.setVelocity(new Vector(0, 0, 0));
        Bukkit.getScheduler().runTaskLater(CVElvenWorkshop.getInstance(), () -> {
            if (!entity.isDead() && isRunningGame) {
                entity.remove();
            }
        }, 30*20);
    }

    @EventHandler
    public void onPlayerPickup(EntityPickupItemEvent e) {
        if (!(e.getEntity() instanceof Player p)) return;
        if (!inGame(p)) return;
        if (e.getItem().getItemStack().getType() != Material.SNOWBALL) return;
        e.setCancelled(true);
        e.getItem().remove();
        addSnowflakes(e.getItem().getItemStack().getAmount());
        p.playSound(p, Sound.ENTITY_ARROW_HIT_PLAYER, 1.0f, 1.5f);
        if (isTutorial() && tutorial.getStage() == 2 && snowflakes >= 5) {
            tutorial.progressTutorial();
        }
    }

    public void onPlayerLeaveGame(Player player) {
        player.resetPlayerWeather();
        if (isRunningGame) {
            for (Order order : orders) {
                order.removePlayer(player);
            }
        } else {
            GameRegion region = (GameRegion) getVariable("region");
            Location min = region.getMin();
            Location max = region.getMax();
            Bukkit.getScheduler().runTaskLater(CVElvenWorkshop.getInstance(), () -> {
                if (getArena().getQueue().getPlayerSet().size() < 1) {
//                    if (!((GameRegion) getVariable("region")).containsPlayer(player)) {
//                        Bukkit.getScheduler().runTaskLater(CVElvenWorkshop.getInstance(), () -> {
//                            cleanEntities(false);
//                        }, 19);
//                    } else {
//                        cleanEntities(false);
//                    }
//                    player.teleport((Location) getVariable("lobby"));
//                    Bukkit.getScheduler().runTaskLater(CVElvenWorkshop.getInstance(), () -> {
//                        if (player.isOnline()) {
//                            player.teleport((Location) getVariable("exit"));
//                        }
//                    }, 20);
                    ChunkUtils.loadChunks(min, max);
                    cleanEntities(false);
                    ChunkUtils.unloadChunks(min, max);
                    clearGame();
                }
            }, 1);
//            Bukkit.getScheduler().runTaskLater(CVElvenWorkshop.getInstance(), o -> {
//                if (getArena().getQueue().getPlayerSet().size() < 1) {
//                    clearGame();
//                }
//            }, 20);
        }
    }

    public void onPlayerJoinGame(Player player) {
        if (isRunningGame) {
            for (Order order : orders) {
                order.getBar().addPlayer(player);
            }
        }
    }

    @Override
    public void onPlayerLeave(Player player) {
        if (getState(player).quickChatMenu != null) {
            getState(player).quickChatMenu.close();
        }
        state.remove(player);
        player.getScoreboard().clearSlot(DisplaySlot.SIDEBAR);
//        if (state.isEmpty()) {
//            if (!((GameRegion) getVariable("region")).containsPlayer(player)) {
//                Bukkit.getScheduler().runTaskLater(CVElvenWorkshop.getInstance(), () -> {
//                    cleanEntities(false);
//                }, 19);
//            } else {
//                cleanEntities(false);
//            }
//            player.teleport((Location) getVariable("lobby"));
//            Bukkit.getScheduler().runTaskLater(CVElvenWorkshop.getInstance(), () -> {
//                if (player.isOnline()) {
//                    player.teleport((Location) getVariable("exit"));
//                }
//            }, 20);
//        }
    }

    public Set<Player> getPlayers() {
        return state.keySet();
    }

    @Override
    public ElvenWorkshopState getState(Player player) {
        return (ElvenWorkshopState) state.get(player);
    }

    public void cleanEntities(Boolean onlyStart) {
        GameRegion region = (GameRegion) getVariable("region");
        Location min = region.getMin();
        Location max = region.getMax();
        Collection<Entity> entities = min.getWorld().getNearbyEntities(new BoundingBox(min.getX(), min.getY(), min.getZ(), max.getX(), max.getY(), max.getZ()));
        for (Entity entity : entities) {
            if (entity instanceof Player) {
            }
            if (onlyStart) {
                if (entity instanceof Item || entity instanceof ItemDisplay || entity instanceof TextDisplay) {
                    entity.remove();
                }
            } else {
                if (!(entity instanceof Player)) {
                    entity.remove();
                }
            }
        }
    }

    @Override
    public void onGameFinish() {
        for (EWMaterialGenerator materialGenerator : materialGenerators) {
            materialGenerator.remove();
        }
        Set<Player> players = new HashSet<>(state.keySet());
        for (Player player : players) {
            if (getState(player).quickChatMenu != null) {
                getState(player).quickChatMenu.close();
            }
            player.getScoreboard().clearSlot(DisplaySlot.SIDEBAR);
            player.resetPlayerWeather();
        }
        if (!debugGame && !isTutorial()) {
            GameUtils.sendMetricToCVStats("elvenworkshop_result", Map.of(
                "arena", arena.getName(),
                "game", getId(),
                "players", getMetricTeam(),
                "player_count", ((Integer) teamPlayers.size()).toString(),
                "score", joy.toString()
            ));
        }
        task.cancel();
        HandlerList.unregisterAll(this);
        forge.remove();
        wrapper.remove();
        influencer.remove();
        upgradeStation.remove();
        if (!isTutorial()) {
            sendMessageToArena(GameUtils.createColorString("&b&lGAME ENDED!"));
        } else {
            sendMessageToArena(GameUtils.createColorString("&a&lTUTORIAL ENDED!"));
        }
        if (debugGame) {
            sendMessageToArena(GameUtils.createColorString("&e⚠ &cDebug Game"));
        }
        List<String> playerStrings = new ArrayList<>();
        for (Player player : teamPlayers) {
            playerStrings.add(player.getName());
        }
        StringJoiner joiner = new StringJoiner(", ");
        playerStrings.forEach(joiner::add);
        if (teamPlayers.size() > 1) {
            sendMessageToArena(GameUtils.createColorString("&7Players: &f" + joiner));
        } else sendMessageToArena(GameUtils.createColorString("&7Player: &f" + joiner));
        sendMessageToArena(GameUtils.createColorString("&7Total Joy: " + EWResourceUtils.getJoyDisplay(joy, true)));
        for (Order order : orders) {
            order.remove();
        }
        GameRegion region = (GameRegion) getVariable("region");
        Location min = region.getMin();
        Location max = region.getMax();
        ChunkUtils.loadChunks(min, max);
        cleanEntities(false);
        ChunkUtils.unloadChunks(min, max);
        if (isTutorial()) {
            tutorial.end();
        }
        clearGame();
    }

    public String getMetricTeam() {
        List<String> playerUUIDs = new ArrayList<>();
        for (Player player : teamPlayers) {
            playerUUIDs.add(player.getUniqueId().toString());
        }
        StringJoiner joiner = new StringJoiner(",");
        playerUUIDs.forEach(joiner::add);
        return joiner.toString();
    }

    public void clearGame() {
        parentGame.endGame(slot);
        parentGame.gamesList.remove(slot);
        ArenaManager.deleteArena(arena.getName());
    }

    public boolean inGame(Player p) {
        return state.get(p) != null && state.get(p) instanceof ElvenWorkshopState;
    }

    public Location getRandomDeliveryLocation() {
        List<Location> locations = (List<Location>) getVariable("delivery-locations");
        for (Order order : orders) {
            locations.remove(order.getLocation());
        }
        Random rand = new Random();
        Integer ret = rand.nextInt(locations.size());
        return locations.get(ret);
    }

    public Location getRandomSnowfallLocation() {
        GameRegion region = (GameRegion) getVariable("snowfall-region");
        Location min = region.getMin();
        Location max = region.getMax();
        Random rand = new Random();
        Integer x = rand.nextInt((max.getBlockX()-min.getBlockX())+1);
        Integer z = rand.nextInt((max.getBlockZ()-min.getBlockZ())+1);
        return new Location(min.getWorld(), (double) x+min.getBlockX(), (double) min.getBlockY(), (double) z+min.getBlockZ());
    }

    public Integer getWeight(Gift gift) {
        Integer weight = gift.getWeight();
        GiftCategory category = gift.getCategory();
        if (influencer.getActiveCategory() != null && influencer.getActiveCategory().equals(category)) {
            weight = (int) Math.round(weight * ((CVElvenWorkshop.getConfigData().getInt("influencer-upgrades." + influencer.getCategoryLevel(category) + ".weight", 0) / 100.0) + 1));
        }
        return weight;
    }

    public Integer getTotalWeight() {
        Integer weight = 0;
        for (Gift gift : getActiveGifts()) {
            weight += getWeight(gift);
        }
        return weight;
    }
    
    public List<Gift> getActiveGifts() {
        List<Gift> gifts = GiftManager.getGifts().values().stream().toList();
        List<Gift> uniqueGifts = influencer.getUniqueGifts();
        List<Gift> finalGifts = new ArrayList<>();
        for (Gift gift : gifts) {
            if (gift.isUnique() && !uniqueGifts.contains(gift)) {
                continue;
            }
            finalGifts.add(gift);
        }
        return finalGifts;
    }

    public Gift getRandomGift() {
        Integer weight = getTotalWeight();
        Random rand = new Random();
        Integer value = rand.nextInt(weight);
        value += 1;
        for (Gift gift : getActiveGifts()) {
            if (value <= getWeight(gift)) {
                return gift;
            }
            else {
                value -= getWeight(gift);
            }
        }
        return null;
    }

    public void claimGift(Order order, Player p) {
        EWInventoryUtils.consumeWrappedGift(p, order.getWrappedGift());
        Integer points = order.getValue() + order.getSpeedBonus();
        addSnowflakes(points);
        addJoy(points);
        order.remove();
        orders.remove(order);
        for (Player player : getPlayers()) {
            player.playSound(player, Sound.ENTITY_PLAYER_LEVELUP, 1f, 0.6f);
        }
        sendMessageToArena(GameUtils.createColorString(order.getWrappedGift().getOrderName() + "&r &fhas been claimed by &e" + p.getName() + "&f!"));
        String bonusText = "";
        if (order.getSpeedBonusTier() == 1) {
            bonusText = "&a(" + order.getSpeedBonus() + " BONUS!) ";
        } else if (order.getSpeedBonusTier() == 2) {
            bonusText = "&e(" + order.getSpeedBonus() + " BONUS!) ";
        }
        sendMessageToArena(GameUtils.createColorString(
            bonusText + EWResourceUtils.getJoyDisplay(points, true) + " " +
            EWResourceUtils.getSnowflakeDisplay(points, true)));
    }

    public Integer getSnowflakes() {
        return snowflakes;
    }

    public void addSnowflakes(Integer snowflakes) {
        this.snowflakes += snowflakes;
    }

    public Integer getJoy() {
        return joy;
    }

    public void addJoy(Integer joy) {
        this.joy += joy;
    }
    
    public void setDebugGame() {
        debugGame = true;
    }
    
    public void setGameSpeed(int gameSpeed) {
        this.gameSpeed = gameSpeed;
    }
    
    public Double getBonusValueModifier() {
        return bonusValueModifier;
    }
    
    public Double getBonusTimeModifier() {
        return bonusTimeModifier;
    }
    
    public void setGameType(ElvenWorkshopGameType gameType) {
        this.gameType = gameType;
    }
    
    public boolean isTutorial() {
        return gameType.equals(ElvenWorkshopGameType.TUTORIAL);
    }
    
    public ElvenWorkshopTutorial getTutorial() {
        return tutorial;
    }
    
    @EventHandler
    public void onInteractEntity(PlayerInteractEntityEvent e) {
        if ((e.getRightClicked() instanceof Player)) {
            if (e.getHand() != EquipmentSlot.HAND) return;
            Player p = e.getPlayer();
            Player oP = (Player) e.getRightClicked();
            if (!inGame(p) || !inGame(oP)) return;
            ItemStack item = p.getInventory().getItemInMainHand();
            if (item.getType() == Material.AIR) return;
            oP.getInventory().addItem(item);
            p.getInventory().setItem(p.getInventory().getHeldItemSlot(), new ItemStack(Material.AIR));
        }
    }

    private void displayScoreboard() {
        List<String> scoreboardLines = new ArrayList<>();
        if (debugGame) {
            scoreboardLines.add(GameUtils.createColorString("&e⚠ &cDebug Game"));
        }
        if (!isTutorial()) {
            long timeRemaining = 24000-timeLapsed;
            Object[] time = new Object[]{(int) timeRemaining / 60 / 20, (timeRemaining % 1200) / 20};
            scoreboardLines.add(GameUtils.createColorString("&fTime Remaining: &c") + String.format("%d:%02d", time));
            if (timeLapsed <= 6000) {
                long orderTimeRemaining = (int) (1200 * (1 + orderModifier)) - sinceOrder;
                Object[] orderTime = new Object[] {(int) orderTimeRemaining / 60 / 20, (orderTimeRemaining % 1200) / 20};
                scoreboardLines.add(GameUtils.createColorString("&fNext Order In: &a") + String.format("%d:%02d", orderTime));
                long eventTimeRemaining = 6000 - timeLapsed;
                Object[] eventTime = new Object[] {(int) eventTimeRemaining / 60 / 20, (eventTimeRemaining % 1200) / 20};
                scoreboardLines.add(GameUtils.createColorString("&fSnowstorm Ends In: &e") + String.format("%d:%02d", eventTime));
            } else if (timeLapsed <= 18000) {
                long orderTimeRemaining = (int) (600 * (1 + orderModifier)) - sinceOrder;
                Object[] orderTime = new Object[] {(int) orderTimeRemaining / 60 / 20, (orderTimeRemaining % 1200) / 20};
                scoreboardLines.add(GameUtils.createColorString("&fNext Order In: &a") + String.format("%d:%02d", orderTime));
                long eventTimeRemaining = 18000 - timeLapsed;
                Object[] eventTime = new Object[] {(int) eventTimeRemaining / 60 / 20, (eventTimeRemaining % 1200) / 20};
                scoreboardLines.add(GameUtils.createColorString("&fChristmas Eve Begins In: &e") + String.format("%d:%02d", eventTime));
            } else {
                long orderTimeRemaining = (int) (400 * (1 + orderModifier)) - sinceOrder;
                Object[] orderTime = new Object[] {(int) orderTimeRemaining / 60 / 20, (orderTimeRemaining % 1200) / 20};
                scoreboardLines.add(GameUtils.createColorString("&fNext Order In: &a") + String.format("%d:%02d", orderTime));
            }
        } else {
            scoreboardLines.add(GameUtils.createColorString("&a❓ &fTutorial"));
        }
        scoreboardLines.add("");
        scoreboardLines.add(GameUtils.createColorString("&fSnowflakes: " + EWResourceUtils.getSnowflakeDisplay(snowflakes, true)));
        scoreboardLines.add(GameUtils.createColorString("&fJoy: " + EWResourceUtils.getJoyDisplay(joy, true)));
        // Scoreboard scoreboard = GameUtils.createScoreboard(arena, GameUtils.createColorString("&c&lElven &f&lWork&2&lshop"), scoreboardLines);
        String title = LegacyComponentSerializer.legacySection().serialize(MiniMessage.miniMessage().deserialize("<color:#ff0000>\uD83D\uDE5A<white>\uD83C\uDF81<color:#ff0000>\uD83D\uDE58 <gradient:#dd2222:#ffffff:#22dd22>ᴇʟᴠᴇɴ ᴡᴏʀᴋsʜᴏᴘ</gradient> <color:#00ff00>\uD83D\uDE5A<white>\uD83C\uDF81<color:#00ff00>\uD83D\uDE58"));
        Scoreboard scoreboard = GameUtils.createScoreboard(arena, title, scoreboardLines);
        sendScoreboardToArena(scoreboard);
    }
    
    public void sendQuickChat(Player player, String message) {
        sendMessageToArena(GameUtils.createColorString("\uD83D\uDDE8&e" + player.getName() + "&f: " + message));
    }

    public void setUpgrade(Upgrade upgrade, int l, ConfigurationSection config) {
        switch (upgrade.getInternalName()) {
            case "movement-speed":
                Integer amount = config.getInt("levels." + l + ".amount");
                movementSpeed = new AttributeModifier(UUID.randomUUID(), "elvenworkshop-movement-speed", amount/100.0, AttributeModifier.Operation.ADD_SCALAR, EquipmentSlot.FEET);
                break;
            case "workshop-movement-speed":
                amount = config.getInt("levels." + l + ".amount");
                workshopMovementSpeed = new AttributeModifier(UUID.randomUUID(), "elvenworkshop-workshop-movement-speed", amount/100.0, AttributeModifier.Operation.ADD_SCALAR, EquipmentSlot.FEET);
                break;
            case "forge-speed":
                amount = config.getInt("levels." + l + ".amount");
                forge.setModifier(amount/100.0);
                break;
            case "wrapper-speed":
                amount = config.getInt("levels." + l + ".amount");
                wrapper.setModifier(amount/100.0);
                break;
            case "same-day-delivery":
                Integer value = config.getInt("levels." + l + ".value");
                Integer time = config.getInt("levels." + l + ".time");
                bonusTimeModifier = time/100.0;
                bonusValueModifier = value/100.0;
                break;
            case "order-speed":
                amount = config.getInt("levels." + l + ".amount");
                orderModifier = amount/100.0;
                break;
        }
    }
}
