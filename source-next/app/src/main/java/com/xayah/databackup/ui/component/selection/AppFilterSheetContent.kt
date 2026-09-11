package com.xayah.databackup.ui.component.selection

import androidx.compose.animation.graphics.res.animatedVectorResource
import androidx.compose.animation.graphics.res.rememberAnimatedVectorPainter
import androidx.compose.animation.graphics.vector.AnimatedImageVector
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.xayah.databackup.R
import com.xayah.databackup.ui.component.FilterButton
import com.xayah.databackup.ui.component.filterButtonSecondaryColors
import com.xayah.databackup.util.SortsSequence
import com.xayah.databackup.util.SortsType

data class AppFilterUserOption(
    val id: Int,
    val name: String,
)

@Composable
fun AppFilterSheetContent(
    modifier: Modifier = Modifier,
    users: List<AppFilterUserOption>,
    selectedUserId: Int,
    sortsType: SortsType,
    sortsSequence: SortsSequence,
    selectedFirst: Boolean,
    filtersUserApps: Boolean,
    filtersSystemApps: Boolean,
    onUserClick: (AppFilterUserOption) -> Unit,
    onSequenceClick: () -> Unit,
    onSelectedFirstClick: () -> Unit,
    onSortTypeClick: (SortsType) -> Unit,
    onFilterUserAppsClick: () -> Unit,
    onFilterSystemAppsClick: () -> Unit,
    supportedSortTypes: Set<SortsType> = SortsType.entries.toSet(),
    showAppTypeFilters: Boolean = true,
) {
    val isAscending = sortsSequence == SortsSequence.ASCENDING
    val animatedSequenceIcon = rememberAnimatedVectorPainter(
        animatedImageVector = AnimatedImageVector.animatedVectorResource(R.drawable.ic_animted_arrow_up_down_a_z),
        atEnd = isAscending
    )

    LazyVerticalGrid(
        modifier = modifier,
        columns = GridCells.Adaptive(minSize = 80.dp),
        contentPadding = PaddingValues(start = 24.dp, top = 8.dp, end = 24.dp, bottom = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        item(span = { GridItemSpan(maxLineSpan) }, key = "users_header") {
            Text(
                text = stringResource(R.string.users),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        items(
            items = users,
            key = { "user_${it.id}" },
        ) { user ->
            FilterButton(
                selected = user.id == selectedUserId,
                title = user.name,
                subtitle = "${user.id}",
                icon = ImageVector.vectorResource(R.drawable.ic_book_user),
            ) {
                onUserClick(user)
            }
        }
        item(span = { GridItemSpan(maxLineSpan) }, key = "users_divider") {
            HorizontalDivider()
        }

        item(span = { GridItemSpan(maxLineSpan) }, key = "sorts_header") {
            Text(
                text = stringResource(R.string.sorts),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        item(key = "sort_sequence") {
            FilterButton(
                selected = true,
                title = if (isAscending) stringResource(R.string.ascending) else stringResource(R.string.descending),
                colors = filterButtonSecondaryColors(),
                icon = animatedSequenceIcon,
            ) {
                onSequenceClick()
            }
        }
        item(key = "sort_selected_first") {
            FilterButton(
                selected = selectedFirst,
                title = stringResource(R.string.selected_first),
                colors = filterButtonSecondaryColors(),
                icon = ImageVector.vectorResource(R.drawable.ic_square_check_big),
            ) {
                onSelectedFirstClick()
            }
        }
        item(key = "sort_a2z") {
            FilterButton(
                selected = sortsType == SortsType.A2Z,
                title = stringResource(R.string.a2z),
                icon = ImageVector.vectorResource(R.drawable.ic_book_a),
            ) {
                onSortTypeClick(SortsType.A2Z)
            }
        }
        if (SortsType.DATA_SIZE in supportedSortTypes) {
            item(key = "sort_data_size") {
                FilterButton(
                    selected = sortsType == SortsType.DATA_SIZE,
                    title = stringResource(R.string.data_size),
                    icon = ImageVector.vectorResource(R.drawable.ic_book_text),
                ) {
                    onSortTypeClick(SortsType.DATA_SIZE)
                }
            }
        }
        if (SortsType.INSTALL_TIME in supportedSortTypes) {
            item(key = "sort_install_time") {
                FilterButton(
                    selected = sortsType == SortsType.INSTALL_TIME,
                    title = stringResource(R.string.install_time),
                    icon = ImageVector.vectorResource(R.drawable.ic_book_down),
                ) {
                    onSortTypeClick(SortsType.INSTALL_TIME)
                }
            }
        }
        if (SortsType.UPDATE_TIME in supportedSortTypes) {
            item(key = "sort_update_time") {
                FilterButton(
                    selected = sortsType == SortsType.UPDATE_TIME,
                    title = stringResource(R.string.update_time),
                    icon = ImageVector.vectorResource(R.drawable.ic_book_up),
                ) {
                    onSortTypeClick(SortsType.UPDATE_TIME)
                }
            }
        }
        if (showAppTypeFilters) {
            item(span = { GridItemSpan(maxLineSpan) }, key = "sorts_divider") {
                HorizontalDivider()
            }
            item(span = { GridItemSpan(maxLineSpan) }, key = "filters_header") {
                Text(
                    text = stringResource(R.string.filters),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            item(key = "filter_user_apps") {
                FilterButton(
                    selected = filtersUserApps,
                    title = stringResource(R.string.user_apps),
                    icon = ImageVector.vectorResource(R.drawable.ic_resource_package),
                ) {
                    onFilterUserAppsClick()
                }
            }
            item(key = "filter_system_apps") {
                FilterButton(
                    selected = filtersSystemApps,
                    title = stringResource(R.string.system_apps),
                    icon = ImageVector.vectorResource(R.drawable.ic_package_2),
                ) {
                    onFilterSystemAppsClick()
                }
            }
        }
    }
}
