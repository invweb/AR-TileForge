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
     * Default configuration: strict rules enabled
     */
    val defaultRules = RulesConfig(
        waterCannotBeOnEdge = false,
        forestRequiresNearbyWater = true,
        mountainsFormChains = true,
        allowAnyCombination = false
    )
    
    /**
     * Check if a tile can be placed at the given position with the specified type.
     * 
     * Rules:
     * - Forest requires at least one adjacent Water tile
     * - Mountains must be adjacent to at least one other Mountain tile
     * - Hills require at least one Mountain neighbor (terrain progression)
     * - Plains and Wasteland are always allowed
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
        
        // Rule: Forest requires nearby water
        if (rules.forestRequiresNearbyWater && type == TileType.Forest) {
            val hasNearbyWater = neighbors.values.any { it.type == TileType.Water }
            if (!hasNearbyWater) {
                println("PLACEMENT RULE: Forest requires nearby Water tile - blocked")
                return false
            }
        }
        
        // Rule: Mountains must form chains (at least one mountain neighbor)
        if (rules.mountainsFormChains && type == TileType.Mountain) {
            val hasMountainNeighbor = neighbors.values.any { it.type == TileType.Mountain }
            if (!hasMountainNeighbor && neighbors.isNotEmpty()) {
                println("PLACEMENT RULE: Mountain requires adjacent Mountain - blocked")
                return false
            }
        }
        
        // Rule: Hills require at least one Mountain neighbor
        if (type == TileType.Hills) {
            val hasMountainNeighbor = neighbors.values.any { it.type == TileType.Mountain }
            if (!hasMountainNeighbor && neighbors.isNotEmpty()) {
                println("PLACEMENT RULE: Hills requires adjacent Mountain - blocked")
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
     * Respects placement rules.
     * 
     * @param centerX The x-coordinate of the center
     * @param centerY The y-coordinate of the center
     * @param existingTiles The existing tiles (for rule checking)
     * @param rules Placement rules
     * @return List of generated tiles
     */
    fun generateNeighbors(
        centerX: Int,
        centerY: Int,
        existingTiles: List<TileData>,
        rules: RulesConfig = defaultRules
    ): List<TileData> {
        val directions = listOf(
            Pair(1, 0),   // Right
            Pair(-1, 0),  // Left
            Pair(0, 1),   // Up
            Pair(0, -1)   // Down
        )
        
        val tileMap = existingTiles.associateBy { Pair(it.x, it.y) }
        val generated = mutableListOf<TileData>()
        
        for (pair in directions) {
            val (dx, dy) = pair
            val nx = centerX + dx
            val ny = centerY + dy
            
            // Try to generate a valid type (up to 5 attempts)
            var placed = false
            for (attempt in 1..5) {
                val candidates = if (rules.allowAnyCombination) {
                    TileType.entries.toList()
                } else {
                    // Weighted: prioritize natural terrain distribution
                    val pool = mutableListOf<TileType>()
                    repeat(4) { pool += TileType.Plains }
                    repeat(3) { pool += TileType.Hills }
                    repeat(3) { pool += TileType.Water }
                    repeat(3) { pool += TileType.Forest }
                    repeat(2) { pool += TileType.Mountain }
                    repeat(1) { pool += TileType.Wasteland }
                    pool
                }
                
                val type = candidates.random()
                if (canPlaceTile(tileMap + (generated.associateBy { Pair(it.x, it.y) }), nx, ny, type, rules)) {
                    generated.add(TileData(type = type, x = nx, y = ny))
                    println("Generated neighbor at ($nx, $ny) = ${type.name}")
                    placed = true
                    break
                }
            }
            
            // Fallback: allow placement even if rules block it (for initial generation)
            if (!placed) {
                val fallbackTypes = when {
                    existingTiles.any { it.type == TileType.Mountain } -> listOf(TileType.Mountain, TileType.Hills, TileType.Forest)
                    existingTiles.any { it.type == TileType.Water } -> listOf(TileType.Forest, TileType.Plains, TileType.Water)
                    else -> listOf(TileType.Plains, TileType.Hills, TileType.Water, TileType.Forest, TileType.Mountain)
                }
                val fallbackType = fallbackTypes.random()
                generated.add(TileData(type = fallbackType, x = nx, y = ny))
                println("Fallback neighbor at ($nx, $ny) = ${fallbackType.name}")
            }
        }
        
        return generated
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
