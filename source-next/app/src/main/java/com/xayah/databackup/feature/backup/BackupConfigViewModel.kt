package com.xayah.databackup.feature.backup

import androidx.lifecycle.viewModelScope
import arrow.optics.copy
import com.xayah.databackup.data.BackupConfigRepository
import com.xayah.databackup.data.rustic.RusticBackupGateway
import com.xayah.databackup.data.rustic.RusticSnapshot
import com.xayah.databackup.entity.BackupBackend
import com.xayah.databackup.entity.BackupConfig
import com.xayah.databackup.entity.name
import com.xayah.databackup.feature.BackupConfigRoute
import com.xayah.databackup.util.BaseViewModel
import com.xayah.databackup.util.PathHelper
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.withContext

data class BackupSnapshotsState(
    val snapshots: List<RusticSnapshot>? = null,
    val isLoading: Boolean = true,
    val hasError: Boolean = false,
)

open class BackupConfigViewModel(
    private val route: BackupConfigRoute,
    private val backupConfigRepo: BackupConfigRepository,
    private val snapshotGateway: RusticBackupGateway,
) : BaseViewModel() {
    companion object {
        private val sharingStarted = SharingStarted.WhileSubscribed(5_000)
    }

    private val currentConfig: BackupConfig?
        get() = backupConfigRepo.configs.value.getOrNull(route.index)

    val backupConfig: StateFlow<BackupConfig?> =
        backupConfigRepo.configs.map { configs ->
            configs.getOrNull(route.index)
        }.stateIn(
            scope = viewModelScope,
            initialValue = currentConfig,
            started = sharingStarted,
        )

    private val _snapshots = MutableStateFlow(BackupSnapshotsState())
    val snapshots = _snapshots.asStateFlow()

    private val _deletingSnapshot = MutableStateFlow(false)
    val deletingSnapshot = _deletingSnapshot.asStateFlow()
    private val _snapshotDeleteFailed = MutableStateFlow(false)
    val snapshotDeleteFailed = _snapshotDeleteFailed.asStateFlow()

    fun clearSnapshotDeleteError() {
        _snapshotDeleteFailed.value = false
    }

    fun deleteSnapshot(config: BackupConfig, snapshotId: String, onDeleted: () -> Unit) {
        val backend = config.backupBackend as? BackupBackend.Rustic ?: return
        if (_deletingSnapshot.value) return
        _deletingSnapshot.value = true
        _snapshotDeleteFailed.value = false
        withLock(Dispatchers.IO) {
            try {
                val repositoryPath = PathHelper.getBackupRepoDir(config.path)
                val snapshots = snapshotGateway.deleteSnapshot(repositoryPath, backend.password, snapshotId)
                if (snapshotsRepositoryPath == repositoryPath) {
                    _snapshots.value = BackupSnapshotsState(snapshots = snapshots, isLoading = false)
                }
                withContext(Dispatchers.Main) {
                    onDeleted()
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                _snapshotDeleteFailed.value = true
            } finally {
                _deletingSnapshot.value = false
            }
        }
    }

    private var snapshotsRepositoryPath: String? = null

    fun refreshSnapshots(config: BackupConfig) = withLock(Dispatchers.IO) {
        refreshSnapshotsLocked(config)
    }

    private suspend fun refreshSnapshotsLocked(config: BackupConfig) {
        val backend = config.backupBackend as? BackupBackend.Rustic ?: return
        val repositoryPath = PathHelper.getBackupRepoDir(config.path)
        val retained = _snapshots.value.snapshots.takeIf { snapshotsRepositoryPath == repositoryPath }
        snapshotsRepositoryPath = repositoryPath
        _snapshots.value = BackupSnapshotsState(snapshots = retained)
        val cached = retained ?: snapshotGateway.readCachedSnapshots(repositoryPath)?.sortedByDescending { it.createdAt }
        _snapshots.value = BackupSnapshotsState(snapshots = cached)
        try {
            val snapshots = if (snapshotGateway.repositoryExists(repositoryPath)) {
                snapshotGateway.listSnapshots(repositoryPath, backend.password).sortedByDescending { it.createdAt }
            } else {
                emptyList()
            }
            _snapshots.value = BackupSnapshotsState(snapshots = snapshots, isLoading = false)
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            _snapshots.value = BackupSnapshotsState(snapshots = cached, isLoading = false, hasError = true)
        }
    }

    fun changeName(name: String) {
        withLock(Dispatchers.Default) {
            currentConfig?.let { config ->
                backupConfigRepo.updateConfig(config.uuidString) {
                    copy {
                        BackupConfig.name set name
                    }
                }
            }
        }
    }

    fun deleteConfig(onDeleted: suspend () -> Unit) {
        withLock(Dispatchers.Default) {
            currentConfig?.let { config ->
                backupConfigRepo.deleteConfig(config.uuidString)
            }
            onDeleted()
        }
    }

    fun selectBackup(onSelected: () -> Unit) {
        withLock(Dispatchers.IO) {
            backupConfigRepo.selectBackup(route.index)
            withContext(Dispatchers.Main) {
                onSelected()
            }
        }
    }
}
