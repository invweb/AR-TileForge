package com.zx_tole.artileforge.tile

import kotlin.math.abs

/**
 * Tile placement rules for procedural map generation.
 * 
 * Rules can be enabled/disabled based on desired gameplay style.
 * Currently supports "simple mode" where any combination is allowed.
 */
object TileGenerator {
    
    /**
     * Configuration for placement rules
     */
    data class RulesConfig(
        val waterCannotBeOnEdge: Boolean = false,
        val forestRequiresNearbyWater: Boolean = false,
        val mountainsFormChains: Boolean = false,
        val allowAnyCombination: Boolean = true
    )
    
    /**
     * Default configuration: simple mode, any combination allowed
     */
    val defaultRules = RulesConfig(allowAnyCombination = true)
    
    /**
     * Check if a tile can be placed at the given position with the specified type.
     * 
     * @param tiles The current map of placed tiles
     * @param x The x-coordinate to place the tile
     * @param y The y-coordinate to place the tile
     * @param type The type of tile to place
     * @param rules The placement rules to apply
     * @return true if the tile can be placed, false otherwise
     */
    fun canPlaceTile(
        tiles: Map<Pair<Int, Int>, TileData>,
        x: Int,
        y: Int,
        type: TileType,
        rules: RulesConfig = defaultRules
    ): Boolean {
        if (rules.allowAnyCombination) {
            return true
        }
        
        val neighbors = getNeighbors(tiles, x, y)
        
        // Rule: Water cannot be on the edge of the map
        if (rules.waterCannotBeOnEdge && type == TileType.Water) {
            if (neighbors.isEmpty()) {
                return false
            }
        }
        
        // Rule: Forest requires nearby water
        if (rules.forestRequiresNearbyWater && type == TileType.Forest) {
            val hasNearbyWater = neighbors.values.any { it.type == TileType.Water }
            if (!hasNearbyWater) {
                return false
            }
        }
        
        // Rule: Mountains must form chains (at least one mountain neighbor)
        if (rules.mountainsFormChains && type == TileType.Mountain) {
            val hasMountainNeighbor = neighbors.values.any { it.type == TileType.Mountain }
            if (!hasMountainNeighbor && neighbors.isNotEmpty()) {
                return false
            }
        }
        
        return true
    }
    
    /**
     * Generate a default starting tile
     */
    fun generateStartTile(): TileData {
        return TileData(
            type = TileType.Plains,
            x = 0,
            y = 0
        )
    }
    
    /**
     * Generate neighboring tiles around a center position
     * 
     * @param centerX The x-coordinate of the center
     * @param centerY The y-coordinate of the center
     * @param rules Placement rules
     * @param count Number of neighbors to generate (default: 4)
     * @return List of generated tiles
     */
    fun generateNeighbors(
        centerX: Int,
        centerY: Int,
        rules: RulesConfig = defaultRules
    ): List<TileData> {
        val directions = listOf(
            Pair(1, 0),   // Right
            Pair(-1, 0),  // Left
            Pair(0, 1),   // Up
            Pair(0, -1)   // Down
        )
        
        return directions.map { pair ->
            val (dx, dy) = pair
            val type = getRandomTileType(rules)
            TileData(
                type = type,
                x = centerX + dx,
                y = centerY + dy
            )
        }
    }
    
    /**
     * Get a random tile type based on rules
     */
    private fun getRandomTileType(rules: RulesConfig): TileType {
        if (rules.allowAnyCombination) {
            return TileType.entries.random()
        }
        
        // Weighted random based on rule constraints
        val availableTypes = TileType.entries.filter { type ->
            // Filter based on current rules
            when (type) {
                TileType.Water -> !rules.waterCannotBeOnEdge || true
                TileType.Forest -> !rules.forestRequiresNearbyWater || true
                TileType.Mountain -> !rules.mountainsFormChains || true
                else -> true
            }
        }
        
        return availableTypes.random()
    }
    
    /**
     * Get all tiles adjacent to a position
     */
    private fun getNeighbors(
        tiles: Map<Pair<Int, Int>, TileData>,
        x: Int,
        y: Int
    ): Map<Pair<Int, Int>, TileData> {
        val directions = listOf(
            Pair(1, 0),   // Right
            Pair(-1, 0),  // Left
            Pair(0, 1),   // Up
            Pair(0, -1)   // Down
        )
        
        return directions.mapNotNull { pair ->
            val (dx, dy) = pair
            val key = Pair(x + dx, y + dy)
            tiles[key]?.let { key to it }
        }.toMap()
    }
    
    /**
     * Calculate the bounding box of the current tile map
     */
    fun calculateBounds(tiles: Map<Pair<Int, Int>, TileData>): TileBounds {
        if (tiles.isEmpty()) {
            return TileBounds(0, 0, 0, 0)
        }
        
        val xCoords = tiles.keys.map { it.first }
        val yCoords = tiles.keys.map { it.second }
        
        val minX = xCoords.minOrNull() ?: 0
        val maxX = xCoords.maxOrNull() ?: 0
        val minY = yCoords.minOrNull() ?: 0
        val maxY = yCoords.maxOrNull() ?: 0
        
        return TileBounds(minX, maxX, minY, maxY)
    }
    
    /**
     * Bounds of the tile map
     */
    data class TileBounds(
        val minX: Int,
        val maxX: Int,
        val minY: Int,
        val maxY: Int
    ) {
        val width get() = maxX - minX + 1
        val height get() = maxY - minY + 1
    }
}
