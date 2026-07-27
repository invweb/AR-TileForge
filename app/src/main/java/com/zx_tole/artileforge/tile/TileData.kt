package com.zx_tole.artileforge.tile

/**
 * Represents a single tile in the AR tile map.
 * 
 * @property type The type of terrain this tile represents
 * @property x The x-coordinate in the grid (0-based)
 * @property y The y-coordinate in the grid (0-based)
 * @property rotation Rotation in degrees (0, 90, 180, 270)
 */
data class TileData(
    val type: TileType,
    val x: Int,
    val y: Int,
    val rotation: Int = 0
) {
    /**
     * Get neighboring tile coordinates in the specified direction.
     * Directions: 0=Right, 1=Up, 2=Left, 3=Down
     */
    fun getNeighbor(direction: Int): Pair<Int, Int> {
        return when (direction) {
            0 -> Pair(x + 1, y)  // Right
            1 -> Pair(x, y + 1)  // Up
            2 -> Pair(x - 1, y)  // Left
            3 -> Pair(x, y - 1)  // Down
            else -> Pair(x, y)
        }
    }

    /**
     * Create a copy of this tile with a different type
     */
    fun withType(newType: TileType): TileData {
        return copy(type = newType)
    }

    /**
     * Create a copy of this tile with a different rotation
     */
    fun withRotation(newRotation: Int): TileData {
        return copy(rotation = newRotation)
    }
}
