package com.xayah.databackup.feature.restore

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.xayah.databackup.R
import com.xayah.databackup.data.restore.RestoreCategory
import com.xayah.databackup.data.restore.RestoreSessionState
import com.xayah.databackup.data.rustic.RusticRestoreInventory
import com.xayah.databackup.database.entity.ContactDeserialized
import com.xayah.databackup.feature.RestoreAppsRoute
import com.xayah.databackup.feature.RestoreCallLogsRoute
import com.xayah.databackup.feature.RestoreContactsRoute
import com.xayah.databackup.feature.RestoreMessagesRoute
import com.xayah.databackup.feature.RestoreNetworksRoute
import com.xayah.databackup.ui.component.InlineNotice
import com.xayah.databackup.ui.component.Preference
import com.xayah.databackup.ui.component.PreferenceGroup
import com.xayah.databackup.ui.component.SectionHeader
import com.xayah.databackup.ui.component.SmallCheckActionButton
import com.xayah.databackup.ui.component.rememberFadingEdgeState
import com.xayah.databackup.ui.component.shimmer
import com.xayah.databackup.ui.component.surfaceTopAppBarColors
import com.xayah.databackup.ui.component.verticalFadingEdges
import com.xayah.databackup.util.Navigator
import com.xayah.databackup.util.PathHelper
import com.xayah.databackup.util.TimeHelper
import com.xayah.databackup.util.formatToStorageSize
import com.xayah.databackup.util.navigateSafely

@Composable
fun RestoreSetupScreen(
    navigator: Navigator,
    viewModel: RestoreSetupViewModel,
    onBack: () -> Unit,
    onRetry: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    RestoreSetupContent(
        state = state, onBack = onBack, onRetry = onRetry,
        onSelectCategory = viewModel::selectCategory,
        onOpenCategory = { category ->
            navigator.navigateSafely(
                when (category) {
                    RestoreCategory.Apps -> RestoreAppsRoute
                    RestoreCategory.Networks -> RestoreNetworksRoute
                    RestoreCategory.Contacts -> RestoreContactsRoute
                    RestoreCategory.CallLogs -> RestoreCallLogsRoute
                    RestoreCategory.Messages -> RestoreMessagesRoute
                }
            )
        },
    )
}

