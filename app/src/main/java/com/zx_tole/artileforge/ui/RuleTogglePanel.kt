package com.zx_tole.artileforge.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.zx_tole.artileforge.tile.TileGenerator

/**
 * Rule toggle panel for placement rules.
 * Shows switches for each rule with descriptive labels.
 */
@Composable
fun RuleTogglePanel(
    rulesConfig: TileGenerator.RulesConfig,
    onRulesChanged: (TileGenerator.RulesConfig) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Placement Rules",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            RuleSwitch(
                label = "Forests need Water nearby",
                checked = rulesConfig.forestRequiresNearbyWater,
                onCheckedChange = { onRulesChanged(rulesConfig.copy(forestRequiresNearbyWater = it)) }
            )

            RuleSwitch(
                label = "Mountains must form chains",
                checked = rulesConfig.mountainsFormChains,
                onCheckedChange = { onRulesChanged(rulesConfig.copy(mountainsFormChains = it)) }
            )

            RuleSwitch(
                label = "Hills need Mountain neighbor",
                checked = true,
                onCheckedChange = {}
            )
        }
    }
}

@Composable
private fun RuleSwitch(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.primary,
                checkedTrackColor = MaterialTheme.colorScheme.primaryContainer,
                uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceContainer
            )
        )
    }
}
