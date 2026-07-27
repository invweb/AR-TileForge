package com.zx_tole.artileforge.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.zx_tole.artileforge.tile.TileData

/**
 * Controls for the tile layer.
 * Provides actions like clear, undo, redo, and export.
 */
@Composable
fun TileLayerControls(
    onClear: () -> Unit,
    onExport: () -> Unit,
    tilesCount: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Info panel
        Box {
            Text(
                text = "Tiles: $tilesCount",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
        
        // Action buttons
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onClear,
                modifier = Modifier.padding(end = 8.dp)
            ) {
                Text("Clear")
            }
            
            Button(
                onClick = onExport
            ) {
                Text("Export")
            }
        }
    }
}
