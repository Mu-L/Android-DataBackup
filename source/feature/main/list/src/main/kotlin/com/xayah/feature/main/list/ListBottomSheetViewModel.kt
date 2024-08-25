package com.xayah.feature.main.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xayah.core.data.repository.SettingsDataRepo
import com.xayah.core.hiddenapi.castTo
import com.xayah.core.model.SettingsData
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
    private val settingsDataRepo: SettingsDataRepo,
) : ViewModel() {
    val uiState: StateFlow<ListBottomSheetUiState> = settingsDataRepo.settingsData.map { Success(settingsData = it) }.stateIn(
        scope = viewModelScope,
        initialValue = Loading,
        started = SharingStarted.WhileSubscribed(5_000),
    )

    fun setLoadSystemApps() {
        viewModelScope.launch {
            if (uiState.value is Success) {
                settingsDataRepo.setLoadSystemApps(uiState.value.castTo<Success>().settingsData.isLoadSystemApps.not())
            }
        }
    }

    fun setSortByType() {}
    fun setSortByIndex(index: Int) {}
}

sealed interface ListBottomSheetUiState {
    data object Loading : ListBottomSheetUiState
    data class Success(
        val sortIndex: Int = 0,
        val sortType: SortType = SortType.ASCENDING,
        val settingsData: SettingsData,
    ) : ListBottomSheetUiState
}
