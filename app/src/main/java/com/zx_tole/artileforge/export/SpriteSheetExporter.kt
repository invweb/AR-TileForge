package com.zx_tole.artileforge.export

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import com.zx_tole.artileforge.tile.TileData
import com.zx_tole.artileforge.tile.TileType
import java.io.File
import java.io.FileOutputStream

/**
 * Exports tile map to PNG sprite sheet format.
 */
class SpriteSheetExporter {
    
    private val tileSize = 64
    private val padding = 4
    private val cols = 3
    
    /**
     * Export tiles to PNG sprite sheet
     */
    fun export(tiles: List<TileData>, filePath: String): File {
        val file = File(filePath)
        file.parentFile?.mkdirs()
        
        val uniqueTypes = tiles.map { it.type }.distinct()
        
        val spriteWidth = tileSize
        val spriteHeight = tileSize
        
        val sheetWidth = (spriteWidth + padding) * cols - padding
        val rows = (uniqueTypes.size + cols - 1) / cols
        val sheetHeight = (spriteHeight + padding) * rows - padding
        
        val bitmap = Bitmap.createBitmap(sheetWidth, sheetHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        
        // Clear background
        canvas.drawColor(Color.WHITE)
        
        uniqueTypes.forEachIndexed { index, type ->
            val col = index % cols
            val row = index / cols
            
            val x = col * (spriteWidth + padding)
            val y = row * (spriteHeight + padding)
            
            // Draw tile background
            val paint = android.graphics.Paint()
            paint.color = Color.parseColor(type.color)
            canvas.drawRect(
                x.toFloat(), y.toFloat(),
                (x + tileSize).toFloat(), (y + tileSize).toFloat(),
                paint
            )
            
            // Draw border
            paint.color = Color.argb(51, 0, 0, 0)
            paint.style = android.graphics.Paint.Style.STROKE
            paint.strokeWidth = 2f
            canvas.drawRect(
                x.toFloat(), y.toFloat(),
                (x + tileSize).toFloat(), (y + tileSize).toFloat(),
                paint
            )
            
            // Draw type text
            paint.color = Color.BLACK
            paint.textSize = 12f
            paint.textAlign = android.graphics.Paint.Align.CENTER
            canvas.drawText(
                type.name,
                (x + tileSize / 2).toFloat(),
                (y + tileSize / 2 + 4).toFloat(),
                paint
            )
        }
        
        // Save to file
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        
        return file
    }
    
    /**
     * Export with tile map coordinates
     */
    fun exportWithMap(
        tiles: List<TileData>,
        spritePath: String,
        mapPath: String
    ): Pair<File, File> {
        val spriteFile = export(tiles, spritePath)
        
        // Generate map file with coordinates
        val mapJson = StringBuilder()
        mapJson.append("{\n")
        mapJson.append("  \"tiles\": [\n")
        
        tiles.forEachIndexed { index, tile ->
            val spriteX = 0
            val spriteY = 0
            mapJson.append("    {\n")
            mapJson.append("      \"type\": \"${tile.type.name}\",\n")
            mapJson.append("      \"x\": ${tile.x},\n")
            mapJson.append("      \"y\": ${tile.y},\n")
            mapJson.append("      \"spriteX\": $spriteX,\n")
            mapJson.append("      \"spriteY\": $spriteY\n")
            mapJson.append("    }${if (index < tiles.size - 1) "," else ""}\n")
        }
        
        mapJson.append("  ]\n")
        mapJson.append("}\n")
        
        val mapFile = File(mapPath)
        mapFile.parentFile?.mkdirs()
        mapFile.writeText(mapJson.toString())
        
        return Pair(spriteFile, mapFile)
    }
}
