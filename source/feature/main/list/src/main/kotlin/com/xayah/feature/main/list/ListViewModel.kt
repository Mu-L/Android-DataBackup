package com.xayah.feature.main.list

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xayah.core.data.repository.AppsRepo
import com.xayah.core.data.repository.ListData
import com.xayah.core.data.repository.ListDataRepo
import com.xayah.core.data.repository.UsersRepo
import com.xayah.core.model.App
import com.xayah.core.model.OpType
import com.xayah.core.model.Target
import com.xayah.core.model.UserInfo
import com.xayah.core.model.database.MediaEntity
import com.xayah.core.model.util.of
import com.xayah.core.ui.route.MainRoutes
import com.xayah.core.util.decodeURL
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ListViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val listDataRepo: ListDataRepo,
    appsRepo: AppsRepo,
    usersRepo: UsersRepo,
) : ViewModel() {
    private val target: Target = Target.valueOf(savedStateHandle.get<String>(MainRoutes.ARG_TARGET)!!.decodeURL().trim())
    private val opType: OpType = OpType.of(savedStateHandle.get<String>(MainRoutes.ARG_OP_TYPE)?.decodeURL()?.trim())

    init {
        // Reset list data
        listDataRepo.initialize(opType)
    }

    private val listData: Flow<ListData> = listDataRepo.getListData()
    private val usersMap: StateFlow<Map<Int, Long>> = usersRepo.getUsersMap(opType).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = mapOf(),
    ) // UserId to items count

    val uiState: StateFlow<ListUiState> =
        when (target) {
            Target.Apps -> combine(
                listData,
                usersMap,
                appsRepo.getApps(opType, listData)
            ) { lData, uMap, apps ->
                ListUiState.Success.Apps(
                    opType = opType,
                    selected = lData.selected,
                    total = lData.total,
                    userIndex = lData.userIndex,
                    userList = lData.userList,
                    usersMap = uMap,
                    list = apps
                )
            }

            Target.Files -> listData.map { _ ->
                ListUiState.Success.Files(
                    opType, 0, 0, listOf()
                )
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ListUiState.Loading,
        )

    fun search(text: String) {
        viewModelScope.launch {
            listDataRepo.setSearchQuery(text)
        }
    }

    fun setUser(index: Int) {
        viewModelScope.launch {
            listDataRepo.setUserIndex(index)
        }
    }
}

sealed interface ListUiState {
    data object Loading : ListUiState
    sealed class Success(
        val target: Target,
        open val opType: OpType,
        open val selected: Long,
        open val total: Long,
    ) : ListUiState {
        data class Apps(
            override val opType: OpType,
            override val selected: Long,
            override val total: Long,
            val userIndex: Int,
            val userList: List<UserInfo>,
            val usersMap: Map<Int, Long>,
            val list: List<App>,
        ) : Success(target = Target.Apps, opType = opType, selected = selected, total = total)

        data class Files(
            override val opType: OpType,
            override val selected: Long,
            override val total: Long,
            val list: List<MediaEntity>
        ) : Success(target = Target.Files, opType = opType, selected = selected, total = total)
    }

    // TODO: data object Empty : ListUiState
}
