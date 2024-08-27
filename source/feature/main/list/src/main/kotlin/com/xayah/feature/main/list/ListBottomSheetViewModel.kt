package com.xayah.feature.main.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xayah.core.data.repository.ListData
import com.xayah.core.data.repository.ListDataRepo
import com.xayah.core.hiddenapi.castTo
import com.xayah.core.model.SortType
import com.xayah.feature.main.list.ListBottomSheetUiState.Loading
import com.xayah.feature.main.list.ListBottomSheetUiState.Success
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ListBottomSheetViewModel @Inject constructor(
    private val listDataRepo: ListDataRepo,
) : ViewModel() {
    val uiState: StateFlow<ListBottomSheetUiState> = listDataRepo.getListData().map { Success(listData = it) }.stateIn(
        scope = viewModelScope,
        initialValue = Loading,
        started = SharingStarted.WhileSubscribed(5_000),
    )

    private fun onSuccess(block: suspend (Success) -> Unit) {
        viewModelScope.launch {
            if (uiState.value is Success) {
                block(uiState.value.castTo())
            }
        }
    }

    fun setLoadSystemApps() = onSuccess { state ->
        listDataRepo.setShowSystemApps(state.listData.showSystemApps.not())
    }


    fun setSortByType() = onSuccess { state ->
        listDataRepo.setSortType(if (state.listData.sortType == SortType.ASCENDING) SortType.DESCENDING else SortType.ASCENDING)
    }

    fun setSortByIndex(index: Int) = onSuccess {
        listDataRepo.setSortIndex(index)
    }
}

sealed interface ListBottomSheetUiState {
    data object Loading : ListBottomSheetUiState
    data class Success(val listData: ListData) : ListBottomSheetUiState
}
