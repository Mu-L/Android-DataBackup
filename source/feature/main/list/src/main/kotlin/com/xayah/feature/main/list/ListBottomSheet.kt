package com.xayah.feature.main.list

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material.icons.outlined.ArrowDropUp
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.xayah.core.model.OpType
import com.xayah.core.model.SortType
import com.xayah.core.model.Target
import com.xayah.core.ui.component.BodyLargeText
import com.xayah.core.ui.component.IconButton
import com.xayah.core.ui.component.ModalBottomSheet
import com.xayah.core.ui.component.TitleLargeText
import com.xayah.core.ui.component.paddingHorizontal
import com.xayah.core.ui.component.paddingStart
import com.xayah.core.ui.component.paddingVertical
import com.xayah.core.ui.token.SizeTokens
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ListFilterSheet(
    viewModel: ListBottomSheetViewModel = hiltViewModel(),
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
    target: Target,
    opType: OpType,
    isShow: Boolean,
    onDismiss: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val sheetState = rememberModalBottomSheetState()
    val onDismissRequest: () -> Unit = {
        coroutineScope.launch { sheetState.hide() }.invokeOnCompletion {
            if (!sheetState.isVisible) {
                onDismiss()
            }
        }
    }

    if (uiState is ListBottomSheetUiState.Success) {
        val state = uiState as ListBottomSheetUiState.Success
        when (target) {
            Target.Apps -> {
                when (opType) {
                    OpType.BACKUP -> AppsBackupFilterSheet(
                        isShow = isShow,
                        sheetState = sheetState,
                        isLoadSystemApps = state.listData.showSystemApps,
                        sortIndex = state.listData.sortIndex,
                        sortType = state.listData.sortType,
                        onLoadSystemAppsChanged = viewModel::setLoadSystemApps,
                        onSortByType = viewModel::setSortByType,
                        onSortByIndex = viewModel::setSortByIndex,
                        onDismissRequest = onDismissRequest,
                    )

                    OpType.RESTORE -> {

                    }
                }
            }

            Target.Files -> {
                when (opType) {
                    OpType.BACKUP -> {

                    }

                    OpType.RESTORE -> {

                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AppsBackupFilterSheet(
    isShow: Boolean,
    sheetState: SheetState,
    isLoadSystemApps: Boolean,
    sortIndex: Int,
    sortType: SortType,
    onLoadSystemAppsChanged: () -> Unit,
    onSortByType: () -> Unit,
    onSortByIndex: (Int) -> Unit,
    onDismissRequest: () -> Unit,
) {
    if (isShow) {
        ModalBottomSheet(onDismissRequest = onDismissRequest, sheetState = sheetState) {
            Title(text = stringResource(id = R.string.filters))
            CheckBox(checked = isLoadSystemApps, text = stringResource(id = R.string.load_system_apps), onValueChange = { onLoadSystemAppsChanged() })

            TitleSort(text = stringResource(id = R.string.sort), sortType = sortType, onSort = onSortByType)
            RadioButtons(selected = sortIndex, items = stringArrayResource(id = R.array.backup_sort_type_items_apps).toList(), onSelect = onSortByIndex)
        }
    }
}

@Composable
private fun Title(text: String) {
    TitleLargeText(
        modifier = Modifier
            .paddingHorizontal(SizeTokens.Level24)
            .paddingVertical(SizeTokens.Level12),
        text = text
    )
}

@Composable
private fun TitleSort(text: String, sortType: SortType, onSort: () -> Unit) {
    Row(
        modifier = Modifier
            .paddingHorizontal(SizeTokens.Level24)
            .paddingVertical(SizeTokens.Level12),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TitleLargeText(text = text)
        IconButton(
            icon = when (sortType) {
                SortType.ASCENDING -> Icons.Outlined.ArrowDropUp
                SortType.DESCENDING -> Icons.Outlined.ArrowDropDown
            },
            onClick = onSort
        )
    }
}

@Composable
private fun CheckBox(
    checked: Boolean,
    text: String,
    onValueChange: (Boolean) -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .toggleable(
                value = checked,
                onValueChange = onValueChange,
                role = Role.Checkbox
            )
            .paddingHorizontal(SizeTokens.Level24)
            .paddingVertical(SizeTokens.Level12),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(checked = checked, onCheckedChange = null)
        BodyLargeText(modifier = Modifier.paddingStart(SizeTokens.Level16), text = text)
    }
}

@Composable
private fun RadioButtons(selected: Int, items: List<String>, onSelect: (Int) -> Unit) {
    Column(Modifier.selectableGroup()) {
        items.forEachIndexed { index, text ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = (index == selected),
                        onClick = {
                            onSelect(index)
                        },
                        role = Role.RadioButton
                    )
                    .paddingHorizontal(SizeTokens.Level24)
                    .paddingVertical(SizeTokens.Level12),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(selected = (index == selected), onClick = null)
                BodyLargeText(modifier = Modifier.paddingStart(SizeTokens.Level16), text = text)
            }
        }
    }
}
