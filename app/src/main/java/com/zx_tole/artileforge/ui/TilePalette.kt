package com.zx_tole.artileforge.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.zx_tole.artileforge.tile.TileRenderer
import com.zx_tole.artileforge.tile.TileType

/**
 * Palette component for selecting tile types.
 * Shows all available tile types for placement.
 */
@Composable
fun TilePalette(
    selectedType: TileType,
    onTypeSelected: (TileType) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surface)
            .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TileType.entries.forEach { type ->
            TileTypeButton(
                tileType = type,
                isSelected = type == selectedType,
                onClick = { onTypeSelected(type) }
            )
        }
    }
}

/**
 * Individual tile type button
 */
@Composable
fun TileTypeButton(
    tileType: TileType,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(56.dp)
            .background(tileType.toComposeColor())
            .clickable { onClick() }
            .border(2.dp, if (isSelected) Color.White else Color(0x66000000))
    ) {
        Text(
            text = tileType.displayName,
            color = if (isSelected) Color.White else Color(0xE0000000),
            modifier = Modifier.padding(4.dp)
        )
    }
}

/**
 * Extension to convert TileType to Compose Color
 */
fun TileType.toComposeColor(): Color {
    return Color(android.graphics.Color.parseColor(color))
}
