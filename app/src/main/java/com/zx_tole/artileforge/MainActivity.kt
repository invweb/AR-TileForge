package com.zx_tole.artileforge

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.core.content.ContextCompat
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

    private lateinit var cameraPermissionLauncher: androidx.activity.result.ActivityResultLauncher<String>

    @SuppressLint("RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Remove enableEdgeToEdge() to let Scaffold handle layout properly

        // Register permission launcher
        cameraPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            // Permission granted callback
        }

        // Check and request camera permission
        val hasPermission = ContextCompat.checkSelfPermission(
            this, Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasPermission) {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }

        setContent {
            ARTileForgeTheme {
                Surface {
                    MainContent(hasPermission)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainContent(hasPermission: Boolean, modifier: Modifier = Modifier) {
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
    var tilesCount by remember { mutableStateOf(initialTiles.size) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AR Tile Map") },
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
                        tiles = mutableMapOf()
                        tilesCount = 0
                    },
                    onExport = {
                        exportTileMap(context, tiles.values.toList())
                        tilesCount = tiles.size
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
            contentAlignment = Alignment.Center
        ) {
            // Render tiles in a grid
            val tileMap = tiles.values.associateBy { Pair(it.x, it.y) }
            val bounds = TileGenerator.calculateBounds(tileMap)
            
                    if (bounds.width > 0 && bounds.height > 0) {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(bounds.width),
                            horizontalArrangement = Arrangement.spacedBy(2.dp),
                            verticalArrangement = Arrangement.spacedBy(2.dp),
                            modifier = Modifier.padding(horizontal = 8.dp)
                        ) {
                            for (y in bounds.minY..bounds.maxY) {
                                items(bounds.width) { x ->
                                    val tile = tileMap[Pair(bounds.minX + x, y)]
                                    if (tile != null) {
                                        TileRenderer(
                                            tileType = tile.type,
                                            modifier = Modifier
                                                .size(48.dp)
                                                .clickable {
                                                    println("Clicking tile at (${tile.x}, ${tile.y}), changing to ${selectedTileType.name}")
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
                                                .clickable {
                                                    val newTile = TileData(
                                                        type = selectedTileType,
                                                        x = bounds.minX + x,
                                                        y = y
                                                    )
                                                    println("Placing tile at (${newTile.x}, ${newTile.y}) type=${newTile.type.name}")
                                                    tiles[packInt(newTile.x, newTile.y)] = newTile
                                                }
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                Text(
                    text = "No tiles placed yet",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

/**
 * Export tile map to JSON and PNG
 */
private fun exportTileMap(context: android.content.Context, tiles: List<TileData>) {
    // Export JSON
    val jsonSerializer = TileMapSerializer()
    val json = jsonSerializer.serialize(tiles)
    
    val exportsDir = context.getExternalFilesDir(null)?.apply { mkdirs() } ?: return
    val jsonFile = java.io.File(exportsDir, "tilemap_${System.currentTimeMillis()}.json")
    jsonFile.writeText(json)
    
    // Export PNG
    val spriteExporter = SpriteSheetExporter()
    val pngFile = java.io.File(exportsDir, "tilesprite_${System.currentTimeMillis()}.png")
    spriteExporter.export(tiles, pngFile.absolutePath)
    
    // Show toast (in real app, use proper notification)
    println("Exported to: ${jsonFile.absolutePath}, ${pngFile.absolutePath}")
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
        MainContent(hasPermission = true)
    }
}
