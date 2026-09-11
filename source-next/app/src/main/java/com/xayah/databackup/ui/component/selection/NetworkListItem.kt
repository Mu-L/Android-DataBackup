package com.xayah.databackup.ui.component.selection

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.xayah.databackup.R
import com.xayah.databackup.database.entity.NetworkUnmarshalled

@Composable
fun NetworkListItem(
    modifier: Modifier,
    context: Context,
    network: NetworkUnmarshalled,
    onCheckedChange: (Boolean) -> Unit,
    showPassword: Boolean,
) {
    val password by remember(showPassword, network.preSharedKey) {
        mutableStateOf(
            if (network.preSharedKey == null) {
                context.getString(R.string.public_network)
            } else if (showPassword) {
                network.preSharedKey.toString()
            } else {
                context.getString(R.string.hidden_password)
            }
        )
    }
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        shape = RoundedCornerShape(16.dp),
        onClick = {
            onCheckedChange(network.selected.not())
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.primary,
                imageVector = if (network.preSharedKey == null) ImageVector.vectorResource(R.drawable.ic_wifi_open)
                else ImageVector.vectorResource(R.drawable.ic_wifi_password),
                contentDescription = "Localized description"
            )
            Column(
                modifier = Modifier
                    .padding(start = 16.dp)
                    .weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = network.ssid,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = password,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Checkbox(
                checked = network.selected,
                onCheckedChange = { onCheckedChange(it) }
            )
        }
    }
}
