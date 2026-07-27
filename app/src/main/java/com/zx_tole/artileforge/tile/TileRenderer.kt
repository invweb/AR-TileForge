package com.zx_tole.artileforge.tile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Composable that renders a single tile based on its type.
 * Uses minimalist flat design with subtle borders.
 */
@Composable
fun TileRenderer(
    tileType: TileType,
    size: Dp = 48.dp,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val tileColor = Color(android.graphics.Color.parseColor(tileType.color))
    val borderColor = tileType.borderAdjustment
        .let { alpha -> Color(0x33000000.toInt()).copy(alpha = alpha) }
    
    Box(
        modifier = modifier
            .background(tileColor)
            .border(2.dp, borderColor)
            .let { if (onClick != null) it.clickable { onClick() } else it }
    )
}
