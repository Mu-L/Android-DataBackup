package com.xayah.core.data.repository

import com.xayah.core.database.dao.PackageDao
import com.xayah.core.datastore.di.DbDispatchers.Default
import com.xayah.core.datastore.di.Dispatcher
import com.xayah.core.model.App
import com.xayah.core.model.DataState
import com.xayah.core.model.OpType
import com.xayah.core.model.database.PackageEntity
import com.xayah.core.model.database.asExternalModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class AppsRepo @Inject constructor(
    @Dispatcher(Default) private val defaultDispatcher: CoroutineDispatcher,
    private val appsDao: PackageDao,
    private val packageRepo: PackageRepository,
) {
    fun getApps(
        opType: OpType,
        listData: Flow<ListData>,
    ): Flow<List<App>> = combine(
        listData,
        appsDao.queryPackagesFlow(opType = opType, existed = true, blocked = false)
    ) { lData, apps ->
        apps.asSequence()
            .filter(packageRepo.getKeyPredicateNew(key = lData.searchQuery))
            .filter(packageRepo.getShowSystemAppsPredicate(value = lData.showSystemApps))
            .filter(packageRepo.getUserIdPredicateNew(userId = lData.userList.getOrNull(lData.userIndex)?.id))
            .sortedWith(packageRepo.getSortComparatorNew(sortIndex = lData.sortIndex, sortType = lData.sortType))
            .sortedByDescending { p -> p.extraInfo.activated }.toList()
            .map(PackageEntity::asExternalModel)
    }.flowOn(defaultDispatcher)

    suspend fun getAppIds(opType: OpType, userId: Int) = appsDao.queryAppIds(opType = opType, userId = userId, existed = true, blocked = false)

    fun countApps(opType: OpType) = appsDao.countPackagesFlow(opType = opType, existed = true, blocked = false)
    fun countSelectedApps(opType: OpType) = appsDao.countActivatedPackagesFlow(opType = opType, existed = true, blocked = false)

    suspend fun selectApp(id: Long, selected: Boolean) {
        appsDao.activateById(id, selected)
    }

    suspend fun selectDataItems(id: Long, apk: DataState, user: DataState, userDe: DataState, data: DataState, obb: DataState, media: DataState) {
        appsDao.selectDataItemsById(id, apk.name, user.name, userDe.name, data.name, obb.name, media.name)
    }

    suspend fun selectAll(ids: List<Long>) {
        appsDao.activateByIds(ids, true)
    }

    suspend fun unselectAll(ids: List<Long>) {
        appsDao.activateByIds(ids, false)
    }

    suspend fun reverseAll(ids: List<Long>) {
        appsDao.reverseActivatedByIds(ids)
    }
}
