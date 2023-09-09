package com.csse3200.game.areas;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.GdxGame;
import com.csse3200.game.areas.MapConfig.MapConfig;
import com.csse3200.game.areas.terrain.TerrainFactory;
import com.csse3200.game.components.gamearea.GameAreaDisplay;
import com.csse3200.game.components.resources.Resource;
import com.csse3200.game.components.resources.ResourceDisplay;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.enemies.EnemyBehaviour;
import com.csse3200.game.entities.enemies.EnemyType;
import com.csse3200.game.entities.factories.*;
import com.csse3200.game.files.FileLoader;
import com.csse3200.game.files.UserSettings;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.services.TerrainService;
import com.csse3200.game.utils.math.GridPoint2Utils;
import com.csse3200.game.utils.math.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * A Base Game Area for any level.
 * Details of map can be defined in a config file to be passed to the constructor
 */
public class MapGameArea extends GameArea{

    private final MapConfig mapConfig;
    private static final Logger logger = LoggerFactory.getLogger(EarthGameArea.class);
    private final TerrainFactory terrainFactory;
    private final GdxGame game;
    private Entity playerEntity;

    public MapGameArea(String configPath, TerrainFactory terrainFactory, GdxGame game) {
        //TODO: Check if this causes an error from diff run locations
        mapConfig = FileLoader.readClass(MapConfig.class, configPath, FileLoader.Location.INTERNAL);
        this.game = game;
        this.terrainFactory = terrainFactory;
    }

    /**
     * Create the game area
     */
    @Override
    public void create() {
        loadAssets();
        displayUI();

        registerEntityPlacementService();
        registerStructurePlacementService();

        spawnTerrain();
        spawnEnvironment();
        spawnPowerups();
        spawnExtractors();
        spawnShip();
        playerEntity = spawnPlayer();
        spawnCompanion(playerEntity);

        spawnEnemies();
        spawnBoss();
        //TODO: Check if needed
        //spawnAsteroids();
        spawnBotanist();

        playMusic();
    }

    //TODO: is this needed?
    public Entity getPlayer() {
        return this.playerEntity;
    }

    /**
     * Loads all assets listed in the config file
     */
    private void loadAssets() {
        logger.debug("Loading assets");
        ResourceService resourceService = ServiceLocator.getResourceService();
        if (mapConfig.texturePaths != null)
            resourceService.loadTextures(mapConfig.texturePaths);
        if (mapConfig.textureAtlasPaths != null)
            resourceService.loadTextureAtlases(mapConfig.textureAtlasPaths);
        if (mapConfig.soundPaths != null)
            resourceService.loadSounds(mapConfig.soundPaths);
        if (mapConfig.backgroundMusicPath != null)
            resourceService.loadMusic(new String[] {mapConfig.backgroundMusicPath});

        while (!resourceService.loadForMillis(10)) {
            // This could be upgraded to a loading screen
            logger.info("Loading... {}%", resourceService.getProgress());
        }
    }

    /**
     * Creates the UI with a planet name as described in config file
     */
    private void displayUI() {
        Entity ui = new Entity();
        //Ensure non-null
        mapConfig.mapName = mapConfig.mapName == null ? "" : mapConfig.mapName;
        ui.addComponent(new GameAreaDisplay(mapConfig.mapName));
        spawnEntity(ui);
    }

    /**
     * Spawns the game terrain with wallSize determined by the config file
     */
    private void spawnTerrain() {
        // Background terrain
        terrain = terrainFactory.createTerrain(mapConfig.terrainPath);
        spawnEntity(new Entity().addComponent(terrain));

        // Terrain walls
        float tileSize = terrain.getTileSize();
        GridPoint2 tileBounds = terrain.getMapBounds(0);
        Vector2 worldBounds = new Vector2(tileBounds.x * tileSize, tileBounds.y * tileSize);

        // Left
        spawnEntityAt(
                ObstacleFactory.createWall(ObstacleFactory.WALL_SIZE, worldBounds.y), GridPoint2Utils.ZERO, false, false);
        // Right
        spawnEntityAt(
                ObstacleFactory.createWall(ObstacleFactory.WALL_SIZE, worldBounds.y),
                new GridPoint2(tileBounds.x, 0),
                false,
                false);
        // Top
        spawnEntityAt(
                ObstacleFactory.createWall(worldBounds.x, ObstacleFactory.WALL_SIZE),
                new GridPoint2(0, tileBounds.y),
                false,
                false);
        // Bottom
        spawnEntityAt(
                ObstacleFactory.createWall(worldBounds.x, ObstacleFactory.WALL_SIZE), GridPoint2Utils.ZERO, false, false);
        ServiceLocator.registerTerrainService(new TerrainService(terrain));
    }

