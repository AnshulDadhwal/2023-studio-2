package com.csse3200.game.components.structures.tools;

import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.utils.ObjectMap;
import com.csse3200.game.components.structures.CostComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.PlaceableEntity;
import com.csse3200.game.services.ServiceLocator;

/**
 * An abstract tool which allows the player to place a structure.
 * This class must be inherited and the createEntity method implemented to function.
 */
public abstract class ReplacementTool extends PlacementTool {

    private final boolean mustReplace;

    /**
     * Creates a new tool which allows the placing of structures with the given cost.
     * @param cost - the cost of the entity being placed.
     */
    protected ReplacementTool(ObjectMap<String, Integer> cost) {
        this(cost, false);
    }

    /**
     * Creates a new tool which allows the placing of structures with the given cost.
     * @param cost - the cost of the entity being placed.
     */
    protected ReplacementTool(ObjectMap<String, Integer> cost, boolean mustPlace) {

        super(cost);
        this.mustReplace = mustPlace;
    }

    /**
     * Attempts to place a structure at the specified position.
     * If the position is already occupied or the player has insufficient resources, does nothing.
     *
     * @param player - the player interacting with the tool.
     * @param position - the position to place the structure.
     * @return whether the structure was successfully placed.
     */
    @Override
    public void performInteraction(Entity player, GridPoint2 position) {
        PlaceableEntity newStructure = createStructure(player);
        newStructure.addComponent(new CostComponent(cost));

        // get the left-most and bottom-most position of the structure to replace
        var existingStructure = structurePlacementService.getStructureAt(position);

        if (existingStructure != null) {
            var placePosition = structurePlacementService.getStructurePosition(existingStructure);

            ServiceLocator.getStructurePlacementService().replaceStructureAt(newStructure, placePosition,
                    false, false);
        } else if (!mustReplace) {
            ServiceLocator.getStructurePlacementService().placeStructureAt(newStructure, position,
                    false, false);
        }
    }

    /**
     * Creates the structure to be placed. This must be implemented in subclasses to function.
     *
     * @param player - the player placing the structure.
     * @return the structure to be placed.
     */
    public abstract PlaceableEntity createStructure(Entity player);

    /**
     * Checks whether the structure can be placed at the given position.
     * By default, checks if the grid position is empty.
     *
     * @param position - the position the structure is trying to be placed at.
     * @return whether the structure can be placed at the given position.
     */
    @Override
    public ToolResponse isPositionValid(GridPoint2 position) {
        if (!mustReplace) {
            return ToolResponse.valid();
        }

        // can only place if clicked on a structure
        var existingStructure = structurePlacementService.getStructureAt(position);

        return existingStructure != null ? ToolResponse.valid()
                : new ToolResponse(PlacementValidity.INVALID_POSITION, "No structure to replace");
    }
}