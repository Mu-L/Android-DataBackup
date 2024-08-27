package com.xayah.feature.main.list

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.rounded.ArrowRight
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.CheckBox
import androidx.compose.material.icons.rounded.CheckBoxOutlineBlank
import androidx.compose.material.icons.rounded.Checklist
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material.icons.rounded.Rule
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.xayah.core.hiddenapi.castTo
import com.xayah.core.model.OpType
import com.xayah.core.model.Target
import com.xayah.core.ui.component.AnimatedModalDropdownMenu
import com.xayah.core.ui.component.Divider
import com.xayah.core.ui.component.IconButton
import com.xayah.core.ui.component.ModalDropdownMenu

@Composable
internal fun ListActions(
    viewModel: ListActionsViewModel = hiltViewModel(),
    target: Target,
    opType: OpType,
    onFilter: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    IconButton(icon = Icons.Outlined.FilterList, onClick = onFilter)

    var checkListExpanded by remember { mutableStateOf(false) }
    var checkListSelectedExpanded by remember { mutableStateOf(false) }
    Box(modifier = Modifier.wrapContentSize(Alignment.TopStart)) {
        IconButton(icon = Icons.Rounded.Checklist) {
            checkListSelectedExpanded = false
            checkListExpanded = true
        }
        AnimatedModalDropdownMenu(
            targetState = checkListSelectedExpanded,
            expanded = checkListExpanded,
            onDismissRequest = { checkListExpanded = false }
        ) {
            if (it.not()) {
                SelectAllItem {
                    viewModel.selectAll(opType)
                }
                UnselectAllItem {
                    viewModel.unselectAll(opType)
                }
                ReverseItem {
                    viewModel.reverseAll(opType)
                }
                Divider(modifier = Modifier.fillMaxWidth())
                ForSelectedItem {
                    checkListSelectedExpanded = true
                }
            } else {
                if (uiState is ListActionsUiState.Success) {
                    val state: ListActionsUiState.Success = uiState.castTo()
                    val enabled = state.listData.selected != 0L
                    when (target) {
                        Target.Apps -> {
                            when (opType) {
                                OpType.BACKUP -> {
                                    BlockItem(enabled) {}
                                    DetailedDataItem(enabled) {}
                                }

                                OpType.RESTORE -> {
                                    DeleteItem(enabled) {}
                                }
                            }
                        }

                        Target.Files -> {
                            when (opType) {
                                OpType.BACKUP -> {
                                    BlockItem(enabled) {}
                                    DetailedDataItem(enabled) {}
                                }

                                OpType.RESTORE -> {
                                    DeleteItem(enabled) {}
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    var moreExpanded by remember { mutableStateOf(false) }
    Box(modifier = Modifier.wrapContentSize(Alignment.TopStart)) {
        IconButton(icon = Icons.Rounded.MoreVert) {
            moreExpanded = true
        }
        ModalDropdownMenu(
            expanded = moreExpanded,
            onDismissRequest = { moreExpanded = false }
        ) {
            RefreshItem {}
        }
    }
}

@Composable
private fun DropdownMenuItem(
    text: String,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    DropdownMenuItem(
        text = { Text(text = text) },
        enabled = enabled,
        onClick = onClick,
        leadingIcon = if (leadingIcon != null) {
            { Icon(imageVector = leadingIcon, contentDescription = null) }
        } else {
            null
        },
        trailingIcon = if (trailingIcon != null) {
            { Icon(imageVector = trailingIcon, contentDescription = null) }
        } else {
            null
        },
    )
}

@Composable
private fun SelectAllItem(onClick: () -> Unit) {
    DropdownMenuItem(
        text = stringResource(id = R.string.select_all),
        leadingIcon = Icons.Rounded.CheckBox,
        onClick = onClick,
    )
}

@Composable
private fun UnselectAllItem(onClick: () -> Unit) {
    DropdownMenuItem(
        text = stringResource(id = R.string.unselect_all),
        leadingIcon = Icons.Rounded.CheckBoxOutlineBlank,
        onClick = onClick,
    )
}

@Composable
private fun ReverseItem(onClick: () -> Unit) {
    DropdownMenuItem(
        text = stringResource(id = R.string.reverse_selection),
        leadingIcon = Icons.Rounded.RestartAlt,
        onClick = onClick,
    )
}

@Composable
private fun ForSelectedItem(onClick: () -> Unit) {
    DropdownMenuItem(
        text = stringResource(id = R.string.for_selected),
        leadingIcon = Icons.Rounded.MoreVert,
        trailingIcon = Icons.Rounded.ArrowRight,
        onClick = onClick,
    )
}

@Composable
private fun BlockItem(enabled: Boolean, onClick: () -> Unit) {
    DropdownMenuItem(
        text = stringResource(id = R.string.block),
        leadingIcon = Icons.Rounded.Block,
        onClick = onClick,
        enabled = enabled,
    )
}

@Composable
private fun DetailedDataItem(enabled: Boolean, onClick: () -> Unit) {
    DropdownMenuItem(
        text = stringResource(id = R.string.detailed_data_items),
        leadingIcon = Icons.Rounded.Rule,
        onClick = onClick,
        enabled = enabled,
    )
}

@Composable
private fun DeleteItem(enabled: Boolean, onClick: () -> Unit) {
    DropdownMenuItem(
        text = stringResource(id = R.string.delete),
        leadingIcon = Icons.Rounded.Delete,
        onClick = onClick,
        enabled = enabled,
    )
}

@Composable
private fun RefreshItem(onClick: () -> Unit) {
    DropdownMenuItem(
        text = stringResource(id = R.string.refresh),
        leadingIcon = Icons.Rounded.Refresh,
        onClick = onClick,
    )
}