package com.xayah.feature.main.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xayah.core.data.repository.AppsRepo
import com.xayah.core.data.repository.ListData
import com.xayah.core.data.repository.ListDataRepo
import com.xayah.core.hiddenapi.castTo
import com.xayah.core.model.OpType
import com.xayah.feature.main.list.ListActionsUiState.Loading
import com.xayah.feature.main.list.ListActionsUiState.Success
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ListActionsViewModel @Inject constructor(
    listDataRepo: ListDataRepo,
    private val appsRepo: AppsRepo,
) : ViewModel() {
    val uiState: StateFlow<ListActionsUiState> = listDataRepo.getListData().map { Success(listData = it) }.stateIn(
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

    fun selectAll(opType: OpType) = onSuccess { state ->
        appsRepo.selectAll(appsRepo.getAppIds(opType, state.listData.userList[state.listData.userIndex].id))
    }

    fun unselectAll(opType: OpType) = onSuccess { state ->
        appsRepo.unselectAll(appsRepo.getAppIds(opType, state.listData.userList[state.listData.userIndex].id))
    }

    fun reverseAll(opType: OpType) = onSuccess { state ->
        appsRepo.reverseAll(appsRepo.getAppIds(opType, state.listData.userList[state.listData.userIndex].id))
    }
}

sealed interface ListActionsUiState {
    data object Loading : ListActionsUiState
    data class Success(val listData: ListData) : ListActionsUiState
}