    /**
     * Spawns the game environment
     */
    private void spawnEnvironment() {
        TiledMapTileLayer collisionLayer = (TiledMapTileLayer) terrain.getMap().getLayers().get("Tree Base");
        Entity environment;
        for (int y = 0; y < collisionLayer.getHeight(); y++) {
            for (int x = 0; x < collisionLayer.getWidth(); x++) {
                TiledMapTileLayer.Cell cell = collisionLayer.getCell(x, collisionLayer.getHeight() - 1 - y);
                if (cell != null) {
                    MapObjects objects = cell.getTile().getObjects();
                    GridPoint2 tilePosition = new GridPoint2(x, collisionLayer.getHeight() - 1 - y);
                    if (objects.getCount() >= 1) {
                        RectangleMapObject object = (RectangleMapObject) objects.get(0);
                        Rectangle collisionBox = object.getRectangle();
                        float collisionX = 0.5f-collisionBox.x / 16;
                        float collisionY = 0.5f-collisionBox.y / 16;
                        float collisionWidth = collisionBox.width / 32;
                        float collisionHeight = collisionBox.height / 32;
                        environment = ObstacleFactory.createEnvironment(collisionWidth, collisionHeight, collisionX, collisionY);
                    }
                    else {
                        environment = ObstacleFactory.createEnvironment();
                    }
                    spawnEntityAt(environment, tilePosition, false, false);
                }
            }
        }
    }

    /**
     * Spawns powerups in the map at the positions as outlined by the config file
     */
    private void spawnPowerups() {
        if (mapConfig.healthPowerups != null) {
            for (GridPoint2 pos: mapConfig.healthPowerups) {
                Entity healthPowerup = PowerupFactory.createHealthPowerup();
                spawnEntityAt(healthPowerup, pos, true, false);
            }
        }

        if (mapConfig.speedPowerups != null) {
            for (GridPoint2 pos: mapConfig.speedPowerups) {
                Entity speedPowerup = PowerupFactory.createSpeedPowerup();
                spawnEntityAt(speedPowerup, pos, true, false);
            }
        }
    }

    /**
     * Helper method to spawn all the extractors of a given resource type
     * @param resource - Type of extractor to create
     * @param positions - List of positions to place the extractors at
     */
    private void spawnResourceExtractors(Resource resource, List<GridPoint2> positions, long production) {
        if (positions != null) {
            for (GridPoint2 pos: positions) {
                Entity extractor = StructureFactory.createExtractor(mapConfig.extractorStartHealth, resource, production, 1);
                spawnEntityAt(extractor, pos, true, false);
            }
        }
    }

    /**
     * Spawns all the extractors for each resource type as defined in the config file
     * Also spawns the resource display for each resource
     */
    private void spawnExtractors() {
        //Spawn Extractors
        spawnResourceExtractors(Resource.Solstite, mapConfig.solstitePositions, mapConfig.solstiteProduction);
        spawnResourceExtractors(Resource.Durasteel, mapConfig.durasteelPositions, mapConfig.durasteelProduction);
        spawnResourceExtractors(Resource.Nebulite, mapConfig.nebulitePositions, mapConfig.nebuliteProduction);

        int scale = 5;
        int steps = 64;
        int maxResource = 1000;

        //Spawn Display
        ResourceDisplay resourceDisplayComponent = new ResourceDisplay(scale, steps, maxResource)
                .withResource(Resource.Durasteel)
                .withResource(Resource.Solstite)
                .withResource(Resource.Nebulite);
        Entity resourceDisplay = new Entity().addComponent(resourceDisplayComponent);
        spawnEntity(resourceDisplay);
    }

    /**
     * Spawns the ship at the position given by the config file
     */
    private void spawnShip() {
        if (mapConfig.shipPosition != null) {
            Entity ship = StructureFactory.createShip(game, mapConfig.winConditions);
            spawnEntityAt(ship, mapConfig.shipPosition, false, false);
        }
    }

