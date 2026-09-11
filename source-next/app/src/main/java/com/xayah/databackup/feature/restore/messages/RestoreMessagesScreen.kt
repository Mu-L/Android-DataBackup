package com.xayah.databackup.feature.restore.messages

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.xayah.databackup.R
import com.xayah.databackup.feature.restore.component.RestoreListScaffold
import com.xayah.databackup.ui.component.selection.MmsListItem
import com.xayah.databackup.ui.component.selection.SmsListItem
import com.xayah.databackup.util.Navigator
import com.xayah.databackup.util.popBackStackSafely

@Composable
fun RestoreMessagesScreen(navigator: Navigator, viewModel: MessagesViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val items by viewModel.items.collectAsStateWithLifecycle()
    val sms by viewModel.sms.collectAsStateWithLifecycle()
    val mms by viewModel.mms.collectAsStateWithLifecycle()
    val visibleItems by viewModel.visibleItems.collectAsStateWithLifecycle()
    val search by viewModel.searchText.collectAsStateWithLifecycle()
    val selectedIndex by viewModel.selectedIndex.collectAsStateWithLifecycle()
    RestoreListScaffold(
        title = stringResource(R.string.select_messages),
        items = visibleItems,
        itemKey = { it },
        selectedCount = items.count { it in state.selected },
        totalCount = items.size,
        searchText = search,
        loading = state.loading,
        onSearchTextChange = viewModel::changeSearchText,
        onBack = navigator::popBackStackSafely,
        onSelectAll = viewModel::selectAllMessages,
        listKey = selectedIndex,
        tabs = {
            val options = listOf(stringResource(R.string.sms), stringResource(R.string.mms))
            SingleChoiceSegmentedButtonRow(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                options.forEachIndexed { index, label ->
                    SegmentedButton(
                        shape = SegmentedButtonDefaults.itemShape(index, options.size),
                        selected = selectedIndex == index,
                        onClick = { viewModel.selectTab(index) },
                        label = { Text(label) },
                    )
                }
            }
        },
    ) { modifier, item ->
        val cardModifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
        if (item in sms) {
            sms[item]?.let { message ->
                SmsListItem(modifier = cardModifier, sms = message.copy(selected = item in state.selected)) { viewModel.selectItem(item, it) }
            }
        } else {
            mms[item]?.let { message ->
                MmsListItem(modifier = cardModifier, mms = message.copy(selected = item in state.selected)) { viewModel.selectItem(item, it) }
            }
        }
    }
}
