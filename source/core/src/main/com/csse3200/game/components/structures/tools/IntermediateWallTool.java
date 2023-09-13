package com.csse3200.game.components.structures.tools;

import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.utils.ObjectMap;
import com.csse3200.game.components.structures.CostComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.PlaceableEntity;
import com.csse3200.game.entities.buildables.Wall;
import com.csse3200.game.entities.buildables.WallType;
import com.csse3200.game.entities.factories.BuildablesFactory;
import com.csse3200.game.services.ServiceLocator;

public class IntermediateWallTool extends PlacementTool {
    public IntermediateWallTool(ObjectMap<String, Integer> cost) {
        super(cost);
    }

    @Override
    public boolean interact(Entity player, GridPoint2 position) {
        if (super.interact(player, position)) {
            return true;
        }

        var existingStructure = ServiceLocator.getStructurePlacementService().getStructureAt(position);

        if (!(existingStructure instanceof Wall)) {
            return false;
        }

        if (!hasEnoughResources()) {
            return false;
        }

        PlaceableEntity newStructure = createEntity(player);
        newStructure.addComponent(new CostComponent(cost));

        ServiceLocator.getStructurePlacementService().replaceStructureAt(newStructure, position, false, false);

        return true;
    }

    @Override
    public PlaceableEntity createEntity(Entity player) {
        return BuildablesFactory.createWall(WallType.intermediate, player);
    }
}
