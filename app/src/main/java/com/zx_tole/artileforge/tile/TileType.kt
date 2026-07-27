package com.zx_tole.artileforge.tile

/**
 * Tile types for the AR tile map.
 * Each type has a unique color and placement behavior.
 */
enum class TileType(
    val displayName: String,
    val color: String,
    val lightColor: String,
    val borderAdjustment: Float
) {
    Plains(
        displayName = "Plains",
        color = "#A8E6CF",
        lightColor = "#BDF0DB",
        borderAdjustment = 0.02f
    ),
    Hills(
        displayName = "Hills",
        color = "#D5E1D6",
        lightColor = "#DFECE0",
        borderAdjustment = 0.03f
    ),
    Water(
        displayName = "Water",
        color = "#89CFF0",
        lightColor = "#A8DAF5",
        borderAdjustment = 0.02f
    ),
    Forest(
        displayName = "Forest",
        color = "#567d46",
        lightColor = "#6A9358",
        borderAdjustment = 0.04f
    ),
    Mountain(
        displayName = "Mountain",
        color = "#8B7355",
        lightColor = "#A08566",
        borderAdjustment = 0.05f
    ),
    Wasteland(
        displayName = "Wasteland",
        color = "#E8D4B8",
        lightColor = "#F0DCCA",
        borderAdjustment = 0.02f
    );

    companion object {
        fun default(): TileType = Plains
    }
}
