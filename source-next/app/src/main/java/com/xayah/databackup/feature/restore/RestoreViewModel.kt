package com.xayah.databackup.feature.restore

import com.xayah.databackup.data.restore.RestoreRepository
import com.xayah.databackup.data.restore.RestoreSession
import com.xayah.databackup.data.restore.RestoreSessionState
import com.xayah.databackup.feature.RestoreRoute
import com.xayah.databackup.util.BaseViewModel
import com.xayah.databackup.util.LogHelper
import kotlinx.coroutines.CancellationException

class RestoreViewModel(
    private val route: RestoreRoute,
    private val repository: RestoreRepository,
) : BaseViewModel() {
    val session = RestoreSession()
    private var isLoading = false

    init {
        load()
    }

    fun load() {
        if (isLoading) return
        isLoading = true
        withLock {
            try {
                session.updateState(RestoreSessionState())
                val data = repository.loadSnapshot(route.configUuid, route.snapshotId)
                session.updateState(
                    RestoreSessionState(
                        loading = false,
                        config = data.config,
                        snapshot = data.snapshot,
                        inventory = data.inventory,
                        selected = data.inventory.allIds,
                        appParts = data.inventory.availableAppParts,
                    )
                )
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                LogHelper.e("RestoreViewModel", "load", "Failed to read snapshot inventory", error)
                session.updateState(RestoreSessionState(loading = false, failed = true))
            } finally {
                isLoading = false
            }
        }
    }
}