@Composable
internal fun RestoreSetupContent(
    state: RestoreSessionState,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onSelectCategory: (RestoreCategory, Boolean) -> Unit,
    onOpenCategory: (RestoreCategory) -> Unit,
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
    val gridState = rememberLazyGridState()
    val fadingEdgeState = rememberFadingEdgeState(gridState, label = "restoreSetup")
    val layoutDirection = LocalLayoutDirection.current
    val selectedCount = state.selected.size
    val totalCount = state.inventory?.totalCount ?: 0
    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = {
                    Column {
                        Text(
                            stringResource(R.string.restore),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        if (!state.failed) {
                            Text(
                                text = stringResource(R.string.items_selected, selectedCount, totalCount),
                                modifier = Modifier.shimmer(state.loading),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(ImageVector.vectorResource(R.drawable.ic_arrow_left), stringResource(R.string.back))
                    }
                },
                colors = TopAppBarDefaults.surfaceTopAppBarColors(),
                scrollBehavior = scrollBehavior,
            )
        },
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Adaptive(156.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(top = padding.calculateTopPadding())
                .consumeWindowInsets(padding)
                .verticalFadingEdges(fadingEdgeState),
            state = gridState,
            contentPadding = PaddingValues(
                start = padding.calculateStartPadding(layoutDirection) + 16.dp,
                top = 16.dp,
                end = padding.calculateEndPadding(layoutDirection) + 16.dp,
                bottom = padding.calculateBottomPadding() + 16.dp,
            ),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Column {
                    AnimatedVisibility(visible = state.failed && !state.loading) {
                        InlineNotice(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            text = stringResource(R.string.restore_snapshot_load_failed),
                            icon = ImageVector.vectorResource(R.drawable.ic_circle_x),
                            action = {
                                TextButton(
                                    onClick = onRetry,
                                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                                ) {
                                    Text(stringResource(R.string.retry))
                                }
                            },
                        )
                    }
                    SnapshotInfo(state)
                }
            }
            item(span = { GridItemSpan(maxLineSpan) }) {
                SectionHeader(title = stringResource(R.string.target), color = MaterialTheme.colorScheme.primary)
            }
            RestoreCategory.entries.forEach { target ->
                item(key = target.name) {
                    val entries = state.inventory?.ids(target).orEmpty()
                    val selected = entries.count { it in state.selected }
                    SmallCheckActionButton(
                        modifier = Modifier.fillMaxWidth(),
                        checked = selected > 0,
                        icon = ImageVector.vectorResource(target.iconRes),
                        title = stringResource(target.titleRes),
                        titleShimmer = state.loading,
                        subtitle = stringResource(R.string.items_selected, selected, entries.size),
                        subtitleShimmer = state.loading,
                        onCheckedChange = if (state.loading || state.failed || entries.isEmpty()) null else { checked ->
                            onSelectCategory(target, checked)
                        },
                        onClick = { if (!state.loading && !state.failed) onOpenCategory(target) },
                    )
                }
                if (target == RestoreCategory.Apps) {
                    item(key = "files") {
                        SmallCheckActionButton(
                            modifier = Modifier.fillMaxWidth(),
                            checked = false,
                            icon = ImageVector.vectorResource(R.drawable.ic_folder),
                            title = stringResource(R.string.files),
                            titleShimmer = state.loading,
                            subtitle = stringResource(R.string.items_selected, 0, 0),
                            subtitleShimmer = state.loading,
                            onCheckedChange = {},
                            onClick = {},
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SnapshotInfo(state: RestoreSessionState) {
    val config = state.config
    val snapshot = state.snapshot
    PreferenceGroup {
        Preference(
            icon = ImageVector.vectorResource(R.drawable.ic_database_backup),
            title = config?.displayName ?: stringResource(R.string.unnamed),
            subtitle = stringResource(R.string.rustic),
            subtitleShimmer = state.loading,
        )
        Preference(
            icon = ImageVector.vectorResource(R.drawable.ic_map_pin),
            title = stringResource(R.string.backup_dir),
            subtitle = config?.let { PathHelper.getChildPath(it.path).ifEmpty { it.path } } ?: stringResource(R.string.unknown),
            subtitleIcon = ImageVector.vectorResource(R.drawable.ic_folder),
            subtitleShimmer = state.loading,
        )
        Preference(
            icon = ImageVector.vectorResource(R.drawable.ic_archive_restore),
            title = stringResource(R.string.snapshot),
            subtitle = snapshot?.let {
                if (it.createdAt > 0) "${TimeHelper.formatTimestampInShort(it.createdAt)} · ${it.id.take(8)}" else it.id.take(8)
            } ?: stringResource(R.string.unknown),
            subtitleShimmer = state.loading,
        )
        Preference(
            icon = ImageVector.vectorResource(R.drawable.ic_database),
            title = stringResource(R.string.storage),
            subtitle = snapshot?.summary?.totalBytesProcessed?.formatToStorageSize ?: stringResource(R.string.unknown),
            subtitleShimmer = state.loading,
        )
    }
}

private val RestoreCategory.titleRes: Int
    get() = when (this) {
        RestoreCategory.Apps -> R.string.apps
        RestoreCategory.Networks -> R.string.networks
        RestoreCategory.Contacts -> R.string.contacts
        RestoreCategory.CallLogs -> R.string.call_logs
        RestoreCategory.Messages -> R.string.messages
    }

private val RestoreCategory.iconRes: Int
    get() = when (this) {
        RestoreCategory.Apps -> R.drawable.ic_layout_grid
        RestoreCategory.Networks -> R.drawable.ic_wifi
        RestoreCategory.Contacts -> R.drawable.ic_user_round
        RestoreCategory.CallLogs -> R.drawable.ic_phone
        RestoreCategory.Messages -> R.drawable.ic_message_circle
    }

@Preview(name = "Phone", widthDp = 400, heightDp = 890)
@Preview(name = "Tablet", widthDp = 840, heightDp = 900)
@Composable
private fun RestoreSetupPreview() {
    val inventory = RusticRestoreInventory(
        contacts = (0..1).associate { "contact:$it" to ContactDeserialized(it.toLong(), mapOf("display_name" to "Contact $it"), emptyList(), false) },
    )
    MaterialTheme {
        RestoreSetupContent(
            state = RestoreSessionState(loading = false, inventory = inventory),
            onBack = {}, onRetry = {}, onSelectCategory = { _, _ -> }, onOpenCategory = {},
        )
    }
}

@Preview(name = "Loading", widthDp = 400, heightDp = 890)
@Composable
private fun RestoreSetupLoadingPreview() {
    MaterialTheme {
        RestoreSetupContent(
            state = RestoreSessionState(),
            onBack = {}, onRetry = {}, onSelectCategory = { _, _ -> }, onOpenCategory = {},
        )
    }
}
