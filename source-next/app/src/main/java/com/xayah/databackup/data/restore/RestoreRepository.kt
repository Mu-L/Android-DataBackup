package com.xayah.databackup.data.restore

import com.xayah.databackup.data.BackupConfigRepository
import com.xayah.databackup.data.rustic.RusticBackupGateway
import com.xayah.databackup.data.rustic.RusticRestoreInventory
import com.xayah.databackup.data.rustic.RusticRestoreInventoryReader
import com.xayah.databackup.data.rustic.RusticSnapshot
import com.xayah.databackup.entity.BackupBackend
import com.xayah.databackup.entity.BackupConfig
import com.xayah.databackup.util.PathHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class RestoreSnapshotData(val config: BackupConfig, val snapshot: RusticSnapshot, val inventory: RusticRestoreInventory)

class RestoreRepository(private val configs: BackupConfigRepository, private val gateway: RusticBackupGateway) {
    suspend fun loadSnapshot(configUuid: String, snapshotId: String): RestoreSnapshotData = withContext(Dispatchers.IO) {
        if (!configs.isLoaded.value) configs.loadBackupConfigsFromLocal()
        val config = requireNotNull(configs.configs.value.find { it.uuidString == configUuid })
        val backend = checkNotNull(config.backupBackend as? BackupBackend.Rustic) { "Snapshot restore requires a Rustic backup configuration." }
        val repository = PathHelper.getBackupRepoDir(config.path)
        val snapshot = gateway.listSnapshots(repository, backend.password).single { it.id == snapshotId }
        val reader = RusticRestoreInventoryReader()
        val manifestPath = PathHelper.getRusticSnapshotMetadataFilePath(PathHelper.getRusticManifestFileRelativePath())
        val manifest =
            reader.manifest(gateway.readSnapshotTextFiles(repository, backend.password, snapshot.id, listOf(manifestPath)).getValue(manifestPath))
        val paths = reader.structuredPaths(manifest)
        val files = if (paths.isEmpty()) emptyMap() else gateway.readSnapshotTextFiles(
            repositoryPath = repository,
            password = backend.password,
            snapshotId = snapshot.id,
            paths = paths.map(PathHelper::getRusticSnapshotMetadataFilePath),
        ).mapKeys { (path, _) -> path.removePrefix("${PathHelper.getRusticSnapshotMetadataDir()}/") }
        val inventory = reader.inventory(manifest, files)
        RestoreSnapshotData(config, snapshot, inventory)
    }
}
