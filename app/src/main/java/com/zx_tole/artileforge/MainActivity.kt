package com.zx_tole.artileforge

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zx_tole.artileforge.export.SpriteSheetExporter
import com.zx_tole.artileforge.export.TileMapSerializer
import com.zx_tole.artileforge.tile.TileData
import com.zx_tole.artileforge.tile.TileGenerator
import com.zx_tole.artileforge.tile.TileRenderer
import com.zx_tole.artileforge.tile.TileType
import com.zx_tole.artileforge.ui.TileLayerControls
import com.zx_tole.artileforge.ui.TilePalette
import com.zx_tole.artileforge.ui.theme.ARTileForgeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ARTileForgeTheme {
                Surface {
                    MainContent()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainContent(modifier: Modifier = Modifier) {
    val context = LocalContext.current

    val initialTiles = mutableMapOf<Int, TileData>().apply {
        val startTile = TileGenerator.generateStartTile()
        put(packInt(0, 0), startTile)

        val neighbors = TileGenerator.generateNeighbors(0, 0)
        neighbors.forEach { tile ->
            put(packInt(tile.x, tile.y), tile)
        }
    }

    var tiles by remember { mutableStateOf(initialTiles) }
    var selectedTileType by remember { mutableStateOf(TileType.Plains) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tile Map Editor") },
                modifier = Modifier.background(MaterialTheme.colorScheme.primary)
            )
        },
        bottomBar = {
            Column {
                TilePalette(
                    selectedType = selectedTileType,
                    onTypeSelected = { selectedTileType = it },
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(8.dp)
                )
                TileLayerControls(
                    onClear = {
                        Toast.makeText(context, "Map cleared", Toast.LENGTH_SHORT).show()
                        tiles = mutableMapOf()
                    },
                    onExport = {
                        val success = exportTileMap(context, tiles.values.toList())
                        if (success) {
                            Toast.makeText(context, "${tiles.size} tiles exported", Toast.LENGTH_SHORT).show()
                        }
                    },
                    tilesCount = tiles.size,
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.background)
                        .padding(8.dp)
                        .padding(bottom = 24.dp)
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF5F5F5)),
            contentAlignment = Alignment.TopCenter
        ) {
            // Render tiles in a grid
            val tileMap = tiles.values.associateBy { Pair(it.x, it.y) }
            val bounds = TileGenerator.calculateBounds(tileMap)

            if (bounds.width > 0 && bounds.height > 0) {
                Column(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    for (y in bounds.minY..bounds.maxY) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            for (x in bounds.minX..bounds.maxX) {
                                val tile = tileMap[Pair(x, y)]
                                if (tile != null) {
                                    TileRenderer(
                                        tileType = tile.type,
                                        rotation = tile.rotation,
                                        modifier = Modifier
                                            .size(48.dp)
                                            .border(3.dp, if (tile.type == selectedTileType) Color(0xFF00FF00) else Color.Transparent),
                                        onClick = {
                                            val newTile = tile.copy(type = selectedTileType)
                                            tiles[packInt(tile.x, tile.y)] = newTile
                                        }
                                    )
                                } else {
                                    // Empty cell - add new tile when clicked
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .background(Color(0xFFE0E0E0))
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .clickable {
                                                    val newTile = TileData(
                                                        type = selectedTileType,
                                                        x = x,
                                                        y = y
                                                    )
                                                    tiles[packInt(newTile.x, newTile.y)] = newTile
                                                }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                Text(
                    text = "Tap empty cells to place tiles",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
        }
    }
}

/**
 * Export tile map to JSON and PNG
 * @return true if export succeeded
 */
private fun exportTileMap(context: android.content.Context, tiles: List<TileData>): Boolean {
    val exportsDir = context.getExternalFilesDir(null)?.apply { mkdirs() } ?: run {
        Toast.makeText(context, "Export failed", Toast.LENGTH_SHORT).show()
        return false
    }

    // Export JSON
    try {
        val jsonSerializer = TileMapSerializer()
        val json = jsonSerializer.serialize(tiles)
        val jsonFile = java.io.File(exportsDir, "tilemap_${System.currentTimeMillis()}.json")
        jsonFile.writeText(json)
    } catch (e: Exception) {
        Toast.makeText(context, "JSON export failed: ${e.message}", Toast.LENGTH_SHORT).show()
        return false
    }

    // Export PNG
    try {
        val spriteExporter = SpriteSheetExporter()
        val pngFile = java.io.File(exportsDir, "tilesprite_${System.currentTimeMillis()}.png")
        spriteExporter.export(tiles, pngFile.absolutePath)
    } catch (e: Exception) {
        Toast.makeText(context, "PNG export failed: ${e.message}", Toast.LENGTH_SHORT).show()
        return false
    }

    return true
}

/**
 * Utility to pack x,y coordinates into Int for Map key
 */
private fun packInt(x: Int, y: Int): Int {
    return (x shl 16) or (y and 0xFFFF)
}

/**
 * Preview for development
 */
@Preview(showBackground = true)
@Composable
fun MainContentPreview() {
    ARTileForgeTheme {
        MainContent()
    }
}
