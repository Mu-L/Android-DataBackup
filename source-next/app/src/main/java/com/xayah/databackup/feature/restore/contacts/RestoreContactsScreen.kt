package com.xayah.databackup.feature.restore.contacts

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.xayah.databackup.R
import com.xayah.databackup.feature.restore.component.RestoreListScaffold
import com.xayah.databackup.ui.component.selection.ContactListItem
import com.xayah.databackup.util.Navigator
import com.xayah.databackup.util.popBackStackSafely

@Composable
fun RestoreContactsScreen(navigator: Navigator, viewModel: ContactsViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val items by viewModel.items.collectAsStateWithLifecycle()
    val search by viewModel.searchText.collectAsStateWithLifecycle()
    RestoreListScaffold(
        title = stringResource(R.string.select_contacts),
        items = items.entries.toList(),
        itemKey = { it.key },
        selectedCount = items.keys.count { it in state.selected },
        totalCount = items.size,
        searchText = search,
        loading = state.loading,
        onSearchTextChange = viewModel::changeSearchText,
        onBack = navigator::popBackStackSafely,
        onSelectAll = { viewModel.selectAll() },
    ) { modifier, item ->
        ContactListItem(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            contact = item.value.copy(selected = item.key in state.selected),
            onCheckedChange = { viewModel.selectItem(item.key, it) },
        )
    }
}
