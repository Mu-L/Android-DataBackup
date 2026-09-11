package com.xayah.databackup.ui.component.selection

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import com.xayah.databackup.R
import com.xayah.databackup.ui.material3.ModalDropdownMenu
import com.xayah.databackup.ui.material3.ModalDropdownMenuItem

@Composable
fun AppSelectionMenu(
    apkAllSelected: Boolean,
    dataAllSelected: Boolean,
    intDataAllSelected: Boolean,
    extDataAllSelected: Boolean,
    addlDataAllSelected: Boolean,
    onSelectAllApk: () -> Unit,
    onSelectAllData: () -> Unit,
    onSelectAllIntData: () -> Unit,
    onSelectAllExtData: () -> Unit,
    onSelectAllAddlData: () -> Unit,
) {
    var mainExpanded by remember { mutableStateOf(false) }
    var customExpanded by remember { mutableStateOf(false) }


    Box {
        IconButton(onClick = {
            mainExpanded = mainExpanded.not()
        }) {
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.ic_list_checks),
                contentDescription = stringResource(R.string.select_all)
            )
        }
        ModalDropdownMenu(
            expanded = mainExpanded,
            onDismissRequest = { mainExpanded = false }
        ) {
            ModalDropdownMenuItem(
                text = { Text(if (apkAllSelected.not()) stringResource(R.string.select_all_apk) else stringResource(R.string.unselect_all_apk)) },
                leadingIcon = {
                    Icon(
                        imageVector = if (apkAllSelected.not())
                            ImageVector.vectorResource(R.drawable.ic_square_check_big)
                        else
                            ImageVector.vectorResource(R.drawable.ic_square),
                        contentDescription = if (apkAllSelected.not()) stringResource(R.string.select_all_apk)
                        else stringResource(R.string.unselect_all_apk)
                    )
                },
                onClick = {
                    onSelectAllApk()
                }
            )
            ModalDropdownMenuItem(
                text = { Text(if (dataAllSelected.not()) stringResource(R.string.select_all_data) else stringResource(R.string.unselect_all_data)) },
                leadingIcon = {
                    Icon(
                        imageVector = if (dataAllSelected.not())
                            ImageVector.vectorResource(R.drawable.ic_square_check_big)
                        else
                            ImageVector.vectorResource(R.drawable.ic_square),
                        contentDescription = if (dataAllSelected.not()) stringResource(R.string.select_all_data)
                        else stringResource(R.string.unselect_all_data)
                    )
                },
                onClick = {
                    onSelectAllData()
                }
            )
            HorizontalDivider()
            ModalDropdownMenuItem(
                text = { Text(stringResource(R.string.custom_selection)) },
                trailingIcon = {
                    Icon(
                        imageVector = ImageVector.vectorResource(R.drawable.ic_arrow_right),
                        contentDescription = stringResource(R.string.custom_selection)
                    )
                },
                onClick = {
                    mainExpanded = false
                    customExpanded = true
                }
            )
        }

        ModalDropdownMenu(
            expanded = customExpanded,
            onDismissRequest = { customExpanded = false }
        ) {
            ModalDropdownMenuItem(
                text = { Text(stringResource(R.string.word_return)) },
                leadingIcon = {
                    Icon(
                        imageVector = ImageVector.vectorResource(R.drawable.ic_arrow_left),
                        contentDescription = stringResource(R.string.word_return)
                    )
                },
                onClick = {
                    mainExpanded = true
                    customExpanded = false
                }
            )
            HorizontalDivider()
            ModalDropdownMenuItem(
                text = { Text(if (apkAllSelected.not()) stringResource(R.string.select_all_apk) else stringResource(R.string.unselect_all_apk)) },
                leadingIcon = {
                    Icon(
                        imageVector = if (apkAllSelected.not())
                            ImageVector.vectorResource(R.drawable.ic_square_check_big)
                        else
                            ImageVector.vectorResource(R.drawable.ic_square),
                        contentDescription = if (apkAllSelected.not()) stringResource(R.string.select_all_apk)
                        else stringResource(R.string.unselect_all_apk)
                    )
                },
                onClick = {
                    onSelectAllApk()
                }
            )
            ModalDropdownMenuItem(
                text = {
                    Text(
                        if (intDataAllSelected.not()) stringResource(R.string.select_all_int_data)
                        else stringResource(R.string.unselect_all_int_data)
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = if (intDataAllSelected.not())
                            ImageVector.vectorResource(R.drawable.ic_square_check_big)
                        else
                            ImageVector.vectorResource(R.drawable.ic_square),
                        contentDescription = if (intDataAllSelected.not()) stringResource(R.string.select_all_int_data)
                        else stringResource(R.string.unselect_all_int_data)
                    )
                },
                onClick = {
                    onSelectAllIntData()
                }
            )
            ModalDropdownMenuItem(
                text = {
                    Text(
                        if (extDataAllSelected.not()) stringResource(R.string.select_all_ext_data)
                        else stringResource(R.string.unselect_all_ext_data)
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = if (extDataAllSelected.not())
                            ImageVector.vectorResource(R.drawable.ic_square_check_big)
                        else
                            ImageVector.vectorResource(R.drawable.ic_square),
                        contentDescription = if (extDataAllSelected.not()) stringResource(R.string.select_all_ext_data)
                        else stringResource(R.string.unselect_all_ext_data)
                    )
                },
                onClick = {
                    onSelectAllExtData()
                }
            )
            ModalDropdownMenuItem(
                text = {
                    Text(
                        if (addlDataAllSelected.not()) stringResource(R.string.select_all_addl_data)
                        else stringResource(R.string.unselect_all_addl_data)
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = if (addlDataAllSelected.not())
                            ImageVector.vectorResource(R.drawable.ic_square_check_big)
                        else
                            ImageVector.vectorResource(R.drawable.ic_square),
                        contentDescription = if (addlDataAllSelected.not()) stringResource(R.string.select_all_addl_data)
                        else stringResource(R.string.unselect_all_addl_data)
                    )
                },
                onClick = {
                    onSelectAllAddlData()
                }
            )
        }
    }
}
