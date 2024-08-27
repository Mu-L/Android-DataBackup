package com.xayah.core.data.repository

import com.xayah.core.database.dao.PackageDao
import com.xayah.core.datastore.di.DbDispatchers.Default
import com.xayah.core.datastore.di.Dispatcher
import com.xayah.core.model.OpType
import com.xayah.core.model.UserInfo
import com.xayah.core.rootservice.service.RemoteRootService
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class UsersRepo @Inject constructor(
    @Dispatcher(Default) private val defaultDispatcher: CoroutineDispatcher,
    private val rootService: RemoteRootService,
    private val appsDao: PackageDao,
) {
    fun getUsers(): Flow<List<UserInfo>> = flow {
        emit(
            rootService.getUsers().map { UserInfo(it.id, it.name) }
        )
    }.flowOn(defaultDispatcher)

    fun getUsersMap(opType: OpType): Flow<Map<Int, Long>> = appsDao.countUsersMapFlow(opType = opType, existed = true, blocked = false)
}
