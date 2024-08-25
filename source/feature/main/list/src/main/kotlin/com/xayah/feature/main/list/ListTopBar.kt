package com.xayah.feature.main.list

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.xayah.core.model.Operation
import com.xayah.core.model.Targets
import com.xayah.core.ui.component.SecondaryTopBar

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
internal fun ListTopBar(
    uiState: ListUiState,
) {
    val title: String,
    var subtitle: String? = null
    when (uiState) {
        is ListUiState.Loading -> {
            title = stringResource(id = R.string.loading)
        }

        is ListUiState.Loaded -> {
            title = when (uiState.targets) {
                Targets.Apps -> {
                    when (uiState.operation) {
                        Operation.Backup -> stringResource(id = R.string.select_apps)

                        Operation.Restore -> stringResource(id = R.string.select_apps)
                    }
                }

                Targets.Files -> {
                    when (uiState.operation) {
                        Operation.Backup -> stringResource(id = R.string.select_apps)

                        Operation.Restore -> stringResource(id = R.string.select_apps)
                    }
                }
            }

            if (uiState.selected != 0 && uiState.total != 0) {
                subtitle = "(${uiState.selected}/${uiState.total})"
            }
        }
    }

    SecondaryTopBar(
        scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState()),
        title = title,
        subtitle = subtitle,
        actions = actions,
    )
}