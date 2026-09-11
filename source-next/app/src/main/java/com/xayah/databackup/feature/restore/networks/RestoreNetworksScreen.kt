package com.xayah.databackup.feature.restore.networks

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.xayah.databackup.R
import com.xayah.databackup.feature.restore.component.RestoreListScaffold
import com.xayah.databackup.ui.component.selection.NetworkListItem
import com.xayah.databackup.util.Navigator
import com.xayah.databackup.util.popBackStackSafely

@Composable
fun RestoreNetworksScreen(navigator: Navigator, viewModel: NetworksViewModel) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsStateWithLifecycle()
    val items by viewModel.items.collectAsStateWithLifecycle()
    val search by viewModel.searchText.collectAsStateWithLifecycle()
    val showPassword by viewModel.showPassword.collectAsStateWithLifecycle()
    RestoreListScaffold(
        title = stringResource(R.string.select_networks),
        items = items.entries.toList(),
        itemKey = { it.key },
        selectedCount = items.keys.count { it in state.selected },
        totalCount = items.size,
        searchText = search,
        loading = state.loading,
        onSearchTextChange = viewModel::changeSearchText,
        onBack = navigator::popBackStackSafely,
        onSelectAll = { viewModel.selectAll() },
        actions = {
            IconButton(onClick = viewModel::showOrHidePassword) {
                Icon(
                    ImageVector.vectorResource(if (showPassword) R.drawable.ic_eye else R.drawable.ic_eye_off),
                    stringResource(if (showPassword) R.string.hide_password else R.string.show_password),
                )
            }
        },
    ) { modifier, item ->
        val selected = remember(item.key, state.selected) { item.key in state.selected }
        NetworkListItem(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            context = context,
            network = item.value,
            selected = selected,
            onCheckedChange = { viewModel.selectItem(item.key, it) },
            showPassword = showPassword,
        )
    }
}
