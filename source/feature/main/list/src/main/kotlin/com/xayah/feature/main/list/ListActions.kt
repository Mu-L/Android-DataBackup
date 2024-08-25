package com.xayah.feature.main.list

import androidx.compose.runtime.Composable
import com.xayah.core.model.Operation
import com.xayah.core.model.Targets

@Composable
internal fun ListActions(
    uiState: ListUiState,
) {
    if (uiState is ListUiState.Loaded) {
        when (uiState.targets) {
            Targets.Apps -> {
                when (uiState.operation) {
                    Operation.Backup -> {

                    }

                    Operation.Restore -> {

                    }
                }
            }

            Targets.Files -> {
                when (uiState.operation) {
                    Operation.Backup -> {

                    }

                    Operation.Restore -> {

                    }
                }
            }
        }
    }
}

