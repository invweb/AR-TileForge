package com.zx_tole.artileforge.export

import com.zx_tole.artileforge.tile.TileData
import com.zx_tole.artileforge.tile.TileGenerator
import com.zx_tole.artileforge.tile.TileType
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/**
 * Serializes tile map data to JSON format.
 */
class TileMapSerializer {
    
    /**
     * Serialize tile list to JSON string
     */
    fun serialize(tiles: List<TileData>): String {
        val tileMap = tiles.associateBy { Pair(it.x, it.y) }
        val bounds = TileGenerator.calculateBounds(tileMap)
        
        val json = JSONObject()
        json.put("version", "1.0")
        json.put("tileSizeCm", 5.0)
        
        val tilesArray = JSONArray()
        tiles.forEach { tile ->
            val tileJson = JSONObject()
            tileJson.put("type", tile.type.name)
            tileJson.put("x", tile.x)
            tileJson.put("y", tile.y)
            tileJson.put("rotation", tile.rotation)
            tilesArray.put(tileJson)
        }
        json.put("tiles", tilesArray)
        
        val boundsJson = JSONObject()
        boundsJson.put("minX", bounds.minX)
        boundsJson.put("maxX", bounds.maxX)
        boundsJson.put("minY", bounds.minY)
        boundsJson.put("maxY", bounds.maxY)
        json.put("bounds", boundsJson)
        
        return json.toString(2)
    }
    
    /**
     * Save serialized tile map to file
     */
    fun saveToFile(tiles: List<TileData>, filePath: String): File {
        val file = File(filePath)
        file.parentFile?.mkdirs()
        file.writeText(serialize(tiles))
        return file
    }

    /**
     * Deserialize JSON string to tile list
     */
    fun deserialize(jsonString: String): List<TileData> {
        val json = JSONObject(jsonString)
        val tilesArray = json.getJSONArray("tiles")
        val tiles = mutableListOf<TileData>()

        for (i in 0 until tilesArray.length()) {
            val tileJson = tilesArray.getJSONObject(i)
            val type = TileType.valueOf(tileJson.getString("type"))
            val x = tileJson.getInt("x")
            val y = tileJson.getInt("y")
            val rotation = tileJson.getInt("rotation")
            tiles.add(TileData(type = type, x = x, y = y, rotation = rotation))
        }

        return tiles
    }

    /**
     * Load tile map from file
     */
    fun loadFromFile(filePath: String): List<TileData> {
        val file = File(filePath)
        if (!file.exists()) {
            throw IllegalArgumentException("File not found: $filePath")
        }
        return deserialize(file.readText())
    }

}
