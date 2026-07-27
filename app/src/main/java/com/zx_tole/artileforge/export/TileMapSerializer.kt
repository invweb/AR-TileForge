package com.zx_tole.artileforge.export

import com.zx_tole.artileforge.tile.TileData
import com.zx_tole.artileforge.tile.TileGenerator
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
}
