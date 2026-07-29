package com.zx_tole.artileforge

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.automirrored.filled.RotateRight
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.zx_tole.artileforge.export.SpriteSheetExporter
import com.zx_tole.artileforge.export.TileMapSerializer
import com.zx_tole.artileforge.tile.TileData
import com.zx_tole.artileforge.tile.TileGenerator
import com.zx_tole.artileforge.tile.TileRenderer
import com.zx_tole.artileforge.tile.TileType
import com.zx_tole.artileforge.ui.RuleTogglePanel
import com.zx_tole.artileforge.ui.TilePalette
import com.zx_tole.artileforge.ui.theme.ARTileForgeTheme
import kotlin.math.roundToInt

// ==================== Undo/Redo Manager ====================

class UndoRedoManager {
    private val history = mutableListOf<List<TileData>>()
    private var index = 0
    var rulesConfig: TileGenerator.RulesConfig = TileGenerator.defaultRules
        set

    init { history.add(emptyList()); index = 0 }

    val current: List<TileData> get() = if (index in history.indices) history[index] else emptyList()
    val canUndo get() = index > 0
    val canRedo get() = index < history.size - 1

    fun push(newState: List<TileData>) {
        while (history.size > index + 1) history.removeAt(history.size - 1)
        history.add(newState.toList())
        index = history.size - 1
    }

    fun undo(): List<TileData>? { if (canUndo) { index--; return current }; return null }
    fun redo(): List<TileData>? { if (canRedo) { index++; return current }; return null }
    fun clear() { history.clear(); history.add(emptyList()); index = 0 }
}

// ==================== MainActivity ====================

