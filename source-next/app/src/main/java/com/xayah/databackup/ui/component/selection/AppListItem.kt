package com.xayah.databackup.ui.component.selection

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.appcompat.content.res.AppCompatResources
import androidx.compose.animation.graphics.res.animatedVectorResource
import androidx.compose.animation.graphics.res.rememberAnimatedVectorPainter
import androidx.compose.animation.graphics.vector.AnimatedImageVector
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TriStateCheckbox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.xayah.databackup.R
import com.xayah.databackup.database.entity.App
import com.xayah.databackup.database.entity.Option
import com.xayah.databackup.ui.component.FadeVisibility
import com.xayah.databackup.ui.component.SelectableChip
import com.xayah.databackup.util.LaunchedEffect
import com.xayah.databackup.util.formatToStorageSize
import kotlinx.coroutines.Dispatchers

@Composable
fun AppListItem(
    modifier: Modifier,
    context: Context,
    app: App,
    availableOptions: Option = Option(),
    selectionState: ToggleableState = app.toggleableState,
    onSelectAll: () -> Unit,
    onSelectApk: (Boolean) -> Unit,
    onSelectInternalData: (Boolean) -> Unit,
    onSelectExternalData: (Boolean) -> Unit,
    onSelectAdditionalData: (Boolean) -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        onClick = { onSelectAll() },
    ) {
        var expanded by remember { mutableStateOf(false) }

        Column(modifier = Modifier.padding(vertical = 16.dp)) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceContainerHighest),
                    contentAlignment = Alignment.Center
                ) {
                    var icon: Drawable? by remember { mutableStateOf(null) }
                    LaunchedEffect(context = Dispatchers.IO, app.pkgUserKey) {
                        icon = runCatching { context.packageManager.getApplicationIcon(app.packageName) }.getOrNull()
                        if (icon == null) {
                            icon = AppCompatResources.getDrawable(context, android.R.drawable.sym_def_app_icon)
                        }
                    }
                    AsyncImage(
                        modifier = Modifier.size(32.dp),
                        model = ImageRequest.Builder(context)
                            .data(icon)
                            .crossfade(true)
                            .build(),
                        contentDescription = null
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = app.info.label,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = app.packageName,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    val storage by remember(app.selectedBytes, app.totalBytes) {
                        mutableStateOf("${app.selectedBytes.formatToStorageSize} / ${app.totalBytes.formatToStorageSize}")
                    }
                    FadeVisibility(visible = app.totalBytes != 0L) {
                        Text(
                            text = storage,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                val animatedCheckIcon = rememberAnimatedVectorPainter(
                    animatedImageVector = AnimatedImageVector.animatedVectorResource(R.drawable.ic_animated_chevron_right_to_down),
                    atEnd = expanded
                )
                IconButton(onClick = { expanded = expanded.not() }) {
                    Icon(
                        painter = animatedCheckIcon,
                        contentDescription = if (expanded) stringResource(R.string.collapsed) else stringResource(R.string.expand)
                    )
                }

                TriStateCheckbox(
                    state = selectionState,
                    onClick = { onSelectAll() }
                )
            }
            FadeVisibility(expanded) {
                Row(
                    modifier = Modifier
                        .horizontalScroll(rememberScrollState())
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Spacer(modifier = Modifier.width(6.dp))

                    SelectableChip(
                        enabled = availableOptions.apk,
                        selected = app.option.apk,
                        icon = AnimatedImageVector.animatedVectorResource(R.drawable.ic_animated_resource_package),
                        text = stringResource(R.string.apk),
                        onCheckedChange = { onSelectApk(it.not()) },
                    )

                    SelectableChip(
                        enabled = availableOptions.internalData,
                        selected = app.option.internalData,
                        icon = AnimatedImageVector.animatedVectorResource(R.drawable.ic_animated_user),
                        text = stringResource(R.string.internal_data),
                        onCheckedChange = { onSelectInternalData(it.not()) },
                    )

                    SelectableChip(
                        enabled = availableOptions.externalData,
                        selected = app.option.externalData,
                        icon = AnimatedImageVector.animatedVectorResource(R.drawable.ic_animated_database),
                        text = stringResource(R.string.external_data),
                        onCheckedChange = { onSelectExternalData(it.not()) },
                    )

                    SelectableChip(
                        enabled = availableOptions.additionalData,
                        selected = app.option.additionalData,
                        icon = AnimatedImageVector.animatedVectorResource(R.drawable.ic_animated_gamepad_2),
                        text = stringResource(R.string.additional_data),
                        onCheckedChange = { onSelectAdditionalData(it.not()) },
                    )

                    Spacer(modifier = Modifier.width(6.dp))
                }
            }
        }
    }
}
