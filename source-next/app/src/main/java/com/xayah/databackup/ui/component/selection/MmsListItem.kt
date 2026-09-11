package com.xayah.databackup.ui.component.selection

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.xayah.databackup.R
import com.xayah.databackup.database.entity.MmsDeserialized

@Composable
fun MmsListItem(
    modifier: Modifier,
    mms: MmsDeserialized,
    onCheckedChange: (Boolean) -> Unit,
) {
    val bodyTextStyle = MaterialTheme.typography.bodySmall
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        shape = RoundedCornerShape(16.dp),
        onClick = {
            onCheckedChange(mms.selected.not())
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(modifier = Modifier.size(36.dp), shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Icon(
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.primary,
                        imageVector = ImageVector.vectorResource(R.drawable.ic_user_round),
                        contentDescription = null
                    )
                }
            }

            Column(
                modifier = Modifier
                    .padding(start = 16.dp)
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = mms.address,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
                val inlineContent = remember {
                    mapOf(
                        mms.iconMod to InlineTextContent(
                            Placeholder(
                                width = bodyTextStyle.fontSize,
                                height = bodyTextStyle.fontSize,
                                placeholderVerticalAlign = PlaceholderVerticalAlign.Center
                            )
                        ) {
                            Icon(
                                modifier = Modifier.fillMaxSize(),
                                tint = MaterialTheme.colorScheme.secondary,
                                imageVector = ImageVector.vectorResource(R.drawable.ic_paperclip),
                                contentDescription = null
                            )
                        }
                    )
                }
                if (mms.body.isNotEmpty()) {
                    Text(
                        inlineContent = inlineContent,
                        text = mms.body,
                        style = bodyTextStyle,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Checkbox(
                checked = mms.selected,
                onCheckedChange = {
                    onCheckedChange(it)
                }
            )
        }
    }
}