class MainActivity : ComponentActivity() {
    private val undoRedo = UndoRedoManager()
    private val filePicker = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            try {
                val json = contentResolver.openInputStream(uri)?.use { it.readBytes().decodeToString() }
                if (json != null) {
                    val loadedTiles = TileMapSerializer().deserialize(json)
                    undoRedo.clear()
                    undoRedo.push(loadedTiles)
                    undoRedo.rulesConfig = TileGenerator.defaultRules
                    Toast.makeText(this, "Loaded ${loadedTiles.size} tiles", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this, "Failed to load: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private val fileSaver = registerForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri: Uri? ->
        if (uri != null) {
            try {
                val json = TileMapSerializer().serialize(undoRedo.current)
                contentResolver.openOutputStream(uri)?.use { it.write(json.toByteArray()) }
                Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this, "Failed to save: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        undoRedo.push(generateInitialTiles())

        setContent {
            ARTileForgeTheme {
                Surface {
                    MainContent(
                        tiles = undoRedo.current,
                        onTilesChanged = { undoRedo.push(it) },
                        onUndo = { undoRedo.undo(); Unit },
                        onRedo = { undoRedo.redo(); Unit },
                        canUndo = undoRedo.canUndo,
                        canRedo = undoRedo.canRedo,
                        onClear = { undoRedo.clear(); undoRedo.push(emptyList()) },
                        onExport = { exportTiles(this, it) },
                        onSave = { fileSaver.launch("tilemap_${System.currentTimeMillis()}.json") },
                        onLoad = { filePicker.launch("application/json") },
                        rulesConfig = undoRedo.rulesConfig,
                        onRulesConfigChanged = { undoRedo.rulesConfig = it }
                    )
                }
            }
        }
    }

    private fun generateInitialTiles(): List<TileData> {
        val tiles = mutableListOf(TileGenerator.generateStartTile())
        tiles.addAll(TileGenerator.generateNeighbors(0, 0, tiles, undoRedo.rulesConfig))
        return tiles
    }
}

// ==================== Main UI ====================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainContent(
    tiles: List<TileData>,
    onTilesChanged: (List<TileData>) -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    canUndo: Boolean,
    canRedo: Boolean,
    onClear: () -> Unit,
    onExport: (List<TileData>) -> Unit,
    onSave: () -> Unit,
    onLoad: () -> Unit,
    rulesConfig: TileGenerator.RulesConfig,
    onRulesConfigChanged: (TileGenerator.RulesConfig) -> Unit
) {
    val context = LocalContext.current
    var selectedType by remember { mutableStateOf(TileType.Plains) }
    var rotationMode by remember { mutableStateOf(false) }
    var panX by remember { mutableFloatStateOf(0f) }
    var panY by remember { mutableFloatStateOf(0f) }
    var scale by remember { mutableFloatStateOf(1f) }

    val transformState = rememberTransformableState { zoomChange, panZoomChange, _ ->
        scale = (scale * zoomChange).coerceIn(0.5f, 3f)
        panX += panZoomChange.x
        panY += panZoomChange.y
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tile Map Editor") },
                navigationIcon = {
                    IconButton(onClick = onUndo, enabled = canUndo) {
                        Icon(Icons.AutoMirrored.Default.ArrowBack, "Undo")
                    }
                },
                actions = {
                    IconButton(onClick = onRedo, enabled = canRedo) {
                        Icon(Icons.AutoMirrored.Default.ArrowForward, "Redo")
                    }
                    IconButton(onClick = { rotationMode = !rotationMode }) {
                        Icon(
                            Icons.AutoMirrored.Default.RotateRight,
                            if (rotationMode) "Rotate ON" else "Rotate"
                        )
                    }
                    IconButton(onClick = onSave) {
                        Icon(Icons.Default.Save, "Save")
                    }
                    IconButton(onClick = onLoad) {
                        Icon(Icons.Default.Download, "Load")
                    }
                    IconButton(onClick = { onExport(tiles) }) {
                        Icon(Icons.Default.Share, "Export")
                    }
                }
            )
        },
        bottomBar = {
            Column {
                RuleTogglePanel(
                    rulesConfig = rulesConfig,
                    onRulesChanged = onRulesConfigChanged
                )
                TilePalette(selectedType = selectedType, onTypeSelected = { selectedType = it })
                Row(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(8.dp)
                        .padding(bottom = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "${tiles.size} tiles", style = MaterialTheme.typography.bodyMedium)
                    IconButton(onClick = onClear) {
                        Icon(Icons.Default.Delete, "Clear")
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF5F5F5)),
            contentAlignment = Alignment.Center
        ) {
            if (tiles.isNotEmpty()) {
                val tileMap = tiles.associateBy { Pair(it.x, it.y) }
                val bounds = TileGenerator.calculateBounds(tileMap)
                val xMin = bounds.minX
                val yMin = bounds.minY
                val xMax = bounds.maxX
                val yMax = bounds.maxY

                Box(
                    modifier = Modifier
                        .offset { IntOffset(panX.roundToInt(), panY.roundToInt()) }
                        .transformable(transformState)
                ) {
                    Column(
                        modifier = Modifier
                            .verticalScroll(rememberScrollState())
                            .fillMaxSize()
                            .padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        for (y in yMin..yMax) {
                            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                for (x in xMin..xMax) {
                                    val tile = tileMap[Pair(x, y)]
                                    if (tile != null) {
                                        TileRenderer(
                                            tileType = tile.type,
                                            rotation = tile.rotation,
                                            modifier = Modifier
                                                .size(48.dp)
                                                .border(
                                                    2.dp,
                                                    if (tile.type == selectedType) Color.Green.copy(alpha = 0.7f) else Color.Transparent
                                                )
                                                .clickable {
                                                    val newTile = if (rotationMode) {
                                                        tile.copy(rotation = (tile.rotation + 90) % 360)
                                                    } else {
                                                        tile.copy(type = selectedType)
                                                    }
                                                    onTilesChanged(tiles.toMutableList().apply {
                                                        val i = indexOf(tile)
                                                        if (i >= 0) this[i] = newTile
                                                    })
                                                },
                                            onClick = {}
                                        )
                                    } else {
                                        val currentMap = tiles.associateBy { Pair(it.x, it.y) }
                                        val canPlace = TileGenerator.canPlaceTile(currentMap, x, y, selectedType, rulesConfig)
                                        val isBlocked = !canPlace

                                        Box(
                                            modifier = Modifier
                                                .size(48.dp)
                                                .background(
                                                    if (isBlocked) Color(0xFFE0A0A0) else Color(0xFFE0E0E0),
                                                    MaterialTheme.shapes.medium
                                                )
                                                .clickable {
                                                    if (canPlace) {
                                                        onTilesChanged(tiles.toMutableList().apply {
                                                            add(TileData(type = selectedType, x = x, y = y))
                                                        })
                                                    } else {
                                                        val msg = when (selectedType) {
                                                            TileType.Forest -> "Forest requires nearby Water!"
                                                            TileType.Mountain -> "Mountain must be adjacent to Mountain!"
                                                            TileType.Hills -> "Hills require adjacent Mountain!"
                                                            else -> null
                                                        }
                                                        if (msg != null) {
                                                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                                        }
                                                    }
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
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

// ==================== Export & Save ====================

private fun exportTiles(context: android.content.Context, tiles: List<TileData>) {
    if (tiles.isEmpty()) {
        Toast.makeText(context, "No tiles to export", Toast.LENGTH_SHORT).show()
        return
    }

    val exportsDir = context.getExternalFilesDir(null)?.apply { mkdirs() } ?: run {
        Toast.makeText(context, "Export failed", Toast.LENGTH_SHORT).show()
        return
    }

    try {
        val json = TileMapSerializer().serialize(tiles)
        val jsonFile = java.io.File(exportsDir, "tilemap_${System.currentTimeMillis()}.json")
        jsonFile.writeText(json)
    } catch (e: Exception) {
        Toast.makeText(context, "JSON export failed", Toast.LENGTH_SHORT).show()
        return
    }

    try {
        val pngFile = java.io.File(exportsDir, "tilesprite_${System.currentTimeMillis()}.png")
        SpriteSheetExporter().export(tiles, pngFile.absolutePath)
    } catch (e: Exception) {
        Toast.makeText(context, "PNG export failed", Toast.LENGTH_SHORT).show()
        return
    }

    Toast.makeText(context, "Map exported (${tiles.size} tiles)", Toast.LENGTH_SHORT).show()
}

// ==================== Preview ====================

@Preview(showBackground = true)
@Composable
fun MainContentPreview() {
    ARTileForgeTheme {
        MainContent(
            tiles = emptyList(),
            onTilesChanged = {},
            onUndo = {},
            onRedo = {},
            canUndo = false,
            canRedo = false,
            onClear = {},
            onExport = {},
            onSave = {},
            onLoad = {},
            rulesConfig = TileGenerator.defaultRules,
            onRulesConfigChanged = {}
        )
    }
}
