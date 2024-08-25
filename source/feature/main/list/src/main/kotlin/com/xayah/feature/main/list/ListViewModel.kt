package com.xayah.feature.main.list

import com.xayah.core.model.Operation
import com.xayah.core.model.Targets
import com.xayah.core.model.database.MediaEntity
import com.xayah.core.model.database.PackageEntity

internal sealed interface ListUiState {
    data object Loading : ListUiState
    sealed class Loaded(
        val targets: Targets,
        open val operation: Operation,
        open val selected: Int,
        open val total: Int,
        open val showFilterSheet: Boolean,
    ) : ListUiState {
        data class Apps(
            override val operation: Operation,
            override val selected: Int,
            override val total: Int,
            override val showFilterSheet: Boolean,
            val list: List<PackageEntity>,
        ) : Loaded(targets = Targets.Apps, operation = operation, selected = selected, total = total, showFilterSheet = showFilterSheet)

        data class Files(
            override val operation: Operation,
            override val selected: Int,
            override val total: Int,
            override val showFilterSheet: Boolean,
            val list: List<MediaEntity>
        ) : Loaded(targets = Targets.Files, operation = operation, selected = selected, total = total, showFilterSheet = showFilterSheet)
    }

    // TODO: data object Empty : ListUiState
}
