package com.xayah.databackup.feature.restore.component

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.xayah.databackup.R
import com.xayah.databackup.ui.component.SearchTextField
import com.xayah.databackup.ui.component.rememberFadingEdgeState
import com.xayah.databackup.ui.component.surfaceTopAppBarColors
import com.xayah.databackup.ui.component.verticalFadingEdges

/** Layout shared by restore category pages. It knows nothing about categories, repositories or selection rules. */
@Composable
internal fun <T> RestoreListScaffold(
    title: String,
    items: List<T>,
    itemKey: (T) -> String,
    selectedCount: Int,
    totalCount: Int,
    searchText: String,
    loading: Boolean,
    failed: Boolean,
    onSearchTextChange: (String) -> Unit,
    onBack: () -> Unit,
    onSelectAll: (() -> Unit)? = null,
    itemSpacing: Dp = 16.dp,
    listKey: Any? = null,
    separateSearchScroll: Boolean = false,
    selectedSize: String? = null,
    actions: @Composable RowScope.() -> Unit = {},
    tabs: @Composable () -> Unit = {},
    itemContent: @Composable (Modifier, T) -> Unit,
) {
    var searching by remember { mutableStateOf(false) }
    val normalScroll = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
    val searchScroll = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
    val focus = remember { FocusRequester() }
    val normalListState = rememberLazyListState()
    val searchListState = rememberLazyListState()
    val listState = if (searching && separateSearchScroll) searchListState else normalListState
    val fading = rememberFadingEdgeState(listState, label = "restoreList")
    LaunchedEffect(searching) {
        if (searching) {
            if (separateSearchScroll) searchListState.scrollToItem(0)
            focus.requestFocus()
        }
    }
    fun closeSearch() { searching = false; onSearchTextChange("") }
    BackHandler(enabled = searching) { closeSearch() }
    val toolbarActions: @Composable RowScope.() -> Unit = {
        if (!searching) IconButton(onClick = { searching = true }) {
            Icon(ImageVector.vectorResource(R.drawable.ic_search), stringResource(R.string.search))
        }
        actions()
        onSelectAll?.let { selectAll ->
            IconButton(onClick = selectAll) {
                Icon(ImageVector.vectorResource(R.drawable.ic_list_checks), stringResource(R.string.select_all))
            }
        }
    }
    Scaffold(
        modifier = Modifier.fillMaxSize().nestedScroll(if (searching) searchScroll.nestedScrollConnection else normalScroll.nestedScrollConnection),
        topBar = {
            Column {
                AnimatedContent(searching) { active ->
                    if (active) TopAppBar(
                        title = {
                            SearchTextField(
                                modifier = Modifier.fillMaxWidth().padding(end = 8.dp).focusRequester(focus),
                                value = searchText, onClose = ::closeSearch, onValueChange = onSearchTextChange,
                            )
                        },
                        actions = toolbarActions, scrollBehavior = searchScroll,
                    ) else LargeTopAppBar(
                        title = {
                            Column {
                                Text(title, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(
                                    if (selectedSize == null) stringResource(R.string.items_selected, selectedCount, totalCount)
                                    else stringResource(R.string.items_selected_and_size, selectedCount, totalCount, selectedSize),
                                    maxLines = 1, overflow = TextOverflow.Ellipsis,
                                    style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        },
                        navigationIcon = {
                            IconButton(onClick = onBack) {
                                Icon(ImageVector.vectorResource(R.drawable.ic_arrow_left), stringResource(R.string.back))
                            }
                        },
                        actions = toolbarActions, scrollBehavior = normalScroll, colors = TopAppBarDefaults.surfaceTopAppBarColors(),
                    )
                }
                if (!searching) tabs()
            }
        },
    ) { padding ->
        if (loading) {
            LoadingIndicator(Modifier.padding(padding).padding(16.dp))
        } else if (failed) {
            Text(stringResource(R.string.restore_snapshot_load_failed), Modifier.padding(padding).padding(16.dp))
        } else AnimatedContent(
            targetState = listKey to items,
            contentKey = { (key, entries) -> key to entries.isEmpty() },
            modifier = Modifier.padding(top = padding.calculateTopPadding()),
        ) { (_, entries) ->
            if (entries.isEmpty()) Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Image(
                    ImageVector.vectorResource(R.drawable.img_empty), stringResource(R.string.it_is_empty),
                    modifier = Modifier.size(300.dp),
                )
                Text(
                    stringResource(R.string.it_is_empty), style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else LazyColumn(
                modifier = Modifier.fillMaxSize().verticalFadingEdges(fading), state = listState,
                contentPadding = PaddingValues(top = itemSpacing, bottom = padding.calculateBottomPadding()),
                verticalArrangement = Arrangement.spacedBy(itemSpacing),
            ) {
                items(entries, key = itemKey) { item -> itemContent(Modifier.animateItem(), item) }
            }
        }
    }
}