    /**
     * Spawns the player at the position given by the config file.
     * If that is null, then spawns at the centre of the map
     * @return The player entity created
     */
    private Entity spawnPlayer() {
        Entity newPlayer = PlayerFactory.createPlayer();
        if (mapConfig.playerPosition != null) {
            spawnEntityAt(newPlayer, mapConfig.playerPosition, true, true);
        } else {
            //If no position specified spawn in middle of map.
            GridPoint2 pos = new GridPoint2(terrain.getMapBounds(0).x/2,terrain.getMapBounds(0).y/2);
            spawnEntityAt(newPlayer, pos, true, true);
        }
        return newPlayer;
    }

    /**
     * Spawns the companion at the position given by the config file
     * @param playerEntity - player that will be accompanied
     */
    private void spawnCompanion(Entity playerEntity) {
        //Could spawn companion next to player if no position is specified.
        if (mapConfig.companionPosition != null) {
            Entity newCompanion = CompanionFactory.createCompanion(playerEntity);
            spawnEntityAt(newCompanion, mapConfig.companionPosition, true, true);
        }
    }

    /**
     * Spawns all the enemies detailed in the Game Area.
     */
    private void spawnEnemies() {
        GridPoint2 minPos = new GridPoint2(0, 0);
        GridPoint2 maxPos = terrain.getMapBounds(0).sub(2, 2);
        //TODO: Stop these being random?

        // Spawning enemies based on set number of each type
        for (int i = 0; i < mapConfig.numMeleePTE; i++) {
            GridPoint2 randomPos1 = RandomUtils.random(minPos, maxPos);
            Entity meleePTE = EnemyFactory.createEnemy(EnemyType.Melee, EnemyBehaviour.PTE);
            spawnEntityAt(meleePTE, randomPos1, true, true);
        }

        for (int i = 0; i < mapConfig.numMeleeDTE; i++) {
            GridPoint2 randomPos2 = RandomUtils.random(minPos, maxPos);
            Entity meleeDTE = EnemyFactory.createEnemy(EnemyType.Melee, EnemyBehaviour.DTE);
            spawnEntityAt(meleeDTE, randomPos2, true, true);
        }

        for (int i = 0; i < mapConfig.numRangePTE; i++) {
            GridPoint2 randomPos3 = RandomUtils.random(minPos, maxPos);
            Entity rangePTE = EnemyFactory.createEnemy(EnemyType.Ranged, EnemyBehaviour.PTE);
            spawnEntityAt(rangePTE, randomPos3, true, true);
        }
    }

    /**
     * Spawns the boss for the Game Area's map.
     */
    private void spawnBoss() {
        if (mapConfig.bossPosition != null) {
            GridPoint2 pos = mapConfig.bossPosition;
            Entity boss = EnemyFactory.createEnemy(EnemyType.BossMelee, EnemyBehaviour.PTE);
            spawnEntityAt(boss, pos, true, true);
            //TODO: Implement this?
            //boss.addComponent(new DialogComponent(dialogueBox));
        }
    }

    /**
     * Spawns the botanist NPC at the position given in the config file
     */
    private void spawnBotanist() {
        GridPoint2 pos = mapConfig.botanistPosition;
        Entity botanist = NPCFactory.createBotanist();
        spawnEntityAt(botanist, pos, false, false);
        //TODO: Implement this?
        //ship.addComponent(new DialogComponent(dialogueBox)); Adding dialogue component after entity creation is not supported
    }

    /**
     * Plays the game music loaded from the config file
     */
    private void playMusic() {
        UserSettings.Settings settings = UserSettings.get();

        Music music = ServiceLocator.getResourceService().getAsset(mapConfig.backgroundMusicPath, Music.class);
        music.setLooping(true);
        music.setVolume(settings.musicVolume);
        music.play();
    }

    @Override
    public void dispose() {
        super.dispose();
        ServiceLocator.getResourceService().getAsset(mapConfig.backgroundMusicPath, Music.class).stop();
        this.unloadAssets();
    }

    /**
     * Unloads all assets from config file
     */
    private void unloadAssets() {
        logger.debug("Unloading assets");
        ResourceService resourceService = ServiceLocator.getResourceService();
        if (mapConfig.texturePaths != null)
            resourceService.unloadAssets(mapConfig.texturePaths);
        if (mapConfig.textureAtlasPaths != null)
            resourceService.unloadAssets(mapConfig.textureAtlasPaths);
        if (mapConfig.soundPaths != null)
            resourceService.unloadAssets(mapConfig.soundPaths);
        if (mapConfig.backgroundMusicPath != null)
            resourceService.unloadAssets(new String[] {mapConfig.backgroundMusicPath});
    }
}
