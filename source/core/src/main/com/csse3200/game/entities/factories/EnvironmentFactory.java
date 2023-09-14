package com.csse3200.game.entities.factories;

import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Rectangle;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.PlaceableEntity;
import com.csse3200.game.files.FileLoader;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.services.StructurePlacementService;


/**
 * Factory to create all environmental entities on the map
 */
public class EnvironmentFactory {
    /** The size of the tiles used in the creation of the map. */
    private static final int tileSize = 16;

    /** The scale used when importing the map into the game. */
    private static final float scaleSize = 0.5f;

    /** The constant used to improve collision hit boxes. */
    private static final float xyShift = 0.3f;

    /**
     * Creates all entities for a specified layer of the map.
     *
     * @param layer The layer in which all the environmental entities will need to be created.
     */
    public static void createEnvironment(TiledMapTileLayer layer) {
        StructurePlacementService placementService = ServiceLocator.getStructurePlacementService();
        Entity environment;

        for (int y = 0; y < layer.getHeight(); y++) {
            for (int x = 0; x < layer.getWidth(); x++) {
                TiledMapTileLayer.Cell cell = layer.getCell(x, layer.getHeight() - 1 - y);
                if (cell != null) {
                    MapObjects objects = cell.getTile().getObjects();
                    GridPoint2 tilePosition = new GridPoint2(x, layer.getHeight() - 1 - y);
                    if (objects.getCount() >= 1) {
                        RectangleMapObject object = (RectangleMapObject) objects.get(0);
                        Rectangle collisionBox = object.getRectangle();
                        float collisionX = xyShift - collisionBox.x / tileSize;
                        float collisionY = xyShift - collisionBox.y / tileSize;
                        float collisionWidth = scaleSize * (collisionBox.width / tileSize);
                        float collisionHeight = scaleSize * (collisionBox.height / tileSize);
                        environment = ObstacleFactory.createEnvironment(collisionWidth, collisionHeight, collisionX, collisionY);
                    } else {
                        environment = ObstacleFactory.createEnvironment();
                    }
                    //placementService.placeStructureAt(environment, tilePosition, false, false);
                }
            }
        }
    }
}
