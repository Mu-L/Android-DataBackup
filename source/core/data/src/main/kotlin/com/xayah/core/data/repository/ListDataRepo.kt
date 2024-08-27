package com.xayah.core.data.repository

import com.xayah.core.model.OpType
import com.xayah.core.model.SortType
import com.xayah.core.model.UserInfo
import com.xayah.core.util.module.combine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ListDataRepo @Inject constructor(
    private val usersRepo: UsersRepo,
    private val appsRepo: AppsRepo,
) {
    private lateinit var selected: Flow<Long>
    private lateinit var total: Flow<Long>
    private lateinit var showSystemApps: MutableStateFlow<Boolean>
    private lateinit var sortIndex: MutableStateFlow<Int>
    private lateinit var sortType: MutableStateFlow<SortType>
    private lateinit var searchQuery: MutableStateFlow<String>
    private lateinit var userIndex: MutableStateFlow<Int>
    private lateinit var userList: Flow<List<UserInfo>>

    fun initialize(opType: OpType) {
        selected = appsRepo.countSelectedApps(opType)
        total = appsRepo.countApps(opType)
        appsRepo.countApps(opType)
        showSystemApps = MutableStateFlow(false)
        sortIndex = MutableStateFlow(0)
        sortType = MutableStateFlow(SortType.ASCENDING)
        searchQuery = MutableStateFlow("")
        userIndex = MutableStateFlow(0)
        userList = usersRepo.getUsers()
    }

    fun getListData(): Flow<ListData> = combine(
        selected,
        total,
        showSystemApps,
        sortIndex,
        sortType,
        searchQuery,
        userIndex,
        userList,
    ) { s, t, sSystemApps, sIndex, sType, sQuery, uIndex, uList ->
        ListData(s, t, sSystemApps, sIndex, sType, sQuery, uIndex, uList)
    }

    suspend fun setShowSystemApps(value: Boolean) {
        showSystemApps.emit(value)
    }

    suspend fun setSortIndex(value: Int) {
        sortIndex.emit(value)
    }

    suspend fun setSortType(value: SortType) {
        sortType.emit(value)
    }

    suspend fun setSearchQuery(value: String) {
        searchQuery.emit(value)
    }

    suspend fun setUserIndex(value: Int) {
        userIndex.emit(value)
    }
}

data class ListData(
    val selected: Long,
    val total: Long,
    val showSystemApps: Boolean,
    val sortIndex: Int,
    val sortType: SortType,
    val searchQuery: String,
    val userIndex: Int,
    val userList: List<UserInfo>,
)
