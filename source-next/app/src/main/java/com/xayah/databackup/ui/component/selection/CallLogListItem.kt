package com.xayah.databackup.ui.component.selection

import android.provider.CallLog
import androidx.compose.foundation.basicMarquee
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.xayah.databackup.R
import com.xayah.databackup.database.entity.CallLogDeserialized

@Composable
fun CallLogListItem(
    modifier: Modifier,
    callLog: CallLogDeserialized,
    onCheckedChange: (Boolean) -> Unit,
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        shape = RoundedCornerShape(16.dp),
        onClick = {
            onCheckedChange(callLog.selected.not())
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
                tint = when (callLog.type) {
                    CallLog.Calls.INCOMING_TYPE -> MaterialTheme.colorScheme.primary
                    CallLog.Calls.OUTGOING_TYPE -> MaterialTheme.colorScheme.secondary
                    else -> MaterialTheme.colorScheme.error
                },
                imageVector = when (callLog.type) {
                    CallLog.Calls.INCOMING_TYPE -> ImageVector.vectorResource(R.drawable.ic_phone_incoming)
                    CallLog.Calls.OUTGOING_TYPE -> ImageVector.vectorResource(R.drawable.ic_phone_outgoing)
                    else -> ImageVector.vectorResource(R.drawable.ic_phone_missed)
                },
                contentDescription = null
            )

            Column(
                modifier = Modifier
                    .padding(start = 16.dp)
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = callLog.number,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    modifier = Modifier.basicMarquee(),
                    text = callLog.description,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Checkbox(
                checked = callLog.selected,
                onCheckedChange = {
                    onCheckedChange(it)
                }
            )
        }
    }
}
